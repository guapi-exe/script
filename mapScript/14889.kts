@file:Depends("coreMindustry/utilNext", "调用菜单")
@file:Depends("coreMindustry/contentsTweaker", "修改核心单位,单位属性")
@file:Depends("coreMindustry/utilMapRule", "修改单位ai")
package mapScript

import arc.func.Func
import arc.graphics.Color
import arc.math.Mathf
import arc.struct.ObjectIntMap
import arc.util.Align
import arc.util.Time
import coreLibrary.lib.util.loop
import coreMindustry.ContentsTweaker
import coreMindustry.UtilNext
import coreMindustry.lib.game
import coreMindustry.lib.listen
import mindustry.Vars
import mindustry.Vars.dataDirectory
import mindustry.ai.Pathfinder
import mindustry.ai.types.GroundAI
import mindustry.ai.types.MissileAI
import mindustry.content.*
import mindustry.ctype.ContentType
import mindustry.entities.Predict
import mindustry.entities.Units
import mindustry.entities.units.UnitController
import mindustry.game.EventType
import mindustry.game.Team
import mindustry.gen.*
import mindustry.type.UnitType
import mindustry.world.Block
import mindustry.world.blocks.campaign.LaunchPad
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.blocks.production.Drill
import mindustry.world.blocks.production.Drill.DrillBuild
import mindustry.world.blocks.storage.CoreBlock
import mindustry.world.blocks.storage.CoreBlock.CoreBuild
import wayzer.Maps

val teammoney: ObjectIntMap<Team> = ObjectIntMap()//队伍货币
val teamunitgrade: ObjectIntMap<Team> = ObjectIntMap()//队伍进攻单位等级
val teamdrillgrade: ObjectIntMap<Team> = ObjectIntMap()//队伍进攻单位等级
val teamunitarmor: ObjectIntMap<Team> = ObjectIntMap()//队伍进攻单位护甲
val teamunitnum: ObjectIntMap<Team> = ObjectIntMap()//队伍进攻单位部队数量
val teamturret: ObjectIntMap<Team> = ObjectIntMap()//队伍武器伤害
val teamlifes: ObjectIntMap<Team> = ObjectIntMap()//核心生命
val overdbuffer: ObjectIntMap<Team> = ObjectIntMap()//buffer
val overcbuffer: ObjectIntMap<Team> = ObjectIntMap()//buffer
val bossbuffer: ObjectIntMap<Team> = ObjectIntMap()//buffer
val shieldbuffer: ObjectIntMap<Team> = ObjectIntMap()//buffer
val teamunitline: ObjectIntMap<String> = ObjectIntMap()//
val teamOppressive: ObjectIntMap<Team> = ObjectIntMap()//团队压迫力
fun addoverdbuffer(team: Team){
    return overdbuffer.put(team,overdbuffer.get(team) + 1)
}
fun addovercbuffer(team: Team){
    return overcbuffer.put(team,overcbuffer.get(team) + 1)
}
fun addbossbuffer(team: Team){
    return bossbuffer.put(team,bossbuffer.get(team) + 1)
}
fun addshieldbuffer(team: Team){
    return shieldbuffer.put(team,shieldbuffer.get(team) + 1)
}
fun Building.addteammoneybuild(amount: Int){
    return teammoney.put(team(),teammoney.get(team()) + amount)
}
fun setteammoney(team: Team,amount: Int){
    return teammoney.put(team,teammoney.get(team) - teammoney.get(team) + amount)
}
fun Player.addteamunitline(unitType: UnitType,amount: Int){
    val teamunit = team().toString() + unitType.toString()
    return teamunitline.put(teamunit,teamunitline.get(teamunit) + amount)
}
fun setteamunitline(team: Team,unitType: UnitType,amount: Int){
    val teamunit = team.toString() + unitType.toString()
    return teamunitline.put(teamunit,teamunitline.get(teamunit) - teamunitline.get(teamunit) + amount)
}
fun Player.addteammoneyplayer(amount: Int){
    return teammoney.put(team(),teammoney.get(team()) + amount)
}
fun Player.addteamdrillgrade(amount: Int){
    return teamdrillgrade.put(team(),teamdrillgrade.get(team()) + amount)
}
fun Player.getteamdrillgrade(): Int{
    return teamdrillgrade.get(team())
}
fun addteammoney(team: Team,amount: Int){
    return teammoney.put(team,teammoney.get(team) + amount)
}
fun Player.getteammoney():Int {
    return teammoney.get(team())
}
fun getteamlifes(team: Team):Int {
    return teamlifes.get(team)
}
fun addteamlifes(team: Team,amount: Int) {
    return teamlifes.put(team,teamlifes.get(team) + amount)
}
fun Player.getteamunitline(unitType: UnitType):Int {
    val teamunit = team().toString() + unitType.toString()
    return teamunitline.get(teamunit)
}
fun getteamunitlinenew(team:Team , unitType: UnitType):Int {
    val teamunit = team.toString() + unitType.toString()
    return teamunitline.get(teamunit)
}
fun Player.getteamunitgrade():Int {
    return teamunitgrade.get(team())
}
fun Player.getteamunitarmor():Int {
    return teamunitarmor.get(team())
}
fun getteamunitarmornew(team: Team):Int {
    return teamunitarmor.get(team)
}
fun Player.getteamunitnum():Int {
    return teamunitnum.get(team())
}
fun getteamunitnumnew(team: Team):Int {
    return teamunitnum.get(team)
}
fun Player.getteamturret():Int {
    return teamturret.get(team())
}
fun Player.removeteammoney(amount: Int){
    return teammoney.put(team(),teammoney.get(team()) - amount)
}
fun teamremoveteammoney(team: Team,amount: Int){
    return teammoney.put(team,teammoney.get(team) - amount)
}
fun Player.addteamunitgrade(amount: Int){
    return teamunitgrade.put(team(),teamunitgrade.get(team()) + amount)
}
fun setteamunitgrade(team: Team,amount: Int){
    return teamunitgrade.put(team,teamunitgrade.get(team) - teamunitgrade.get(team) + amount)
}
fun Player.addteamunitarmor(amount: Int){
    return teamunitarmor.put(team(),teamunitarmor.get(team()) + amount)
}
fun setteamunitarmor(team: Team,amount: Int){
    return teamunitarmor.put(team,teamunitarmor.get(team) - teamunitarmor.get(team) + amount)
}
fun Player.addteamunitnum(amount: Int){
    return teamunitnum.put(team(),teamunitnum.get(team()) + amount)
}
fun setteamunitnum(team: Team,amount: Int){
    return teamunitnum.put(team,teamunitnum.get(team) - teamunitnum.get(team) + amount)
}
fun Player.addteamturret(amount: Int){
    return teamturret.put(team(),teamturret.get(team()) + amount)
}
fun setteamturret(team: Team,amount: Int){
    return teamturret.put(team,teamturret.get(team) - teamturret.get(team) + amount)
}
fun Building.removeteamlifes(amount: Int){
    return teamlifes.put(team(),teamlifes.get(team()) - amount)
}
fun Player.removeteamunitline(unitType: UnitType,amount: Int){
    val teamunit = team().toString() + unitType.toString()
    return teamunitline.put(teamunit,teamunitline.get(teamunit) - amount)
}
val list = Vars.content.getBy<UnitType>(ContentType.unit).filterNot { it.internal }
//导弹单位
val missile = arrayOf(
    list[63],
    UnitTypes.anthicus.weapons.get(0).bullet.spawnUnit,
    UnitTypes.quell.weapons.get(0).bullet.spawnUnit,
    UnitTypes.disrupt.weapons.get(0).bullet.spawnUnit,
    UnitTypes.elude,
)
//超级单位
val superunits = arrayOf(
    UnitTypes.disrupt,
    UnitTypes.conquer,
    UnitTypes.navanax,
    UnitTypes.eclipse,
    UnitTypes.toxopid,
    UnitTypes.reign
)
//allsttackunits
val allattackunits = arrayOf(
    UnitTypes.stell,
    UnitTypes.flare,
    UnitTypes.nova,
    UnitTypes.crawler,
    UnitTypes.locus,
    UnitTypes.horizon,
    UnitTypes.pulsar,
    UnitTypes.atrax,
    UnitTypes.precept,
    UnitTypes.zenith,
    UnitTypes.quasar,
    UnitTypes.spiroct,
    UnitTypes.vanquish,
    UnitTypes.quad,
    UnitTypes.vela,
    UnitTypes.arkyid,
    UnitTypes.oct
)
//100,20
val T1attackunits = arrayOf(
    UnitTypes.stell,
    UnitTypes.flare,
    UnitTypes.nova,
    UnitTypes.crawler
)
//1000,200
val T2attackunits = arrayOf(
    UnitTypes.locus,
    UnitTypes.horizon,
    UnitTypes.pulsar,
    UnitTypes.atrax
)
//3000,600
val T3attackunits = arrayOf(
    UnitTypes.precept,
    UnitTypes.zenith,
    UnitTypes.quasar,
    UnitTypes.spiroct
)
//10000,2000
val T4attackunits = arrayOf(
    UnitTypes.vanquish,
    UnitTypes.quad,
    UnitTypes.vela,
    UnitTypes.arkyid
)
//30000,6000
val T5attackunits = arrayOf(
    UnitTypes.oct
)
//drills
val Drills = arrayOf(
    Blocks.mechanicalDrill,
    Blocks.pneumaticDrill,
    Blocks.laserDrill,
    Blocks.impactDrill,
    Blocks.blastDrill,
    Blocks.eruptionDrill
)
val turrets = arrayOf(
    Blocks.salvo,
    Blocks.lancer,
    Blocks.scatter,
    Blocks.diffuse,
    Blocks.tsunami,
    Blocks.ripple,
    Blocks.lustre,
    Blocks.sublimate,
    Blocks.titan,
    Blocks.spectre,
    Blocks.disperse,
    Blocks.afflict,
    Blocks.foreshadow,
    Blocks.regenProjector,
    Blocks.meltdown,
    Blocks.smite,
    Blocks.malign,
    Blocks.overdriveDome,
    Blocks.shockwaveTower,
    Blocks.cryofluidMixer,
    Blocks.neoplasiaReactor
)
val menu = contextScript<UtilNext>()

//炮塔提示ui
fun Player.launchpadmenu(x :Float, y :Float, task: LaunchPad.LaunchPadBuild){
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        while(true){
            delay(200)
            if (Time.millis() - dorpTime >= 1_000) break
            if(unit().within(task.x,task.y,300f)) {
                Call.label(
                    con ?: return@launch, "[green]点我放置炮塔", 0.21f,
                    dropX, dropY
                )
            }
        }
    }
}
fun Player.buildmenu(b:Building, amount:Int){
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        while(true){
            delay(200)
            if (Time.millis() - dorpTime >= 4_000) break
            if(unit().within(b.x,b.y,300f)) {
                Call.label(
                    con ?: return@launch, "[green]放置${b.block.emoji()}花费$amount", 0.21f,
                    b.x,b.y - 10f
                )
            }
        }
    }
}
fun Player.healmenu(x :Float, y :Float, team:Team){
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        while(true){
            delay(200)
            if (Time.millis() - dorpTime >= 80_000) break
            val str :String = String.format("%.2f",((80*1000 - (Time.millis() - dorpTime))/1000).toFloat())
            Call.label(
                con ?: return@launch, "[red]等待炮塔位点重生${str}/s", 0.21f,
                dropX, dropY
            )
        }
        var tile = Vars.world.tiles.getn(
            x.toInt() / 8,
            y.toInt() / 8
        )
        tile.setNet(Blocks.launchPad,team,0)
    }
}
fun Player.callmenu(x :Float, y :Float){
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        while(true){
            delay(200)
            if (Time.millis() - dorpTime >= 50_000) break
            val str :String = String.format("%.2f",((50*1000 - (Time.millis() - dorpTime))/1000).toFloat())
            Call.label(
                con ?: return@launch, "[red]距离下次召唤${str}/s", 0.21f,
                dropX, dropY
            )
        }
    }
}
fun Player.healcoremenu(x :Float, y :Float, team:Team){
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        while(true){
            delay(200)
            if (Time.millis() - dorpTime >= 100_000) break
            val str :String = String.format("%.2f",((100*1000 - (Time.millis() - dorpTime))/1000).toFloat())
            Call.label(
                con ?: return@launch, "[red]等待核心重生${str}/s", 0.21f,
                dropX, dropY
            )
        }
        var tile = Vars.world.tiles.getn(
            x.toInt() / 8,
            y.toInt() / 8
        )
        tile.setNet(Blocks.coreCitadel,team,0)
    }
}
fun Player.dropmenu(x :Float, y :Float, amount: Int){
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        while(true){
            delay(200)
            if (Time.millis() - dorpTime >= 3_000) break
            Call.label(
                con ?: return@launch, "${amount}", 0.21f,
                dropX, dropY
            )
        }
    }
}
//钻头提示ui
fun Player.drillsmenu(x :Float, y :Float, task: DrillBuild){
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        while(true){
            delay(200)
            if (Time.millis() - dorpTime >= 1_000) break
            if(unit().within(task.x,task.y,300f)) {
                if(task.block() == Blocks.mechanicalDrill && getteamdrillgrade() >= 1) {
                    Call.label(
                        con ?: return@launch, "${Blocks.mechanicalDrill.emoji()}\n${3*teamdrillgrade.get(team())}/2s", 0.21f,
                        dropX, dropY
                    )
                }else if(task.block() == Blocks.pneumaticDrill && getteamdrillgrade() >= 2) {
                    Call.label(
                        con ?: return@launch, "${Blocks.pneumaticDrill.emoji()}\n${8*teamdrillgrade.get(team())}/2s", 0.21f,
                        dropX, dropY
                    )
                }else if(task.block() == Blocks.laserDrill && getteamdrillgrade() >= 3){
                    Call.label(
                        con ?: return@launch, "${Blocks.laserDrill.emoji()}\n${30*teamdrillgrade.get(team())}/2s", 0.21f,
                        dropX, dropY
                    )
                }else if(task.block() == Blocks.impactDrill && getteamdrillgrade() >= 4){
                    Call.label(
                        con ?: return@launch, "${Blocks.impactDrill.emoji()}\n${50*teamdrillgrade.get(team())}/2s", 0.21f,
                        dropX, dropY
                    )
                }else if(task.block() == Blocks.blastDrill&& getteamdrillgrade() >=5){
                    Call.label(
                        con ?: return@launch, "${Blocks.blastDrill.emoji()}\n${70*teamdrillgrade.get(team())}/2s", 0.21f,
                        dropX, dropY
                    )
                }else{
                    if(getteamdrillgrade() >= 6 ) {
                        Call.label(
                            con ?: return@launch, "${Blocks.eruptionDrill.emoji()}\n${140 * teamdrillgrade.get(team())}/2s", 0.21f,
                            dropX, dropY
                        )
                    }
                }
            }
        }
    }
}
//炮塔放置ui
suspend fun Player.placeMenu(Launchpad: LaunchPad.LaunchPadBuild) {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            炮塔放置界面
        """.trimIndent()
    ) {
        //T1
        this += listOf(
            "${Blocks.salvo.emoji()}\n500￥" to {
                if(getteammoney()>=500) {
                    removeteammoney(500)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    val turretx = Launchpad.x.toInt() / 8
                    val turrety = Launchpad.y.toInt() / 8
                    tile.setNet(Blocks.salvo, team(), 0)
                    val size = (tile.build.hitSize() / 8).toInt()
                    var tile1 = Vars.world.tiles.getn(
                        turretx + size,
                        turrety
                    )
                    tile1.setNet(Blocks.itemSource, team(), 0)
                    tile1.build.apply { configure(Items.thorium) }
                    delay(100)
                    tile1.setNet(Blocks.air, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金500￥放置了炮塔${Blocks.salvo.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.salvo.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.lancer.emoji()}\n500￥" to {
                if(getteammoney()>=500) {
                    removeteammoney(500)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    tile.setNet(Blocks.lancer, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金500￥放置了炮塔${Blocks.lancer.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.lancer.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.scatter.emoji()}\n500￥" to {
                if(getteammoney()>=500) {
                    removeteammoney(500)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    val turretx = Launchpad.x.toInt() / 8
                    val turrety = Launchpad.y.toInt() / 8
                    tile.setNet(Blocks.scatter, team(), 0)
                    val size = (tile.build.hitSize() / 8).toInt()
                    var tile1 = Vars.world.tiles.getn(
                        turretx + size,
                        turrety
                    )
                    tile1.setNet(Blocks.itemSource, team(), 0)
                    tile1.build.apply { configure(Items.metaglass) }
                    delay(100)
                    tile1.setNet(Blocks.air, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金500￥放置了炮塔${Blocks.scatter.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.scatter.emoji()}但队伍资金不足")
                        }
                    }
                }
            }
        )
        //T2
        this += listOf(
            "${Blocks.diffuse.emoji()}\n1000￥" to {
                if(getteammoney()>=1000) {
                    removeteammoney(1000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    val turretx = Launchpad.x.toInt() / 8
                    val turrety = Launchpad.y.toInt() / 8
                    tile.setNet(Blocks.diffuse, team(), 0)
                    val size = (tile.build.hitSize() / 8).toInt()
                    var tile1 = Vars.world.tiles.getn(
                        turretx + size - 1,
                        turrety
                    )
                    tile1.setNet(Blocks.itemSource, team(), 0)
                    tile1.build.apply { configure(Items.graphite) }
                    delay(100)
                    tile1.setNet(Blocks.air, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金1000￥放置了炮塔${Blocks.diffuse.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.diffuse.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.tsunami.emoji()}\n1000￥" to {
                if(getteammoney()>=1000) {
                    removeteammoney(1000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    val turretx = Launchpad.x.toInt() / 8
                    val turrety = Launchpad.y.toInt() / 8
                    tile.setNet(Blocks.tsunami, team(), 0)
                    val size = (tile.build.hitSize() / 8).toInt()
                    var tile1 = Vars.world.tiles.getn(
                        turretx + size - 1,
                        turrety
                    )
                    tile1.setNet(Blocks.liquidSource, team(), 0)
                    tile1.build.apply { configure(Liquids.cryofluid) }
                    delay(100)
                    tile1.setNet(Blocks.air, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金1000￥放置了炮塔${Blocks.tsunami.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.tsunami.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.ripple.emoji()}\n1000￥" to {
                if(getteammoney()>=1000) {
                    removeteammoney(1000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    val turretx = Launchpad.x.toInt() / 8
                    val turrety = Launchpad.y.toInt() / 8
                    tile.setNet(Blocks.ripple, team(), 0)
                    val size = (tile.build.hitSize() / 8).toInt()
                    var tile1 = Vars.world.tiles.getn(
                        turretx + size - 1,
                        turrety
                    )
                    tile1.setNet(Blocks.itemSource, team(), 0)
                    tile1.build.apply { configure(Items.plastanium) }
                    delay(100)
                    tile1.setNet(Blocks.air, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金1000￥放置了炮塔${Blocks.ripple.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.ripple.emoji()}但队伍资金不足")
                        }
                    }
                }
            }
        )
        //T3
        this += listOf(
            "${Blocks.lustre.emoji()}\n3000￥" to {
                if(getteammoney()>=3000) {
                    removeteammoney(3000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    tile.setNet(Blocks.lustre, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金3000￥放置了炮塔${Blocks.lustre.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.lustre.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.sublimate.emoji()}\n3000￥" to {
                if(getteammoney()>=3000) {
                    removeteammoney(3000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    val turretx = Launchpad.x.toInt() / 8
                    val turrety = Launchpad.y.toInt() / 8
                    tile.setNet(Blocks.sublimate, team(), 0)
                    val size = (tile.build.hitSize() / 8).toInt()
                    var tile1 = Vars.world.tiles.getn(
                        turretx + size - 1,
                        turrety
                    )
                    tile1.setNet(Blocks.liquidSource, team(), 0)
                    tile1.build.apply { configure(Liquids.ozone) }
                    delay(100)
                    tile1.setNet(Blocks.air, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金3000￥放置了炮塔${Blocks.sublimate.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.sublimate.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.titan.emoji()}\n3000￥" to {
                if(getteammoney()>=3000) {
                    removeteammoney(3000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    val turretx = Launchpad.x.toInt() / 8
                    val turrety = Launchpad.y.toInt() / 8
                    tile.setNet(Blocks.titan, team(), 0)
                    val size = (tile.build.hitSize() / 8).toInt()
                    var tile1 = Vars.world.tiles.getn(
                        turretx + size - 1,
                        turrety
                    )
                    tile1.setNet(Blocks.itemSource, team(), 0)
                    tile1.build.apply { configure(Items.thorium) }
                    delay(100)
                    tile1.setNet(Blocks.air, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金3000￥放置了炮塔${Blocks.titan.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.titan.emoji()}但队伍资金不足")
                        }
                    }
                }
            }
        )
        //T4
        this += listOf(
            "${Blocks.spectre.emoji()}\n6000￥" to {
                if(getteammoney()>=6000) {
                    removeteammoney(6000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    val turretx = Launchpad.x.toInt() / 8
                    val turrety = Launchpad.y.toInt() / 8
                    tile.setNet(Blocks.spectre, team(), 0)
                    val size = (tile.build.hitSize() / 8).toInt()
                    var tile1 = Vars.world.tiles.getn(
                        turretx + size - 1,
                        turrety
                    )
                    tile1.setNet(Blocks.itemSource, team(), 0)
                    tile1.build.apply { configure(Items.thorium) }
                    delay(100)
                    tile1.setNet(Blocks.air, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金6000￥放置了炮塔${Blocks.spectre.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.spectre.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.disperse.emoji()}\n6000￥" to {
                if(getteammoney()>=6000) {
                    removeteammoney(6000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    val turretx = Launchpad.x.toInt() / 8
                    val turrety = Launchpad.y.toInt() / 8
                    tile.setNet(Blocks.disperse, team(), 0)
                    val size = (tile.build.hitSize() / 8).toInt()
                    var tile1 = Vars.world.tiles.getn(
                        turretx + size - 1,
                        turrety
                    )
                    tile1.setNet(Blocks.itemSource, team(), 0)
                    tile1.build.apply { configure(Items.tungsten) }
                    delay(100)
                    tile1.setNet(Blocks.air, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金6000￥放置了炮塔${Blocks.disperse.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.disperse.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.afflict.emoji()}\n6000￥" to {
                if(getteammoney()>=6000) {
                    removeteammoney(6000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    tile.setNet(Blocks.afflict, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金6000￥放置了炮塔${Blocks.afflict.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.afflict.emoji()}但队伍资金不足")
                        }
                    }
                }
            }
        )
        //T5
        this += listOf(
            "${Blocks.foreshadow.emoji()}\n20000￥" to {
                if(getteammoney()>=20000) {
                    removeteammoney(20000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    val turretx = Launchpad.x.toInt() / 8
                    val turrety = Launchpad.y.toInt() / 8
                    tile.setNet(Blocks.foreshadow, team(), 0)
                    val size = (tile.build.hitSize() / 8).toInt()
                    var tile1 = Vars.world.tiles.getn(
                        turretx + size - 1,
                        turrety
                    )
                    tile1.setNet(Blocks.itemSource, team(), 0)
                    tile1.build.apply { configure(Items.surgeAlloy) }
                    delay(100)
                    tile1.setNet(Blocks.air, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金20000￥放置了炮塔${Blocks.foreshadow.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.foreshadow.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.regenProjector.emoji()}\n20000￥" to {
                if(getteammoney()>=20000) {
                    removeteammoney(20000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    tile.setNet(Blocks.regenProjector, team(), 0)
                    delay(100)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金20000￥放置了炮塔${Blocks.regenProjector.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.regenProjector.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.meltdown.emoji()}\n20000￥" to {
                if(getteammoney()>=20000) {
                    removeteammoney(20000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    tile.setNet(Blocks.meltdown, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金20000￥放置了炮塔${Blocks.meltdown.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.meltdown.emoji()}但队伍资金不足")
                        }
                    }
                }
            }
        )
        //T6
        this += listOf(
            "${Blocks.smite.emoji()}\n80000￥" to {
                if(getteammoney()>=80000) {
                    removeteammoney(80000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    val turretx = Launchpad.x.toInt() / 8
                    val turrety = Launchpad.y.toInt() / 8
                    tile.setNet(Blocks.smite, team(), 0)
                    val size = (tile.build.hitSize() / 8).toInt()
                    var tile1 = Vars.world.tiles.getn(
                        turretx + size - 2,
                        turrety
                    )
                    tile1.setNet(Blocks.itemSource, team(), 0)
                    tile1.build.apply { configure(Items.surgeAlloy) }
                    delay(100)
                    tile1.setNet(Blocks.air, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金80000￥放置了炮塔${Blocks.smite.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.smite.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.malign.emoji()}\n100000￥" to {
                if(getteammoney()>=100000) {
                    removeteammoney(100000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    tile.setNet(Blocks.malign, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金100000￥放置了炮塔${Blocks.malign.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.malign.emoji()}但队伍资金不足")
                        }
                    }
                }
            }
        )
        //others
        this += listOf(
            "${Blocks.overdriveDome.emoji()}\n100000￥" to {
                if(getteammoney()>=100000) {
                    removeteammoney(100000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    tile.setNet(Blocks.overdriveDome, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金100000￥放置了炮塔${Blocks.overdriveDome.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.overdriveDome.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.shockwaveTower.emoji()}\n100000￥" to {
                if(getteammoney()>=100000) {
                    removeteammoney(100000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    tile.setNet(Blocks.shockwaveTower, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金100000￥放置了炮塔${Blocks.shockwaveTower.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.shockwaveTower.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.cryofluidMixer.emoji()}\n100000￥" to {
                if(getteammoney()>=100000) {
                    removeteammoney(100000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    tile.setNet(Blocks.cryofluidMixer, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金100000￥放置了炮塔${Blocks.cryofluidMixer.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.cryofluidMixer.emoji()}但队伍资金不足")
                        }
                    }
                }
            },"${Blocks.neoplasiaReactor.emoji()}\n100000￥" to {
                if(getteammoney()>=100000) {
                    removeteammoney(100000)
                    var tile = Vars.world.tiles.getn(
                        Launchpad.x.toInt() / 8,
                        Launchpad.y.toInt() / 8
                    )
                    tile.setNet(Blocks.neoplasiaReactor, team(), 0)
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}消费队伍资金100000￥放置了炮塔${Blocks.neoplasiaReactor.emoji()}")
                        }
                    }
                }else{
                    Groups.player.each { p->
                        if(p.team() == team()){
                            p.sendMessage("玩家${name()}想要放置炮塔${Blocks.neoplasiaReactor.emoji()}但队伍资金不足")
                        }
                    }
                }
            }
        )
    }
}
//钻头升级ui
//炮塔拆除ui
suspend fun Player.destoryMenu(turret: Building) {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            如果该炮塔你不再需要你可以拆除它并且你将会获得1￥的巨额补偿
        """.trimIndent()
    ){
        this += listOf(
            "[green]拆除" to {
                var tile = Vars.world.tiles.getn(
                    turret.x.toInt() / 8,
                    turret.y.toInt() / 8
                )
                tile.setNet(Blocks.air)
                tile.setNet(Blocks.launchPad,team(),0)
                addteammoneyplayer(1)
                for (player in Groups.player) {
                    if(team() == player.team()){
                        player.sendMessage("玩家${name()}拆除了炮塔${turret.block.emoji()}")
                    }
                }
            },"[red]否" to {
            }
        )
    }
}
suspend fun Player.SureMenu(unitType: UnitType) {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            ${getteamunitline(unitType)}
            ${unitType.emoji()}
            再这里购买一条单位产线或者移除
            移除将只返回原价的1/2
        """.trimIndent()
    ){
        if(getteamunitline(unitType) <= 15) {
            this += listOf(
                "[green]添加" to {
                    addteamunitline(unitType, 1)
                    when (unitType) {
                        in T1attackunits -> removeteammoney(200)
                        in T2attackunits -> removeteammoney(1000)
                        in T3attackunits -> removeteammoney(3000)
                        in T4attackunits -> removeteammoney(10000)
                        in T5attackunits -> removeteammoney(30000)
                    }
                    SureMenu(unitType)
                }, "[green]移除" to {
                    if (getteamunitline(unitType) > 0) {
                        removeteamunitline(unitType, 1)
                        when (unitType) {
                            in T1attackunits -> addteammoney(team(), 200 / 2)
                            in T2attackunits -> addteammoney(team(), 1000 / 2)
                            in T3attackunits -> addteammoney(team(), 3000 / 2)
                            in T4attackunits -> addteammoney(team(), 10000 / 2)
                            in T5attackunits -> addteammoney(team(), 30000 / 2)
                        }
                        SureMenu(unitType)
                    } else {
                        sendMessage("你没有产线可以移除")
                    }
                }
            )
        }else{
            this += listOf(
                "[red]该单位产线超过定额" to {
                    SureMenu(unitType)
                }
            )
        }
    }
}
suspend fun Player.UnitshopMenu() {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]每种单位都拥有自己的优势",
        """
            [red]单位
            [green]购买
            [yellow]维护费用
            再这里购买一条单位产线或者移除
            请先升级你的进攻单位等级不然你无法购买单位
            每个单位产线都需要维护费
        """.trimIndent()
    ){
        if(getteamunitgrade() == 0){
            this += listOf(
                "你的团队单位等级没法购买任何单位" to {
                    UnitgradeMenu()
                }
            )
        }
        if(getteamunitgrade() >= 1) {
            this += listOf(
                "${T1attackunits[0].emoji()}\n200\n20" to {
                    SureMenu(T1attackunits[0])
                },
                "${T1attackunits[1].emoji()}\n200\n20" to {
                    SureMenu(T1attackunits[1])
                },
                "${T1attackunits[2].emoji()}\n200\n20" to {
                    SureMenu(T1attackunits[2])
                },
                "${T1attackunits[3].emoji()}\n200\n20" to {
                    SureMenu(T1attackunits[3])
                }
            )
        }
        if(getteamunitgrade() >= 2) {
            this += listOf(
                "${T2attackunits[0].emoji()}\n1000\n200" to {
                    SureMenu(T2attackunits[0])
                },
                "${T2attackunits[1].emoji()}\n1000\n200" to {
                    SureMenu(T2attackunits[1])
                },
                "${T2attackunits[2].emoji()}\n1000\n200" to {
                    SureMenu(T2attackunits[2])
                },
                "${T2attackunits[3].emoji()}\n1000\n200" to {
                    SureMenu(T2attackunits[3])
                }
            )
        }
        if(getteamunitgrade() >= 3) {
            this += listOf(
                "${T3attackunits[0].emoji()}\n3000\n600" to {
                    SureMenu(T3attackunits[0])
                },
                "${T3attackunits[1].emoji()}\n3000\n600" to {
                    SureMenu(T3attackunits[1])
                },
                "${T3attackunits[2].emoji()}\n3000\n600" to {
                    SureMenu(T3attackunits[2])
                },
                "${T3attackunits[3].emoji()}\n3000\n600" to {
                    SureMenu(T3attackunits[3])
                }
            )
        }
        if(getteamunitgrade() >= 4) {
            this += listOf(
                "${T4attackunits[0].emoji()}\n1w\n2000" to {
                    SureMenu(T4attackunits[0])
                },
                "${T4attackunits[1].emoji()}\n1w\n2000" to {
                    SureMenu(T4attackunits[1])
                },
                "${T4attackunits[2].emoji()}\n1w\n2000" to {
                    SureMenu(T4attackunits[2])
                },
                "${T4attackunits[3].emoji()}\n1w\n2000" to {
                    SureMenu(T4attackunits[3])
                }
            )
        }
        if(getteamunitgrade() >= 5) {
            this += listOf(
                "${UnitTypes.oct.emoji()}\n3w\n6000" to {
                    SureMenu(UnitTypes.oct)
                }
            )
        }
    }
}
suspend fun Player.UnitgradeMenu() {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            再这里你可以升级你的综合属性
            进攻部队数量将会对你方单位产线生产量成倍
        """.trimIndent()
    ){
        if(getteamunitgrade() < 5) {
            this += listOf(
                "[green]升级当前:${getteamunitgrade()}->${getteamunitgrade()+1}级\n${Math.pow((2000 * getteamunitgrade()).toDouble(), 1.2).toInt()}" to {
                    if (getteammoney() >= Math.pow((2000 * getteamunitgrade()).toDouble(), 1.2)) {
                        removeteammoney(Math.pow((2000 * getteamunitgrade()).toDouble(), 1.2).toInt())
                        addteamunitgrade(1)
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                player.sendMessage("玩家${name()}购买了进攻单位等级当前:${getteamunitgrade()}级")
                            }
                        }
                        UnitgradeMenu()
                    } else {
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                player.sendMessage("玩家${name()}想要购买了进攻单位等级但队伍货币不足")
                            }
                        }
                        UnitgradeMenu()
                    }
                }
            )
        }else{
            this += listOf(
                "等级已满无需购买" to {
                    UnitgradeMenu()
                }
            )
        }
        this += listOf(
            "[red]升级进攻单位护甲当前${getteamunitarmor()}->${getteamunitarmor()+5}\n${((getteamunitarmor()+5)*500)*((getteamunitarmor()+5)/4)}" to {
                if(getteammoney() > ((getteamunitarmor()+5)*500)*((getteamunitarmor()+5)/4)) {
                    removeteammoney(((getteamunitarmor()+5)*500)*((getteamunitarmor()+5)/4))
                    addteamunitarmor(5)
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            player.sendMessage("玩家${name()}购买了进攻单位护甲当前:${getteamunitarmor()}")
                        }
                    }
                    UnitgradeMenu()
                }else {
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            player.sendMessage("玩家${name()}想要购买了进攻单位护甲但队伍货币不足")
                        }
                    }
                    UnitgradeMenu()
                }
            }
        )
        this += listOf(
            "[yellow]升级进攻单位部队数量当前${getteamunitnum()}->${getteamunitnum()+1}\n${((getteamunitnum())*50000)}" to {
                if(getteammoney() > ((getteamunitnum())*50000)) {
                    removeteammoney(((getteamunitnum())*50000))
                    addteamunitnum(1)
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            player.sendMessage("玩家${name()}购买了进攻单位部队数量当前:${getteamunitnum()}")
                        }
                    }
                    UnitgradeMenu()
                }else {
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            player.sendMessage("玩家${name()}想要购买了进攻单位部队数量但队伍货币不足")
                        }
                    }
                    UnitgradeMenu()
                }
            }
        )
        this += listOf(
            "[green]升级炮塔伤害当前${String.format("%.2f",team().rules().blockDamageMultiplier)}->${String.format("%.2f",team().rules().blockDamageMultiplier + 0.05f)}\n${((getteamturret()+1)*1000)*(getteamturret()/4+1)}" to {
                if(getteammoney() > ((getteamturret()+1)*1000)*(getteamturret()/4+1)) {
                    removeteammoney(((getteamturret()+1)*1000)*(getteamturret()/4+1))
                    addteamturret(1)
                    team().rules().blockDamageMultiplier += 0.05f
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            player.sendMessage("玩家${name()}购买了炮塔伤害当前:${1+getteamturret()*0.05}")
                        }
                    }
                    UnitgradeMenu()
                }else {
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            player.sendMessage("玩家${name()}想要购买了炮塔伤害但队伍货币不足")
                        }
                    }
                    UnitgradeMenu()
                }
            }
        )
        this += listOf(
            "[red]单位产线购买" to {
                UnitshopMenu()
            }
        )
        this += listOf(
            "[green]购买队伍建筑生命${String.format("%.2f",team().rules().blockHealthMultiplier)}->${String.format("%.2f",team().rules().blockHealthMultiplier+0.1f)}\n20000" to {
                if(getteammoney() > 20000) {
                    removeteammoney(20000)
                    team().rules().blockHealthMultiplier += 0.1f
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            player.sendMessage("玩家${name()}购买了队伍建筑生命当前:${team().rules().blockHealthMultiplier+0.1}")
                        }
                    }
                    UnitgradeMenu()
                }else {
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            player.sendMessage("玩家${name()}想要购买队伍建筑生命但队伍货币不足")
                        }
                    }
                    UnitgradeMenu()
                }
            }
        )
        this += listOf(
            "[yellow]为进攻单位购买buff" to {
                buffMenu()
            }
        )
        this += listOf(
            "[red]抽取超级防御单位\n100000" to {
                superMenu()
            }
        )
        if(getteamdrillgrade() <6) {
            this += listOf(
                "[green]升级我们对钻头的认知度当前:${getteamdrillgrade()}->${getteamdrillgrade()+1}级\n${Math.pow((5000 * getteamdrillgrade()).toDouble(), 1.2).toInt()}" to {
                    if (getteammoney() >= Math.pow((5000 * getteamdrillgrade()).toDouble(), 1.2)) {
                        removeteammoney(Math.pow((5000 * getteamdrillgrade()).toDouble(), 1.2).toInt())
                        addteamdrillgrade(1)
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                player.sendMessage("你们对钻头的认知度加深了等级当前:${getteamdrillgrade()}级")
                            }
                        }
                        UnitgradeMenu()
                    } else {
                        sendMessage("团队资金不足")
                        UnitgradeMenu()
                    }
                }
            )
        }else{
            this += listOf(
                "等级已满无需购买" to {
                    UnitgradeMenu()
                }
            )
        }
    }
}
suspend fun Player.superMenu() {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            再这里你可以抽取超级单位
        """.trimIndent()
    ){
        this += listOf(
            "[red]抽取超级单位\n100000" to {
                if(getteammoney() >= 100000){
                    removeteammoney(100000)
                    for(Spawn in Groups.build) {
                        if(Spawn.block() == Blocks.basicAssemblerModule && Spawn.team() == team()) {
                            val unitType = superunits.random()
                            val random = Math.random() * 360f
                            unitType.create(team()).apply {
                                armor(getteamunitarmornew(team).toFloat())
                                set(
                                    ((Spawn.x / 8 + (10f * Math.sin(random))) * 8).toFloat(),
                                    ((Spawn.y / 8 + (10f * Math.cos(random))) * 8).toFloat()
                                )
                                add()
                            }
                            for(allplayer in Groups.player){
                                allplayer.sendMessage("${team()}成功生成了一个超级单位")
                            }
                            break
                        }
                    }
                }else{
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            player.sendMessage("玩家${name()}想要购买超级单位但队伍无法支撑")
                        }
                    }
                }
            }
        )
    }
}
suspend fun Player.buffMenu() {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            再这里为你的进攻部队添加buff
        """.trimIndent()
    ){
        if(overdbuffer.get(team()) == 0) {
            this += listOf(
                "${StatusEffects.overdrive.emoji()}\n20000" to {
                    if (getteammoney() >= 20000) {
                        removeteammoney(20000)
                        addoverdbuffer(team())
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                player.sendMessage("玩家${name()}购买单位buff${StatusEffects.overdrive.emoji()}")
                            }
                        }
                        buffMenu()
                    } else {
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                player.sendMessage("玩家${name()}想要购买buff${StatusEffects.overdrive.emoji()}但队伍货币不足")
                            }
                        }
                        buffMenu()
                    }
                }
            )
        }else{
            this += listOf(
                "你的进攻队伍已经拥有${StatusEffects.overdrive.emoji()}" to {
                    buffMenu()
                }
            )
        }
        if(overcbuffer.get(team()) == 0) {
            this += listOf(
                "${StatusEffects.overclock.emoji()}\n30000" to {
                    if (getteammoney() >= 30000) {
                        removeteammoney(30000)
                        addovercbuffer(team())
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                player.sendMessage("玩家${name()}购买单位buff${StatusEffects.overclock.emoji()}")
                            }
                        }
                        buffMenu()
                    } else {
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                player.sendMessage("玩家${name()}想要购买buff${StatusEffects.overclock.emoji()}但队伍货币不足")
                            }
                        }
                        buffMenu()
                    }
                }
            )
        } else{
            this += listOf(
                "你的进攻队伍已经拥有${StatusEffects.overclock.emoji()}" to {
                    buffMenu()
                }
            )
        }
        if(bossbuffer.get(team()) == 0) {
            this += listOf(
                "${StatusEffects.boss.emoji()}\n40000" to {
                    if (getteammoney() >= 40000) {
                        removeteammoney(40000)
                        addbossbuffer(team())
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                player.sendMessage("玩家${name()}购买单位buff${StatusEffects.boss.emoji()}")
                            }
                        }
                        buffMenu()
                    } else {
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                player.sendMessage("玩家${name()}想要购买buff${StatusEffects.boss.emoji()}但队伍货币不足")
                            }
                        }
                        buffMenu()
                    }
                }
            )
        }else{
            this += listOf(
                "你的进攻队伍已经拥有${StatusEffects.boss.emoji()}" to {
                    buffMenu()
                }
            )
        }
        if(shieldbuffer.get(team()) == 0) {
            this += listOf(
                "${StatusEffects.shielded.emoji()}\n60000" to {
                    if (getteammoney() >= 60000) {
                        removeteammoney(60000)
                        addshieldbuffer(team())
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                player.sendMessage("玩家${name()}购买单位buff${StatusEffects.shielded.emoji()}")
                            }
                        }
                        buffMenu()
                    } else {
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                player.sendMessage("玩家${name()}想要购买buff${StatusEffects.shielded.emoji()}但队伍货币不足")
                            }
                        }
                        buffMenu()
                    }
                }
            )
        }else{
            this += listOf(
                "你的进攻队伍已经拥有${StatusEffects.shielded.emoji()}" to {
                    buffMenu()
                }
            )
        }
    }
}
//监听玩家点击
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    if((it.tile.block() is LaunchPad)
        && it.tile.team() == player.team()){
        launch(Dispatchers.game){player.placeMenu(it.tile.build as LaunchPad.LaunchPadBuild)}
    }
}
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    if((it.tile.block() is Drill)
        && it.tile.team() == player.team()){
        launch(Dispatchers.game){player.drillsmenu(it.tile.worldx(),it.tile.worldy(),it.tile.build as DrillBuild)}
    }
}
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    if((it.tile.block() == Blocks.basicAssemblerModule)
        && it.tile.team() == player.team()){
        launch(Dispatchers.game){player.superMenu()}
    }
}
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    if((it.tile.block() is Turret ||it.tile.block() == Blocks.neoplasiaReactor || it.tile.block() == Blocks.cryofluidMixer || it.tile.block() == Blocks.shockwaveTower || it.tile.block() == Blocks.overdriveDome)
        && it.tile.team() == player.team()){
        launch(Dispatchers.game){player.destoryMenu(it.tile.build as Building)}
    }
}
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    if((it.tile.block() is CoreBlock && it.tile.block() != Blocks.coreCitadel)
        && it.tile.team() == player.team()){
        launch(Dispatchers.game){player.UnitgradeMenu()}
    }
}
//监听单位摧毁
//蠢比之方法
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
listen<EventType.BlockDestroyEvent>{ b->
    if(b.tile.block() is LaunchPad || b.tile.block() in turrets ){
        Groups.player.forEach { p ->
            if (p.team() == b.tile.build.team()) {
                p.healmenu((b.tile.x * 8).toFloat(), (b.tile.y * 8).toFloat(), b.tile.build.team())
            }
        }
    }
}
listen<EventType.BlockDestroyEvent>{ b->
    if(b.tile.block() == Blocks.coreCitadel ){
        Groups.player.forEach { p ->
            if (p.team() == b.tile.build.team()) {
                p.healcoremenu((b.tile.x * 8).toFloat(), (b.tile.y * 8).toFloat(), b.tile.build.team())
            }
        }
    }
}
listen<EventType.BlockBuildEndEvent>{ b ->
    if(b.tile.block() in Drills){
        if(b.tile.block() == Blocks.mechanicalDrill) {teammoney.put(b.tile.team(),teammoney.get(b.tile.team())-30*teamdrillgrade.get(b.team));for (player in Groups.player) {
            player.buildmenu(b.tile.build,30*teamdrillgrade.get(b.team))
            player.drillsmenu(b.tile.worldx(),b.tile.worldy(),b.tile.build as DrillBuild)
        } }
        if(b.tile.block() == Blocks.pneumaticDrill) {teammoney.put(b.tile.team(),teammoney.get(b.tile.team())-60*teamdrillgrade.get(b.team));for (player in Groups.player) {
            player.buildmenu(b.tile.build,60*teamdrillgrade.get(b.team))
            player.drillsmenu(b.tile.worldx(),b.tile.worldy(),b.tile.build as DrillBuild)
        } }
        if(b.tile.block() == Blocks.laserDrill) {teammoney.put(b.tile.team(),teammoney.get(b.tile.team())-300*teamdrillgrade.get(b.team));for (player in Groups.player) {
            player.buildmenu(b.tile.build,300*teamdrillgrade.get(b.team))
            player.drillsmenu(b.tile.worldx(),b.tile.worldy(),b.tile.build as DrillBuild)
        } }
        if(b.tile.block() == Blocks.impactDrill) {teammoney.put(b.tile.team(),teammoney.get(b.tile.team())-900*teamdrillgrade.get(b.team));for (player in Groups.player) {
            player.buildmenu(b.tile.build,900*teamdrillgrade.get(b.team))
            player.drillsmenu(b.tile.worldx(),b.tile.worldy(),b.tile.build as DrillBuild)
        } }
        if(b.tile.block() == Blocks.blastDrill) {teammoney.put(b.tile.team(),teammoney.get(b.tile.team())-1000*teamdrillgrade.get(b.team));for (player in Groups.player) {
            player.buildmenu(b.tile.build,1000*teamdrillgrade.get(b.team))
            player.drillsmenu(b.tile.worldx(),b.tile.worldy(),b.tile.build as DrillBuild)
        } }
        if(b.tile.block() == Blocks.eruptionDrill) {teammoney.put(b.tile.team(),teammoney.get(b.tile.team())-3000*teamdrillgrade.get(b.team));for (player in Groups.player) {
            player.buildmenu(b.tile.build,3000*teamdrillgrade.get(b.team))
            player.drillsmenu(b.tile.worldx(),b.tile.worldy(),b.tile.build as DrillBuild)
        } }
    }
}
listen<EventType.UnitDestroyEvent>{ u->
    var teams = ArrayList<Team>()
    for (player in Groups.player) {
        if(player.team() !in teams && player.team() != Team.get(255)) {
            teams.add(player.team())
        }
    }
    when(u.unit.type) {
        in T1attackunits -> {
            teams.remove(u.unit.team())
            for (team in teams) {
                addteammoney(team, 5)
            }
            for (player in Groups.player) {
                player.dropmenu(u.unit.x, u.unit.y, 5)
            }
        }
        in T2attackunits -> {
            teams.remove(u.unit.team())
            for(team in teams) {
                addteammoney(team,20)
            }
            for(player in Groups.player){
                player.dropmenu(u.unit.x,u.unit.y,20)
            }
        }
        in T3attackunits -> {
            teams.remove(u.unit.team())
            for(team in teams) {
                addteammoney(team,60)
            }
            for(player in Groups.player){
                player.dropmenu(u.unit.x,u.unit.y,60)
            }
        }
        in T4attackunits -> {
            teams.remove(u.unit.team())
            for(team in teams) {
                addteammoney(team,150)
            }
            for(player in Groups.player){
                player.dropmenu(u.unit.x,u.unit.y,150)
            }
        }
        in T4attackunits -> {
            teams.remove(u.unit.team())
            for(team in teams) {
                addteammoney(team,200)
            }
            for(player in Groups.player){
                player.dropmenu(u.unit.x,u.unit.y,200)
            }
        }
    }
    teams.clear()
}
//内容修改
onEnable {
    contextScript<coreMindustry.UtilMapRule>().apply {
        for (unittype in allattackunits) {
            registerMapRule(unittype::controller) { Func<mindustry.gen.Unit, UnitController> { NewGroundAi() } }
        }
        for (unittype in superunits) {
            registerMapRule(unittype::controller) { Func<mindustry.gen.Unit, UnitController> { NewGroundAi() } }
        }
        registerMapRule(UnitTypes.omura::abilities) { UnitTypes.oct.abilities }
        registerMapRule(Blocks.metalFloor5::placeableOn) { false }
        registerMapRule(UnitTypes.atrax::hovering) { false }
        registerMapRule(UnitTypes.spiroct::hovering) { false }
        registerMapRule(UnitTypes.arkyid::hovering) { false }
        registerMapRule(Blocks.neoplasiaReactor::outputsLiquid) { false }
        registerMapRule(Blocks.interplanetaryAccelerator::update){ false }
        registerMapRule(Blocks.basicAssemblerModule::update){ false }
        registerMapRule((Blocks.coreAcropolis as CoreBlock)::armor){ Float.MAX_VALUE }
        registerMapRule((Blocks.coreAcropolis as CoreBlock)::health){ Int.MAX_VALUE }
        registerMapRule(Blocks.shieldProjector::health){ Int.MAX_VALUE }
    }
    contextScript<coreMindustry.UtilMapRule>().registerMapRule(
        UnitTypes.disrupt.weapons.get(0).bullet.spawnUnit::controller
    ) { Func<mindustry.gen.Unit, UnitController> { disMissileAi() } }
    /*//因为不可抗力因素被迫删除飞艇创创
    contextScript<coreMindustry.UtilMapRule>().registerMapRule(
        UnitTypes.disrupt.weapons.get(0).bullet.spawnUnit.weapons.get(0).bullet::spawnUnit
    ) { list[63] }
    contextScript<coreMindustry.UtilMapRule>().registerMapRule(
        UnitTypes.disrupt.weapons.get(0).bullet.spawnUnit.weapons.get(0).bullet.spawnUnit::controller
    ) { Func<mindustry.gen.Unit, UnitController> { ccMissileAi() } }
     */
    contextScript<ContentsTweaker>().addPatch(
        "pvp",
        Vars.dataDirectory.child("scripts").child("mapScript").child("14889.json").readString()
    )
}
//飞艇创创ai暂时改为适配无创创版本
class disMissileAi: MissileAI() {
    override fun updateMovement() {
        unloadPayloads();

        val time = if (unit is TimedKillc) (unit as TimedKillc).time() else 1000000f
        if(time >= unit.type.homingDelay && shooter != null){
            unit.lookAt(shooter.aimX, shooter.aimY);
        }
        unit.moveAt(vec.trns(unit.rotation, if(unit.type.missileAccelTime <= 0f)  (unit.speed()) else Mathf.pow(Math.min(time / unit.type.missileAccelTime, 1f), 2f) * unit.speed()))

        if(unit.within(shooter.aimX,shooter.aimY,10f)||shooter.isShooting == false){
            unit.kill()
        }
    }
}

class ccMissileAi: MissileAI() {
    override fun updateMovement() {
        unloadPayloads()
        val time = if (unit is TimedKillc) (unit as TimedKillc).time() else 1000000f
        val build = unit.buildOn()
        unit.moveAt(vec.trns(unit.rotation, if(unit.type.missileAccelTime <= 0f)  (unit.speed()) else Mathf.pow(Math.min(time / unit.type.missileAccelTime, 1f), 2f) * unit.speed()))
        if (build != null && build.team !== unit.team && (build === target || !build.block.underBullets)) {
            unit.kill()
        }
    }
}

class NewGroundAi: GroundAI() {
    override fun updateMovement() {

        val core: Building? = unit.closestEnemyCore()
        if (core != null && unit.within(core, unit.range() / 1.3f + core.block.size * Vars.tilesize / 2f)) {
            target = core
            for (mount in unit.mounts) {
                if (mount.weapon.controllable && mount.weapon.bullet.collidesGround) {
                    mount.target = core
                }
            }
        }

        if (!unit.isShooting) {
            pathfind(Pathfinder.fieldCore)
        }

        if (unit.type.canBoost && (unit.elevation > 0.001f) && !unit.onSolid()) {
            unit.elevation = Mathf.approachDelta(unit.elevation, 0f, unit.type.riseSpeed)
        }
        faceTarget()
    }
}
//循环结构
//坦克火箭弹
//防止SpawnUnit出现问题
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
//天帝核弹
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
//播报
onEnable{
    launch{
        var LastTime: Long = Time.millis()
        loop(Dispatchers.game) {
            delay(10)
            Groups.player.each { p ->
                var text = ""
                text += buildString {
                    val unit = p.unit() ?: return@buildString
                    if(teammoney.get(p.team()) <= 0 ) {
                        appendLine("[red]队伍货币负债请注意\n如果队伍持续负债超过5w将会剥夺队伍所有单位产线")
                    }
                    appendLine("[red]镇压度指你的队伍产线总能力\n最高镇压度方将获得2500*钻头认知等级/10s")
                    appendLine("[red]点击发射台可以防止炮塔\n再次点击炮塔可以拆除返回位点")
                    appendLine("[red]各类钻头可以为队伍增加货币\n升级钻头认知等级来增加收入")
                    appendLine("[green]队伍总资金${p.getteammoney()}")
                    appendLine("[green]核心生命${getteamlifes(p.team())+20}")
                    for(unitType in allattackunits){
                        if(getteamunitlinenew(p.team(),unitType) > 0)
                        appendLine("[white]${unitType.emoji()}共${getteamunitlinenew(p.team(),unitType)}条产线")
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
//发射台
onEnable {
    launch {
        loop(Dispatchers.game) {
            delay(200)
            for (b in Groups.build) {
                if (b.block() == Blocks.launchPad) {
                    Groups.player.each { allplayer ->
                        allplayer.launchpadmenu(b.x, b.y, b as LaunchPad.LaunchPadBuild)
                    }
                }
            }
        }
    }
}
//肿瘤塔
////////
onEnable {
    launch {
        loop(Dispatchers.game) {
            delay(200)
            for (b in Groups.build) {
                if (b.block() == Blocks.neoplasiaReactor) {
                   for(unit in Groups.unit){
                       if(unit.team() != b.team() && unit.within(b.x(),b.y(),200f)){
                           Call.effect(Fx.neoplasiaSmoke,b.x,b.y,200f, Color.gold)
                           Call.effect(Fx.spawnShockwave,b.x,b.y,200f, Color.gold)
                           val rand = Math.random()*10f
                           Call.effect(Fx.regenSuppressParticle,
                               (unit.x+Math.sin(Math.random()*360f)*rand).toFloat(),
                               (unit.y+Math.cos(Math.random()*360f)*rand).toFloat(),
                               20f, Color.gold)
                           unit.apply(StatusEffects.corroded,100f)
                           unit.apply(StatusEffects.sapped,100f)
                       }
                   }
                }
            }
        }
    }
}
//冷冻塔
onEnable {
    launch {
        loop(Dispatchers.game) {
            delay(2000)
            for (b in Groups.build) {
                if (b.block() == Blocks.cryofluidMixer) {
                    for(unit in Groups.unit){
                        if(unit.team() != b.team() && unit.within(b.x(),b.y(),200f)){
                            Call.effect(Fx.regenParticle,b.x,b.y,20f, Color.gold)
                            Call.effect(Fx.healWave,b.x,b.y,200f, Color.gold)
                            unit.apply(StatusEffects.freezing,100f)
                        }
                        for(bb in Groups.build){
                            if(bb.team() == b.team() && bb.block() is Turret && bb.acceptLiquid(bb,Liquids.cryofluid) && bb.within(b.x,b.y,200f)){
                                bb.liquids.add(Liquids.cryofluid,0.5f)
                                val rand = Math.random()*(bb.block().size*5).toFloat()
                                Call.effect(Fx.regenParticle,
                                    (bb.x+Math.sin(Math.random()*360f)*rand).toFloat(),
                                    (bb.y+Math.cos(Math.random()*360f)*rand).toFloat(),
                                    20f, Color.gold)
                            }
                            if(bb.team() == b.team() && bb.block() is Turret && (bb.acceptLiquid(bb,Liquids.water) && !(bb.acceptLiquid(bb,Liquids.cryofluid))) && bb.within(b.x,b.y,200f)){
                                bb.liquids.add(Liquids.water,0.5f)
                                val rand = Math.random()*(bb.block().size*0.5).toFloat()
                                Call.effect(Fx.regenSuppressParticle,
                                    (bb.x+Math.sin(Math.random()*360f)*rand).toFloat(),
                                    (bb.y+Math.cos(Math.random()*360f)*rand).toFloat(),
                                    20f, Color.HSVtoRGB(173F, 216F, 230F))

                            }
                        }
                    }
                }
            }
        }
    }
}
//震爆塔
onEnable {
    launch {
        loop(Dispatchers.game) {
            delay(5000)
            for (b in Groups.build) {
                if (b.block() == Blocks.shockwaveTower) {
                    for(unit in Groups.unit){
                        if(unit.team() != b.team() && unit.within(b.x(),b.y(),200f)){
                            Call.effect(Fx.healBlock,b.x,b.y,20f, Color.gold)
                            Call.effect(Fx.spawnShockwave,b.x,b.y,200f, Color.gold)
                            unit.apply(StatusEffects.unmoving,100f)
                        }
                    }
                }
            }
        }
    }
}
///////
onEnable {
    launch {
        loop(Dispatchers.game) {
            delay(200)
            for (b in Groups.build) {
                if (b.block() == Blocks.launchPad) {
                    Groups.player.each { allplayer ->
                        allplayer.launchpadmenu(b.x, b.y, b as LaunchPad.LaunchPadBuild)
                    }
                }
            }
        }
    }
}
//钻头
onEnable {
    launch{
        loop(Dispatchers.game){
            for(unit in Groups.unit){
                for(b in Groups.build){
                    if(b.block() is CoreBlock && unit.team() != b.team() && unit.within(b.x,b.y,100f) && unit.type != UnitTypes.emanate && b.block() == Blocks.coreAcropolis){
                        b.removeteamlifes(1)
                        Call.effect(Fx.spawnShockwave, b.x, b.y, 200f, Color.gold)
                        unit.kill()
                        val lastTime = Time.millis()
                        while(true) {
                            if (Time.millis() - lastTime >= 2_000) return@loop
                            delay(100)
                        }
                    }
                }
            }
            delay(10)
        }
    }
}
//钻头加钱系统
onEnable{
    launch{
        loop(Dispatchers.game){
            delay(200)
            for (b in Groups.build) {
                if(b.block() is Drill){
                    if(b.block() == Blocks.mechanicalDrill && teamOppressive.get(b.team()) >=1){
                        b.addteammoneybuild(3*teamdrillgrade.get(b.team()))
                    }else if(b.block() == Blocks.pneumaticDrill && teamOppressive.get(b.team()) >=2){
                        b.addteammoneybuild(8*teamdrillgrade.get(b.team()))
                    }else if(b.block() == Blocks.laserDrill && teamOppressive.get(b.team()) >=3){
                        b.addteammoneybuild(30*teamdrillgrade.get(b.team()))
                    }else if(b.block() == Blocks.impactDrill && teamOppressive.get(b.team()) >=4){
                        b.addteammoneybuild(50*teamdrillgrade.get(b.team()))
                    }else if(b.block() == Blocks.blastDrill && teamOppressive.get(b.team()) >=5){
                        b.addteammoneybuild(70*teamdrillgrade.get(b.team()))
                    }else if(b.block() == Blocks.eruptionDrill && teamOppressive.get(b.team()) >=6){
                        b.addteammoneybuild(140*teamdrillgrade.get(b.team()))
                    }else{
                        b.addteammoneybuild(0)
                    }
                }
            }
            delay(2000)
        }
    }
}
onEnable{
    launch {
        loop(Dispatchers.game){
            var teams = ArrayList<Team>()
            for (player in Groups.player) {
                if(player.team() !in teams && player.team() != Team.get(255)) {
                    teams.add(player.team())
                }
            }
            for(team in teams){
                for(unitType in allattackunits){
                    if(getteamunitlinenew(team,unitType) != 0){
                        for(Spawn in Groups.build){
                            if((Spawn.block() == Blocks.interplanetaryAccelerator) && (Spawn.team() == team)) {
                                val range = 10f
                                var i = 1
                                while(i <= getteamunitlinenew(team,unitType) * getteamunitnumnew(team)){
                                    unitType.create(team).apply {
                                        armor(getteamunitarmornew(team).toFloat())
                                        val random = Math.random() * 360f
                                        set(
                                            ((Spawn.x / 8 + (range * Math.sin(random))) * 8).toFloat(),
                                            ((Spawn.y / 8 + (range * Math.cos(random))) * 8).toFloat()
                                        )
                                        Call.effect(Fx.healBlock,x,y,20f, Color.gold)
                                        if(overdbuffer.get(team) == 1){
                                            apply(StatusEffects.overdrive, Float.MAX_VALUE)
                                        }
                                        if(overcbuffer.get(team) == 1){
                                            apply(StatusEffects.overclock, Float.MAX_VALUE)
                                        }
                                        if(bossbuffer.get(team) == 1){
                                            apply(StatusEffects.boss, Float.MAX_VALUE)
                                        }
                                        if(shieldbuffer.get(team) == 1){
                                            apply(StatusEffects.shielded, Float.MAX_VALUE)
                                        }
                                        add()
                                    }

                                    i++
                                }
                                when(unitType) {
                                    in T1attackunits->teamremoveteammoney(team, 120)
                                    in T1attackunits->teamremoveteammoney(team, 1200)
                                    in T1attackunits->teamremoveteammoney(team, 3600)
                                    in T1attackunits->teamremoveteammoney(team, 12000)
                                    in T1attackunits->teamremoveteammoney(team, 36000)
                                }
                            }
                        }
                    }
                }
                Groups.player.forEach { p->
                    if(p.team() == team){
                        for(Spawn in Groups.build) {
                            if ((Spawn.block() == Blocks.interplanetaryAccelerator) && (Spawn.team() == team)) {
                                p.callmenu(Spawn.x,Spawn.y)
                            }
                        }
                    }
                }
            }
            teams.clear()
            delay(60_000)
        }
    }
}
onEnable{
    launch{
        loop(Dispatchers.game){
            for(Spawn in Groups.build) {
                if(Spawn.block() == Blocks.interplanetaryAccelerator) {
                    val rand = Math.random() * (30f).toFloat()
                    val random = Math.random() * 360f
                    val range = 10f
                    Call.effect(
                        Fx.regenParticle,
                        (((Spawn.x / 8 + (range * Math.sin(random))) * 8).toFloat() + Math.sin(Math.random() * 360f) * rand).toFloat(),
                        (((Spawn.y / 8 + (range * Math.cos(random))) * 8).toFloat() + Math.cos(Math.random() * 360f) * rand).toFloat(),
                        20f, Color.gold
                    )
                    delay(30)
                }
            }
        }
    }
}
//游戏规则设置
onEnable{
    launch {
        loop(Dispatchers.game) {
            delay(500)
            var teams = ArrayList<Team>()
            for (player in Groups.player) {
                if (player.team() !in teams && player.team() != Team.get(255)) {
                    teams.add(player.team())
                }
            }
            for (team in teams) {
                if (getteamlifes(team) <= -20) {
                    team.cores().forEach { core ->
                        core.tile.setAir()
                    }
                }
                var coretypelist = ArrayList<Block>()
                for (core in team.cores()) {
                    coretypelist.add(core.block())
                }
                if (Blocks.coreAcropolis !in coretypelist) {
                    team.cores().forEach { core ->
                        core.tile.setAir()
                    }
                }
                coretypelist.clear()
                if (teammoney.get(team) <= 0) {
                    var teamline: Int = 0
                    for (unitType in allattackunits) {
                        teamline += getteamunitlinenew(team, unitType)
                    }
                    if (teammoney.get(team) <= -50000 && teamline != 0) {
                        for (player in Groups.player) {
                            player.sendMessage("[red]队伍${team}欠款严重已经剥夺该队所有单位产线")
                        }
                        for (unitType in allattackunits) {
                            setteamunitline(team, unitType, 0)
                        }
                    }
                    teamline = 0
                }
            }
            Groups.player.forEach { p ->
                if ((p.y()).toInt() / 8 > 90) {
                    while ((p.y()).toInt() / 8 > 90) {
                        p.team().cores().forEach { c ->
                            if (c.block == Blocks.coreNucleus) {
                                while ((p.y()).toInt() / 8 > 90) {
                                    val x = c.tile.worldx()
                                    val y = c.tile.worldy()
                                    p.unit().set(x, y)
                                    yield()
                                    delay(10)
                                }
                            }
                        }
                        p.sendMessage("禁止进入作战区域")
                    }
                }
                delay(2000)
            }
        }
    }
}
onEnable{
    launch{
        loop(Dispatchers.game){
            var teams = ArrayList<Team>()
            for (player in Groups.player) {
                if (player.team() !in teams && player.team() != Team.get(255)) {
                    teams.add(player.team())
                }
            }
            for(team in teams){
                for(unittype in allattackunits){
                    when(unittype){
                        in T1attackunits -> teamOppressive.put(team,teamOppressive.get(team)+getteamunitlinenew(team,unittype)*1)
                        in T2attackunits -> teamOppressive.put(team,teamOppressive.get(team)+getteamunitlinenew(team,unittype)*2)
                        in T3attackunits -> teamOppressive.put(team,teamOppressive.get(team)+getteamunitlinenew(team,unittype)*3)
                        in T4attackunits -> teamOppressive.put(team,teamOppressive.get(team)+getteamunitlinenew(team,unittype)*4)
                        in T5attackunits -> teamOppressive.put(team,teamOppressive.get(team)+getteamunitlinenew(team,unittype)*5)
                    }
                }
            }
            val teamOppressivelist = ArrayList<Int>()
            for(team in teams) {
                Groups.player.forEach { p ->
                    Call.setHudText(p.con, "为了获得最佳游戏体验请安装ct\n队伍${team.name}对其他队伍的镇压度是${teamOppressive.get(team)}")
                }
                teamOppressivelist.add(teamOppressive.get(team))
                delay(5000)
            }
            var largest = teamOppressivelist[0]
            for (num in teamOppressivelist) {
                if (largest < num)
                    largest = num
            }
            for(team in teams) {
                if(teamOppressive.get(team) == largest) {
                    Groups.player.forEach { p ->
                        Call.setHudText(p.con, "为了获得最佳游戏体验请安装ct\n队伍${team.name}对其他队伍的镇压度最高${teamOppressive.get(team)}获得大额增益")
                    }
                    addteammoney(team,2500*teamdrillgrade.get(team))
                    delay(5000)
                }
            }
            for(team in teams){
                teamOppressive.put(team,0)
            }
            teams.clear()
            teamOppressivelist.clear()
        }
    }
}

//游戏内容初始化
//////////
onEnable {
    launch {
        delay(1000)
        var teams = ArrayList<Team>()
        for (player in Groups.player) {
            if (player.team() !in teams && player.team() != Team.get(255)) {
                teams.add(player.team())
            }
        }
        if (teams.size < 2) {
            Groups.player.forEach { p ->
                p.sendMessage("玩家数量不足")
            }; delay(1000)
        }
        for (team in teams) {
            team.rules().buildSpeedMultiplier = 0.3f
            team.rules().blockHealthMultiplier = 3f
            team.rules().unitDamageMultiplier = 0.8f
            team.rules().unitBuildSpeedMultiplier = 0.1f
            Vars.state.rules.possessionAllowed = false
            setteammoney(team, 2000)
            setteamturret(team, 0)
            setteamunitarmor(team, 0)
            setteamunitnum(team, 1)
            setteamunitgrade(team, 1)
            bossbuffer.put(team, 0)
            overdbuffer.put(team, 0)
            overcbuffer.put(team, 0)
            shieldbuffer.put(team, 0)
            teamdrillgrade.put(team, 1)
            teamOppressive.put(team, 0)
            teamlifes.put(team,0)
            for (unitType in allattackunits) {
                setteamunitline(team, unitType, 0)
            }
        }
        teams.clear()
        Groups.player.forEach { p ->
            p.sendMessage("游戏所有内容已经重置完毕,by:guapi")
        }
    }
}
///什么？优化，同志这简直天方夜谭

