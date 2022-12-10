@file:Depends("coreMindustry/utilNext", "调用菜单")
@file:Depends("coreMindustry/contentsTweaker", "修改核心单位,单位属性")
package mapScript

import arc.struct.ObjectIntMap
import arc.util.Align
import arc.util.Time
import arc.util.Tmp
import coreLibrary.lib.util.loop
import coreMindustry.ContentsTweaker
import coreMindustry.UtilNext
import coreMindustry.lib.game
import mindustry.Vars
import mindustry.Vars.dataDirectory
import mindustry.ai.Pathfinder
import mindustry.ai.types.CommandAI
import mindustry.content.*
import mindustry.core.World
import mindustry.ctype.ContentType
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.BulletType
import mindustry.entities.effect.MultiEffect
import mindustry.entities.pattern.ShootAlternate
import mindustry.game.EventType
import mindustry.game.SpawnGroup
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.gen.Sounds
import mindustry.graphics.Pal
import mindustry.logic.LExecutor.SpawnUnitI
import mindustry.logic.LStatements.SpawnUnitStatement
import mindustry.type.Category
import mindustry.type.ItemStack
import mindustry.type.UnitType
import mindustry.type.Weapon
import mindustry.type.unit.MissileUnitType
import mindustry.world.blocks.campaign.LaunchPad
import mindustry.world.blocks.campaign.LaunchPad.LaunchPadBuild
import mindustry.world.blocks.defense.turrets.ItemTurret
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.blocks.storage.CoreBlock
import mindustry.world.blocks.storage.CoreBlock.CoreBuild
import kotlin.random.Random

val playerchance: ObjectIntMap<String> = ObjectIntMap()
fun Player.setplayerchance(amount: Int){
    return playerchance.put(uuid(),playerchance.get(uuid())-playerchance.get(uuid())+amount)
}
fun Player.getplayerchance():Int {
    return playerchance.get(uuid())
}
fun Player.removeplayerchance(amount: Int){
    return playerchance.put(uuid(),playerchance.get(uuid())-amount)
}
val list = Vars.content.getBy<UnitType>(ContentType.unit).filterNot { it.internal }
val missile = arrayOf(
    list[63],
    UnitTypes.anthicus.weapons.get(0).bullet.spawnUnit,
    UnitTypes.quell.weapons.get(0).bullet.spawnUnit,
    UnitTypes.disrupt.weapons.get(0).bullet.spawnUnit,
    UnitTypes.elude
)
val units = arrayOf(
    UnitTypes.collaris,
    UnitTypes.disrupt,
    UnitTypes.conquer,
    UnitTypes.navanax,
    UnitTypes.navanax,
    UnitTypes.omura,
    UnitTypes.eclipse,
    UnitTypes.toxopid,
    UnitTypes.reign
)
val menu = contextScript<UtilNext>()
/*
fun Player.launchpadmenu(x :Float, y :Float, task: LaunchPadBuild){
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        while(true){
            delay(200)
            if (Time.millis() - dorpTime >= 1_000) break
            Call.label(con ?: return@launch, "[green]点我放置炮塔", 0.21f,
                dropX, dropY
            )
        }
    }
}

 */
/*
suspend fun Player.destoryMenu(Launchpad: LaunchPadBuild) {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            炮塔放置界面
        """.trimIndent()
    ) {
      this += listOf(
          "[炮塔]" to {
              var tile = Vars.world.tiles.getn(
                  Launchpad.x.toInt()/8,
                  Launchpad.y.toInt()/8
              )
              tile.setNet(Blocks.spectre,Team.get(1), 0)
          }
      )
    }
}

 */
//Thanks for klp
fun Player.createUnit(core: CoreBuild,unitType: UnitType): Boolean{
    if (unitType == null) return false
    var times = 0
    val spawnRadius = 20
    val unit = unitType.create(team())
    unit.apply {
        while (true){
            Tmp.v1.rnd(spawnRadius.toFloat() * Vars.tilesize)
            val sx = core.x + Tmp.v1.x
            val sy = core.y + Tmp.v1.y

            if (canPass(World.toTile(sx), World.toTile(sy))) {
                set(sx, sy)
                break
            }

            if (++times > 20) {
                return false
            }
        }
        launch(Dispatchers.game){
            val spawnTime = Time.millis()
            while(Time.timeSinceMillis(spawnTime) / 1000 <= 10){
                Call.label("${unit.type.emoji()}[red]神仙单位部署[white]${unit.type.emoji()}\n$name [white]${10 - Time.timeSinceMillis(spawnTime) / 1000}", 0.2026f, x, y)
                Call.effect(Fx.spawnShockwave, x, y, 0f, team().color)
                delay(200)
            }
            spawnedByCore = true
            apply(StatusEffects.boss, Float.MAX_VALUE)
            apply(StatusEffects.overdrive, Float.MAX_VALUE)
            apply(StatusEffects.overclock, Float.MAX_VALUE)
            apply(StatusEffects.shielded, Float.MAX_VALUE)
            add()
            unit(unit)
            Call.announce("$name [#${team().color}]单位\n!${unit.type.emoji()}出征${unit.type.emoji()}!")
            Call.logicExplosion(team, x, y, 32f * 8f, 7500f, true, true, true)
            Call.effect(Fx.impactReactorExplosion, x, y, 0f, team().color)
        }
    }
    return true
}

suspend fun Player.spawnMenu(core: CoreBuild) {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            [red]随机部署一位神仙单位
        """.trimIndent()
    ) {
        if(unit().type !in units) {
            if(getplayerchance() == 0) {
                this += listOf(
                    "[部署]" to {
                        removeplayerchance(1)
                        createUnit(core, units.random())
                    }
                )
            }else{
                this += listOf(
                    "[部署机会不足]" to {
                    }
                )
            }
        }
        else{
            this += listOf(
                "[你已经部署]" to {
                }
            )
        }
    }
}
/*
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    if((it.tile.block() is LaunchPad)
        && it.tile.team() == player.team()){
        launch(Dispatchers.game){player.destoryMenu(it.tile.build as LaunchPadBuild)}
    }
}

 */
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    if((it.tile.block() is CoreBlock)
        && it.tile.team() == player.team()){
        launch(Dispatchers.game){player.spawnMenu(it.tile.build as CoreBuild)}
    }
}
listen<EventType.UnitDestroyEvent>{ u->
    if(u.unit.type == list[55]){
        UnitTypes.elude.create(u.unit.team()).apply{
            set(u.unit.x,u.unit.y)
            add()
        }
    }
}
listen<EventType.UnitDestroyEvent>{ u->
    if(u.unit.type == list[46]){
        Call.logicExplosion(u.unit.team(), u.unit.x, u.unit.y, 32f * 8f, 11000f, true, true, true)
        Call.effect(Fx.impactReactorExplosion,u.unit.x,u.unit.y, 1f, u.unit.team.color)
        Call.effect(Fx.reactorExplosion,(u.unit.x)+200f,(u.unit.y)+200f, 1f, u.unit.team.color)
        Call.effect(Fx.reactorExplosion,(u.unit.x)-200f,(u.unit.y)-200f, 1f, u.unit.team.color)
        Call.effect(Fx.reactorExplosion,(u.unit.x)-200f,(u.unit.y)+200f, 1f, u.unit.team.color)
        Call.effect(Fx.reactorExplosion,(u.unit.x)+200f,(u.unit.y)-200f, 1f, u.unit.team.color)
        Call.effect(Fx.reactorExplosion,u.unit.x,(u.unit.y)-300f, 1f, u.unit.team.color)
        Call.effect(Fx.reactorExplosion,u.unit.x,(u.unit.y)+300f, 1f, u.unit.team.color)
        Call.effect(Fx.reactorExplosion,(u.unit.x)-300f,u.unit.y, 1f, u.unit.team.color)
        Call.effect(Fx.reactorExplosion,(u.unit.x)+300f,u.unit.y, 1f, u.unit.team.color)
    }
}
onEnable{
    launch{
        loop(Dispatchers.game){
            Groups.unit.forEach { u->
                if(u.type == UnitTypes.conquer){
                    if(u.isShooting){
                        missile[0].create(u.team()).apply{
                            speedMultiplier(10f)
                            set(u.x,u.y)
                            rotation(u.angleTo(u.aimX(),u.aimY()))
                            add()
                            delay(100)
                        }
                    }
                }
            }
            delay(100)
        }
        delay(100)
    }
}
onEnable{
    launch{
        loop(Dispatchers.game){
            Groups.unit.forEach { u->
                if(u.type == UnitTypes.collaris){
                    if(u.isShooting){
                        missile[1].create(u.team()).apply{
                            speedMultiplier(10f)
                            set(u.x,u.y)
                            rotation(u.angleTo(u.aimX(),u.aimY()))
                            add()
                            delay(10000)
                        }
                    }
                }
            }
            delay(100)
        }
        delay(100)
    }
}
onEnable{
    launch {
        loop(Dispatchers.game) {
            Groups.build.forEach {
                Vars.fogControl.forceUpdate(Team.get(1), it)
            }
            Groups.build.forEach { build ->
                if ((build is CoreBuild)
                    && build.team() == Team.get(1)
                ) {
                    Pathfinder.PositionTarget(build)
                    Groups.unit.forEach {
                        Vars.controlPath to build.block()
                        val command: CommandAI = it.command()
                    }
                }
            }
            delay(2000)
        }
    }
}
onEnable {
    contextScript<ContentsTweaker>().addPatch(
        "pvp",
        dataDirectory.child("contents-patch").child("10.json").readString()
    )
}
onEnable{
    launch{
        var LastTime: Long = Time.millis()
        loop(Dispatchers.game) {
            delay(10)
            Groups.player.each { p ->
                var text = ""
                text += buildString {
                    val unit = p.unit() ?: return@buildString
                    appendLine("[red]再${200-(Time.millis() - LastTime)/1000}s后你可以获得一次部署机会最大一次")
                    appendLine("[red]测试中无限机会谢谢你klp相信抄你一点代码你没意见的")
                    if(Time.millis() - LastTime>=200){
                        Groups.player.forEach{p ->
                            p.setplayerchance(0)
                            LastTime = Time.millis()
                        }
                    }
                }
                Call.infoPopup(
                    p.con, text, 0.51f,
                    Align.topLeft, 350, 0, 0, 0
                )
            }
            delay(500)
        }
    }
}
/*
onEnable{
    launch{
        loop(Dispatchers.game){
            delay(200)
            for (b in Groups.build) {
                if(b.block() == Blocks.launchPad){
                    Groups.player.each { allplayer->
                        allplayer.launchpadmenu(b.x,b.y,b as LaunchPadBuild)
                        //BasicBulletType.createBullet(BulletType(8f,99f), Team.get(1),allplayer.x,allplayer.y,allplayer.angleTo(allplayer.mouseX,allplayer.mouseY),999f,8f,80f)
                    }
                }
            }
        }
    }
}

 */

