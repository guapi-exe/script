@file:Depends("coreMindustry/utilNext", "调用菜单")
@file:Depends("coreMindustry/contentsTweaker", "修改核心单位,单位属性")
@file:Depends("coreMindustry/utilMapRule", "修改单位ai")
package mapScript

import arc.Core
import arc.func.Func
import arc.math.Mathf
import arc.struct.ObjectIntMap
import arc.struct.ObjectMap
import arc.util.Align
import arc.util.Time
import coreLibrary.lib.util.loop
import coreMindustry.ContentsTweaker
import coreMindustry.UtilNext
import coreMindustry.lib.game
import coreMindustry.lib.listen
import mindustry.Vars
import mindustry.Vars.dataDirectory
import mindustry.ai.types.*
import mindustry.content.*
import mindustry.ctype.ContentType
import mindustry.entities.bullet.BulletType
import mindustry.entities.units.UnitController
import mindustry.game.EventType
import mindustry.game.Team
import mindustry.gen.*
import mindustry.type.UnitType
import mindustry.world.blocks.campaign.LaunchPad
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild
import mindustry.world.blocks.production.Drill
import mindustry.world.blocks.production.Drill.DrillBuild
import mindustry.world.blocks.storage.CoreBlock
import mindustry.world.meta.BlockGroup
import java.util.Objects

val teammoney: ObjectIntMap<Team> = ObjectIntMap()//队伍货币
val teamunitgrade: ObjectIntMap<Team> = ObjectIntMap()//队伍进攻单位等级
val teamunitarmor: ObjectIntMap<Team> = ObjectIntMap()//队伍进攻单位护甲
val teamunitnum: ObjectIntMap<Team> = ObjectIntMap()//队伍进攻单位部队数量
val teamturret: ObjectIntMap<Team> = ObjectIntMap()//队伍武器伤害
val teamunitline: ObjectIntMap<String> = ObjectIntMap()//
fun Building.addteammoneybuild(amount: Int){
    return teammoney.put(team(),teammoney.get(team()) + amount)
}
fun Player.addteamunitline(unitType: UnitType,amount: Int){
    val teamunit = team().toString() + unitType.toString()
    return teamunitline.put(teamunit,teamunitline.get(teamunit) + amount)
}
fun Player.addteammoneyplayer(amount: Int){
    return teammoney.put(team(),teammoney.get(team()) + amount)
}
fun Player.getteammoney():Int {
    return teammoney.get(team())
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
fun getteamunitgradenew(team: Team):Int {
    return teamunitgrade.get(team)
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
fun Player.addteamunitgrade(amount: Int){
    return teamunitgrade.put(team(),teamunitgrade.get(team()) + amount)
}
fun Player.addteamunitarmor(amount: Int){
    return teamunitarmor.put(team(),teamunitarmor.get(team()) + amount)
}
fun Player.addteamunitnum(amount: Int){
    return teamunitnum.put(team(),teamunitnum.get(team()) + amount)
}
fun Player.addteamturret(amount: Int){
    return teamturret.put(team(),teamturret.get(team()) + amount)
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
val T1attackunits = arrayOf(
    UnitTypes.stell,
    UnitTypes.flare,
    UnitTypes.nova,
    UnitTypes.crawler
)
val T2attackunits = arrayOf(
    UnitTypes.locus,
    UnitTypes.horizon,
    UnitTypes.pulsar,
    UnitTypes.atrax
)
val T3attackunits = arrayOf(
    UnitTypes.precept,
    UnitTypes.zenith,
    UnitTypes.quasar,
    UnitTypes.spiroct
)
val T4attackunits = arrayOf(
    UnitTypes.vanquish,
    UnitTypes.quad,
    UnitTypes.vela,
    UnitTypes.arkyid
)
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
            Call.label(con ?: return@launch, "[green]点我放置炮塔", 0.21f,
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
            Call.label(con ?: return@launch, "[green]点我升级钻头", 0.21f,
                dropX, dropY
            )
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
      this += listOf(
          "哒哒哒哒${Blocks.spectre.emoji()}\n3000￥" to {
              if(getteammoney()>=3000) {
                  removeteammoney(3000)
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
                  delay(30)
                  tile1.setNet(Blocks.air, team(), 0)
                  Groups.player.each { p->
                      if(p.team() == team()){
                          sendMessage("玩家${name()}消费队伍资金3000￥放置了炮塔${Blocks.spectre.emoji()}")
                      }
                  }
              }else{
                  Groups.player.each { p->
                      if(p.team() == team()){
                          sendMessage("玩家${name()}想要放置炮塔${Blocks.spectre.emoji()}但队伍资金不足")
                      }
                  }
              }
          },"滋滋滋${Blocks.lustre.emoji()}\n2000￥" to {
              if(getteammoney()>=2000) {
                  removeteammoney(2000)
                  var tile = Vars.world.tiles.getn(
                      Launchpad.x.toInt() / 8,
                      Launchpad.y.toInt() / 8
                  )
                  val turretx = Launchpad.x.toInt() / 8
                  val turrety = Launchpad.y.toInt() / 8
                  tile.setNet(Blocks.lustre, team(), 0)
                  val size = (tile.build.hitSize() / 8).toInt()
                  var tile1 = Vars.world.tiles.getn(
                      turretx + size - 1,
                      turrety
                  )
                  tile1.setNet(Blocks.itemSource, team(), 0)
                  tile1.build.apply { configure(Items.thorium) }
                  delay(30)
                  tile1.setNet(Blocks.air, team(), 0)
                  Groups.player.each { p->
                      if(p.team() == team()){
                          sendMessage("玩家${name()}消费队伍资金3000￥放置了炮塔${Blocks.lustre.emoji()}")
                      }
                  }
              }else{
                  Groups.player.each { p->
                      if(p.team() == team()){
                          sendMessage("玩家${name()}想要放置炮塔${Blocks.lustre.emoji()}但队伍资金不足")
                      }
                  }
              }
          }
      )
    }
}
//钻头升级ui
suspend fun Player.DrillMenu(drill: DrillBuild) {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            钻头升级界面
            当前钻头等级为${drill}
        """.trimIndent()
    ) {
        if(drill.block == Blocks.mechanicalDrill) {
            this += listOf(
                "升级->${Blocks.pneumaticDrill.emoji()}\n50￥" to {
                    if(getteammoney() >= 50) {
                        removeteammoney(50)
                        var tile = Vars.world.tiles.getn(
                            drill.x.toInt() / 8,
                            drill.y.toInt() / 8
                        )
                        tile.setNet(Blocks.pneumaticDrill, team(), 0)
                        Groups.player.each { p->
                            if(p.team() == team()){
                                sendMessage("玩家${name()}消费队伍资金50￥升级了钻头")
                            }
                        }
                    }else{
                        Groups.player.each { p->
                            if(p.team() == team()){
                                sendMessage("玩家${name()}想要升级钻头但队伍资金不足")
                            }
                        }
                    }
                }
            )
        }else if(drill.block == Blocks.pneumaticDrill){
            this += listOf(
                "升级->${Blocks.laserDrill.emoji()}\n300￥" to {
                    if(getteammoney() >= 300) {
                        removeteammoney(300)
                        var tile = Vars.world.tiles.getn(
                            drill.x.toInt() / 8,
                            drill.y.toInt() / 8
                        )
                        tile.setNet(Blocks.laserDrill, team(), 0)
                        Groups.player.each { p->
                            if(p.team() == team()){
                                sendMessage("玩家${name()}消费队伍资金300￥升级了钻头")
                            }
                        }
                    }else{
                        Groups.player.each { p->
                            if(p.team() == team()){
                                sendMessage("玩家${name()}想要升级钻头但队伍资金不足")
                            }
                        }
                    }
                }
            )
        }else if(drill.block == Blocks.laserDrill){
            this += listOf(
                "升级->${Blocks.impactDrill.emoji()}\n500￥" to {
                    if(getteammoney() >= 500) {
                        removeteammoney(500)
                        var tile = Vars.world.tiles.getn(
                            drill.x.toInt() / 8,
                            drill.y.toInt() / 8
                        )
                        tile.setNet(Blocks.impactDrill, team(), 0)
                        Groups.player.each { p->
                            if(p.team() == team()){
                                sendMessage("玩家${name()}消费队伍资金500￥升级了钻头")
                            }
                        }
                    }else{
                        Groups.player.each { p->
                            if(p.team() == team()){
                                sendMessage("玩家${name()}想要升级钻头但队伍资金不足")
                            }
                        }
                    }
                }
            )
        } else if(drill.block == Blocks.impactDrill){
            this += listOf(
                "升级->${Blocks.blastDrill.emoji()}\n1000￥" to {
                    if(getteammoney() >= 1000) {
                        removeteammoney(1000)
                        var tile = Vars.world.tiles.getn(
                            drill.x.toInt() / 8,
                            drill.y.toInt() / 8
                        )
                        tile.setNet(Blocks.blastDrill, team(), 0)
                        Groups.player.each { p->
                            if(p.team() == team()){
                                sendMessage("玩家${name()}消费队伍资金1000￥升级了钻头")
                            }
                        }
                    }else{
                        Groups.player.each { p->
                            if(p.team() == team()){
                                sendMessage("玩家${name()}想要升级钻头但队伍资金不足")
                            }
                        }
                    }
                }
            )
        } else if(drill.block() == Blocks.blastDrill){
            this += listOf(
                "升级->${Blocks.eruptionDrill.emoji()}\n2000￥" to {
                    if(getteammoney() >= 2000) {
                        removeteammoney(2000)
                        var tile = Vars.world.tiles.getn(
                            drill.x.toInt() / 8,
                            drill.y.toInt() / 8
                        )
                        tile.setNet(Blocks.eruptionDrill, team(), 0)
                        Groups.player.each { p->
                            if(p.team() == team()){
                                sendMessage("玩家${name()}消费队伍资金2000￥升级了钻头")
                            }
                        }
                    }else{
                        Groups.player.each { p->
                            if(p.team() == team()){
                                sendMessage("玩家${name()}想要升级钻头但队伍资金不足")
                            }
                        }
                    }
                }
            )
        }else{
            this += listOf(
                "[red]该钻头等级已满!!" to{
                }
            )
        }
    }
}
//炮塔拆除ui
suspend fun Player.destoryMenu(turret: TurretBuild) {
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
                addteammoneyplayer(1)
                for (player in Groups.player) {
                    if(team() == player.team()){
                        sendMessage("玩家${name()}拆除了炮塔${turret}name:${turret.block.name}")
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
        """.trimIndent()
    ){
        this += listOf(
            "[green]添加" to {
                addteamunitline(unitType,1)
            },"[green]移除" to {
                removeteamunitline(unitType,1)
            }
        )
    }
}
suspend fun Player.UnitshopMenu() {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            再这里购买一条单位产线或者移除
        """.trimIndent()
    ){
        this += listOf(
            "${UnitTypes.dagger.emoji()}\n100\n维护价格每秒:20" to {
                SureMenu(UnitTypes.dagger)
                removeteammoney(100)
            },
            "${UnitTypes.flare.emoji()}\n100\n维护价格每秒:20" to {
                SureMenu(UnitTypes.flare)
                removeteammoney(100)
            }
        )
    }
}
suspend fun Player.UnitgradeMenu() {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            再这里你可以升级你的综合属性
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
                                sendMessage("玩家${name()}购买了进攻单位等级当前:${getteamunitgrade()}级")
                            }
                        }
                    } else {
                        for (player in Groups.player) {
                            if (team() == player.team()) {
                                sendMessage("玩家${name()}想要购买了进攻单位等级但队伍货币不足")
                            }
                        }
                    }
                }
            )
        }else{
            this += listOf(
                "等级已满无需购买" to {
                }
            )
        }
        this += listOf(
            "[red]升级进攻单位护甲当前${getteamunitarmor()}->${getteamunitarmor()+5}\n${((getteamunitarmor()+5)*500)*((getteamunitarmor()+5)/4)}" to {
                if(getteammoney() > ((getteamunitarmor()+5)*500)*((getteamunitarmor()+5)/4)) {
                    addteamunitarmor(5)
                    removeteammoney(((getteamunitarmor()+5)*500)*((getteamunitarmor()+5)/4))
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            sendMessage("玩家${name()}购买了进攻单位护甲当前:${getteamunitarmor()}")
                        }
                    }
                }else {
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            sendMessage("玩家${name()}想要购买了进攻单位护甲但队伍货币不足")
                        }
                    }
                }
            }
        )
        this += listOf(
            "[yellow]升级进攻单位部队数量当前${getteamunitnum()}->${getteamunitnum()+1}\n${((getteamunitnum())*20000)}" to {
                if(getteammoney() > ((getteamunitnum())*20000)) {
                    addteamunitnum(1)
                    removeteammoney(((getteamunitnum())*20000))
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            sendMessage("玩家${name()}购买了进攻单位部队数量当前:${getteamunitnum()}")
                        }
                    }
                }else {
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            sendMessage("玩家${name()}想要购买了进攻单位部队数量但队伍货币不足")
                        }
                    }
                }
            }
        )
        this += listOf(
            "[green]升级武器伤害当前${1+getteamturret()*0.05}->${1+(getteamturret()+1)*0.05}\n${((getteamturret()+1)*200)*(getteamturret()/4+1)}" to {
                if(getteammoney() > ((getteamturret()+1)*200)*(getteamturret()/4+1)) {
                    addteamturret(1)
                    removeteammoney(((getteamturret()+1)*200)*(getteamturret()/4+1))
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            sendMessage("玩家${name()}购买了武器伤害当前:${1+getteamturret()*0.05}")
                        }
                    }
                }else {
                    for (player in Groups.player) {
                        if (team() == player.team()) {
                            sendMessage("玩家${name()}想要购买了武器伤害但队伍货币不足")
                        }
                    }
                }
            }
        )
        this += listOf(
            "[red]单位产线购买" to {
                UnitshopMenu()
            }
        )
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
        launch(Dispatchers.game){player.DrillMenu(it.tile.build as Drill.DrillBuild)}
    }
}
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    if((it.tile.block() is Turret)
        && it.tile.team() == player.team()){
        launch(Dispatchers.game){player.destoryMenu(it.tile.build as TurretBuild)}
    }
}
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    if((it.tile.block() is CoreBlock)
        && it.tile.team() == player.team()){
        launch(Dispatchers.game){player.UnitgradeMenu()}
    }
}
//监听单位摧毁
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
//内容修改
onEnable {
    contextScript<coreMindustry.UtilMapRule>().registerMapRule(
        UnitTypes.disrupt.weapons.get(0).bullet.spawnUnit::controller
    ) { Func<mindustry.gen.Unit, UnitController> { disMissileAi() } }
    contextScript<coreMindustry.UtilMapRule>().registerMapRule(
        UnitTypes.disrupt.weapons.get(0).bullet.spawnUnit.weapons.get(0).bullet::spawnUnit
    ) { list[63] }
    contextScript<coreMindustry.UtilMapRule>().registerMapRule(
        UnitTypes.disrupt.weapons.get(0).bullet.spawnUnit.weapons.get(0).bullet.spawnUnit::controller
    ) { Func<mindustry.gen.Unit, UnitController> { ccMissileAi() } }
    contextScript<ContentsTweaker>().addPatch(
        "pvp",
        dataDirectory.child("contents-patch").child("10.json").readString()
    )
}
class disMissileAi: MissileAI() {
    override fun updateMovement() {
        unloadPayloads();

        val time = if (unit is TimedKillc) (unit as TimedKillc).time() else 1000000f
        if(time >= unit.type.homingDelay && shooter != null){
            unit.lookAt(shooter.aimX, shooter.aimY);
        }
        unit.moveAt(vec.trns(unit.rotation, if(unit.type.missileAccelTime <= 0f)  (unit.speed()) else Mathf.pow(Math.min(time / unit.type.missileAccelTime, 1f), 2f) * unit.speed()))

        if(unit.within(shooter.aimX,shooter.aimY,100f)||shooter.isShooting == false){
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
//循环结构
//坦克火箭弹
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
            for(unitType in all)
            Groups.player.each { p ->
                var text = ""
                text += buildString {
                    val unit = p.unit() ?: return@buildString
                    appendLine("[red]队伍总资金${p.getteammoney()}")
                    appendLine("null")
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
//出兵点
onEnable {
    launch {
        loop(Dispatchers.game) {
            delay(200)
            for (b in Groups.build) {
                if (b.block() == Blocks.interplanetaryAccelerator) {
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
    launch {
        loop(Dispatchers.game) {
            delay(200)
            for (b in Groups.build) {
                if (b.block() is Drill) {
                    Groups.player.each { allplayer ->
                        if(b.block() == Blocks.eruptionDrill) return@each
                        allplayer.drillsmenu(b.x,b.y,b as DrillBuild)
                    }
                }
            }
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
                    if(b.block() == Blocks.mechanicalDrill){
                        b.addteammoneybuild(5)
                    }else if(b.block() == Blocks.pneumaticDrill){
                        b.addteammoneybuild(10)
                    }else if(b.block() == Blocks.laserDrill){
                        b.addteammoneybuild(40)
                    }else if(b.block() == Blocks.impactDrill){
                        b.addteammoneybuild(70)
                    }else if(b.block() == Blocks.blastDrill){
                        b.addteammoneybuild(100)
                    }else if(b.block() == Blocks.eruptionDrill){
                        b.addteammoneybuild(200)
                    }else{
                        b.addteammoneybuild(0)
                    }
                }
            }
            delay(1000)
        }
    }
}
onEnable{
    launch {
        var teams = ArrayList<Team>()
        loop(Dispatchers.game){
            for (player in Groups.player) {
                if(player.team() !in teams) {
                    teams.add(player.team())
                }
            }
            for(team in teams){
                val allattackunit = T1attackunits+T2attackunits+T3attackunits+T4attackunits+T5attackunits
                for(unitType in allattackunit){
                    if(getteamunitlinenew(team,unitType) != 0){
                        for(Spawn in Groups.build){
                            if((Spawn.block() == Blocks.interplanetaryAccelerator) && (Spawn.team() != team)){
                                var time = 0
                                while(time <= 20) {
                                    val range = 15f
                                    val random = Math.random() * 360f
                                    var tile = Vars.world.tiles.getn(
                                        ((Spawn.x + (range * Math.sin(random))) / 8).toInt(),
                                        ((Spawn.y + (range * Math.cos(random))) / 8).toInt()
                                    )
                                    if(tile.floor() != null && tile.block() == null) break
                                    time++
                                }
                                val range = 15f
                                repeat(getteamunitnumnew(team)) {
                                    unitType.create(Team.get(2)).apply {
                                        val random = Math.random() * 360f
                                        armor(getteamunitarmornew(team).toFloat())
                                        set(
                                            (Spawn.x + (range * Math.sin(random))).toFloat(),
                                            (Spawn.y + (range * Math.cos(random))).toFloat()
                                        )
                                    }
                                }
                            }else{
                                Groups.player.forEach{ p->
                                    p.sendMessage("游戏出现错误行星发射器各队数量不足")
                                }
                            }
                        }
                    }
                }
            }
            delay(10000)
        }
    }
}

