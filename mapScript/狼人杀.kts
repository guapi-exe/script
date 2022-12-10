@file:Depends("wayzer/map/betterTeam")
@file:Depends("coreMindustry/utilNext", "调用菜单")
@file:Depends("coreMindustry/utilMapRule", "修改核心单位,单位属性")
package mapScript

import arc.graphics.Color
import arc.net.Server
import arc.struct.ObjectIntMap
import arc.struct.ObjectMap
import arc.util.Align
import arc.util.Time
import coreLibrary.lib.ConfigBuilder.ConfigKey.Companion.toConfigValue
import coreLibrary.lib.util.loop
import coreMindustry.lib.sendMessage
import mindustry.Vars
import mindustry.content.*
import mindustry.entities.Units
import mindustry.game.EventType.PlayerConnect
import mindustry.game.EventType.UnitDrownEvent
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Iconc.*
import mindustry.gen.Player
import mindustry.gen.Playerc
import mindustry.type.StatusEffect
import mindustry.type.UnitType
import mindustry.world.Block
import mindustry.world.blocks.environment.Floor
import mindustry.world.blocks.defense.turrets.ItemTurret
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.blocks.campaign.LaunchPad
import mindustry.world.blocks.storage.CoreBlock
import mindustry.world.blocks.storage.CoreBlock.CoreBuild
import wayzer.lib.dao.PlayerProfile
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.random.Random
import cf.wayzer.placehold.PlaceHoldContext
import coreMindustry.lib.util.sendMenuPhone
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.ceil
import kotlin.math.max
import arc.math.geom.Geometry
import arc.math.geom.Vec2
import arc.util.Tmp
import cf.wayzer.placehold.DynamicVar
import coreLibrary.DBApi
import coreLibrary.lib.util.loop
import coreMindustry.lib.game
import kotlinx.coroutines.*
import mindustry.content.Blocks
import mindustry.content.Items
import mindustry.content.StatusEffects
import mindustry.content.UnitTypes
import mindustry.core.NetServer.ChatFormatter
import mindustry.core.World
import mindustry.game.EventType
import mindustry.game.EventType.PlayerChatEvent
import mindustry.gen.SendChatMessageCallPacket
import mindustry.net.Administration.ChatFilter
import mindustry.type.Item
import mindustry.ui.fragments.ChatFragment
import mindustry.type.ItemStack
import mindustry.world.Tile
import kotlin.reflect.KMutableProperty1
import mindustry.world.blocks.production.Drill
import java.lang.Math.pow
import mindustry.game.Rules.TeamRule
import mindustry.gen.Iconc
import mindustry.gen.WaterMovec
import mindustry.net.Administration
import mindustry.net.Administration.PlayerAction
import mindustry.ui.fragments.PlayerListFragment
import org.jetbrains.exposed.sql.transactions.transaction
/*import wayzer.lib.dao.PlayerData.Usid.clone*/
import wayzer.lib.dao.util.TransactionHelper
import java.util.*



val menu = contextScript<coreMindustry.UtilNext>()

val playerMoney: ObjectIntMap<String> = ObjectIntMap()//money
val playervote: ObjectIntMap<String> = ObjectIntMap()//vote
val playernumber: ObjectIntMap<String> = ObjectIntMap()//number
val playervoted: ObjectIntMap<String> = ObjectIntMap()//voted
val kills: ObjectIntMap<String> = ObjectIntMap()//players
val playerteam: ObjectIntMap<String> = ObjectIntMap()//playerhaveteam
val getplayer: ObjectIntMap<String> = ObjectIntMap()//get
val playerchat:MutableMap<String, String?> = mutableMapOf()
val playergetvote = ArrayList<Int>()
val badplayer = ArrayList<Player>()
val goodplayer = ArrayList<Player>()
val haveplayer = ArrayList<Player>()
val teams = contextScript<wayzer.map.BetterTeam>()
lateinit var canVote: (Player) -> Boolean
var playerlist = Groups.player.toList()
var playercount = Groups.player.toList().count()
var unitcount = Groups.unit.toList().count()
fun allCanVote() = Groups.player.filter(canVote)
fun Player.addplayervoted(amount: Int){
    return playervoted.put(uuid(),playervoted.get(uuid())+amount)
}
fun Player.addplayer(amount: Int){
    return getplayer.put(uuid(),getplayer.get(uuid())+amount)
}
fun Player.addplayervote(amount: Int){
    return playervote.put(uuid(),playervote.get(uuid())+amount)
}
fun Player.addkills(amount: Int){
    return kills.put(uuid(),kills.get(uuid())+amount)
}
fun Player.addplayernumber(amount: Int){
    return playernumber.put(uuid(),playernumber.get(uuid())+amount)
}
fun Player.getplayervotes():Int {
    return playervote.get(uuid())
}
fun Player.getplayervoted():Int {
    return playervoted.get(uuid())
}
fun Player.getplayers():Int {
    return getplayer.get(uuid())
}
fun Player.getkills():Int {
    return kills.get(uuid())
}
fun Player.getplayernumber():Int {
    return playernumber.get(uuid())
}
fun Player.checkplayervotes(amount:Int): Boolean {
    return playervote.get(uuid()) >= amount
}
fun Player.checkplayer(amount:Int): Boolean {
    return getplayer.get(uuid()) >= amount
}
fun Player.removeplayervotes(amount: Int){
    return playervote.put(uuid(),playervote.get(uuid()) - amount)
}
fun Player.removeplayervoted(amount: Int){
    return playervoted.put(uuid(),playervoted.get(uuid()) - amount)
}
fun Player.removeplayer(amount: Int){
    return getplayer.put(uuid(),getplayer.get(uuid()) - amount)
}
fun Player.removeplayernumber(amount: Int){
    return playernumber.put(uuid(),playernumber.get(uuid()) - amount)
}
fun Player.removekills(amount: Int){
    return kills.put(uuid(),kills.get(uuid()) - amount)
}
//玩家投票ui
fun playerui(amount: Int, x :Float, y :Float){
    if (amount <= 0) return
    var playervoted = amount
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        var i = 0
        while(i<=playercount){
            i--
            delay(200)
            if (Time.millis() - dorpTime >= 2_000) break
            val units = buildList {
                Units.nearby(null,dropX,dropY,400f){
                    add(it)
                }
            }
            units.forEach{
                Call.label(
                    it.player?.con ?: return@forEach, "[green]${playervoted}", 0.21f,
                    dropX, dropY)
            }
        }
    }
}

//玩家死亡ui
fun playerdead(x :Float, y :Float,number: Int){
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        var i = 0
        while(i<=playercount){
            i--
            delay(200)
            if (Time.millis() - dorpTime >= 500_000) break
            val units = buildList {
                Units.nearby(null,dropX,dropY,400f){
                    add(it)
                }
            }
            units.forEach{
                Call.label(
                    it.player?.con ?: return@forEach, "[green]这可能是玩家${number}的尸体\n[lightgray]${((Time.millis() - dorpTime) / 1000)}s/500s", 0.21f,
                    dropX, dropY)
            }
        }
    }
}

//提示ui
suspend fun Player.startvoteMenu() {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]注意:无法悔票",
        """
            [green]每人只有一票
            [yellow]点击你认为的玩家开始投票
        """.trimIndent()
    ){
        add(listOf(
            "[green]确定" to {
            }
        ))
    }
}
//playerkilldelay
fun Player.killdelay(){
    launch(Dispatchers.game){
        val lastTime = Time.millis()
        if(getkills()>=1) {
            delay(100)
            sendMessage("[red]猎杀技能进入冷却80s后冷却完毕")
            while (true) {
                if (Time.millis() - lastTime >= 80_000) {
                    addkills(-1)
                    sendMessage("[green]猎杀者技能冷却完毕")
                    return@launch
                }
                delay(100)
            }
        }
        delay(100)
    }
}
//vote Strat
fun voteStrat(){
    launch(Dispatchers.game){
        playerlist.forEach { allplayer ->
            allplayer.addplayervote(1)
            var i = 0
            while(i<10){
                i++
                allplayer.unit().set((((Math.sin(((360/playercount)*allplayer.getplayernumber()).toDouble())*25f)+244f)*8).toFloat()
                    ,(((Math.cos(((360/playercount)*allplayer.getplayernumber()).toDouble())*25f)+275f)*8).toFloat())
                delay(20)
                allplayer.unit().apply(StatusEffects.unmoving,1000f)
            }
        }
    }
}
//vote stop
fun votestop(){
    launch(Dispatchers.game){
        playerlist.forEach { allplayer->
            if(allplayer.checkplayervotes(1)) {
                allplayer.removeplayervotes(1)
                allplayer.sendMessage("[red]投票结束")
            }
        }
    }
}



/*
    loop(Dispatchers.game){
        if(){}
        Groups.unit.each{
            it.set(((((Math.sin(360 / playercount.toDouble())) * 1) * 50) + 200).toFloat(),
                ((((Math.cos(360 / playercount.toDouble())) * 1) * 50) + 200).toFloat())
        }
        yield()
    }

 */
/*
var startTime = Time.millis()
onEnable{
    loop(Dispatchers.game){
        Groups.unit.each{
            if(it.isPlayer){
                it.set((((((Math.sin(360 / playercount.toDouble())) * 1) * 50) + 200)*8f).toFloat(),
                    (((((Math.cos(360 / playercount.toDouble())) * 1) * 50) + 200)*8f).toFloat())
            }
        }

        yield()
    }
}

 */

//投票
listen<EventType.TapEvent> {
    val player = it.player
    if (player.dead()) return@listen
    if (player.checkplayervotes(1)) {
        for (otherplayer in playerlist) {
            if(player.getplayervotes()<=0) return@listen
            if ((otherplayer.unit().within(player.mouseX(), player.mouseY(), 10f))) {
                player.removeplayervotes(1)
                player.sendMessage("[yellow]你已经投票")
                otherplayer.addplayervoted(1)
                otherplayer.sendMessage("[yellow]你再次获得一票")
                var amount = otherplayer.getplayervoted()
                var x = otherplayer.unit().x
                var y = otherplayer.unit().y
                playerui(amount,x,y)
            }
        }
    }
}

suspend fun Player.badPlayermenu(number: Int){
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]注意:选择无法更改",
        """
            你可更换的玩家编号为${number}
            你可以通过伪造自己身份和尸体信息来迷惑其他人
            你有50s的杀人冷却注意
        """.trimIndent()
    ) {
        add(listOf(
            "[green]确定" to {
                playerdead(x(),y(),getplayernumber())
                removeplayernumber(getplayernumber())
                addplayernumber(number)
            },
            "[green]否" to {
                playerdead(x(),y(),number)
            }
        ))
    }
}
//猎杀者系统
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    badplayer.forEach{bad->
        if(player == bad){
            for(otherplayer in playerlist){
                if(otherplayer.unit().within(player.mouseX(),player.mouseY(),10f)
                    &&player != otherplayer
                    &&otherplayer.unit().within(player.unit().x,player.unit().y,100f)) {
                    if(otherplayer.unit().within(244f*8f,267f*8f,450f)){
                        player.sendMessage("[red]在安全区你不能杀人")
                        return@listen
                    }
                    if(player.getkills()>=1) {
                        player.sendMessage("猎杀技能冷却中")
                        return@listen
                    }
                    for (bad2 in badplayer) {
                        if(otherplayer == bad2) {
                            player.sendMessage("这是你的队友你不能杀死他")
                            return@listen
                        }
                    }
                    otherplayer.removeplayer(1)
                    otherplayer.sendMessage("[red]你死了")
                    var number = otherplayer.getplayernumber()
                    launch(Dispatchers.game){player.badPlayermenu(number)}
                    otherplayer.removeplayernumber(otherplayer.getplayernumber())//玩家死亡移除编号
                    player.addkills(1)//猎杀者移除一次杀人机会
                    player.killdelay()
                    teams.changeTeam(otherplayer, teams.spectateTeam)
                    return@listen
                }
            }
        }
    }
}
//踢人系统
fun playerban() {
    launch(Dispatchers.game) {
        for (allplayer in playerlist) {
            playergetvote.add(allplayer.getplayervoted())
        }
        var largest = playergetvote[0]
        for (num in playergetvote) {
            if (largest < num)
                largest = num
        }
        playergetvote.remove(largest)
        var last = playergetvote[0]
        for (num in playergetvote) {
            if (last < num)
                last = num
        }
        if (last == largest) {
            playerlist.forEach { it.sendMessage("多人票数相同投票结束") }
            return@launch
        }
        for (allplayer in playerlist) {
            if (allplayer.getplayervoted() == largest) {
                allplayer.removeplayer(1)
                for(otherplayer in playerlist){
                    otherplayer.sendMessage("[green]${allplayer.name}被票出")
                }
                allplayer.removeplayervoted(allplayer.getplayervoted())
                teams.changeTeam(allplayer, teams.spectateTeam)
            }
        }
    }
}

/*
listen<EventType.TapEvent> {
    val player = it.player
    if(player.dead()) return@listen
    if(playertype == null){
        for (otherplayer in playerlist) {
            if ((otherplayer.unit().within(player.mouseX(), player.mouseY(), 10f))
                && player != otherplayer){
                teams.changeTeam(otherplayer, teams.spectateTeam)
                var x = otherplayer.x
                var y = otherplayer.y
                }
        }
    }
}

 */
//玩家匿名系统加反重生
fun Player.updateName() {
    name = "玩家${this.getplayernumber()}".with(
        "player" to this
    ).toString()
}

listen<EventType.PlayerConnect> {
    val p = it.player
    p.updateName()
}

onEnable {
    launch {
        DBApi.DB.awaitInit()
        loop(Dispatchers.game) {
            delay(5)
            Groups.player.forEach { it.updateName() }
            for(allplayer in playerlist){
                if(allplayer.getplayers() < 0 && allplayer.team() != Team.get(255))
                    teams.changeTeam(allplayer, teams.spectateTeam)
            }
        }
    }
}

//游戏全局记时
onEnable{
    launch{
        delay(100)
        loop(Dispatchers.game) {
            val lastTime = Time.millis()
            while (true) {
                Groups.player.forEach {
                    Call.setHudText(it.con, "[green]距离投票开始还有:${(400 * 1000 - (Time.millis() - lastTime)) / 1000}/s")
                }
                if(Time.millis()-lastTime>=400_000) {
                    voteStrat()
                    playerlist.forEach { allplayer ->
                        launch{ allplayer.startvoteMenu() }
                    }
                    delay(100)
                    val lastTime = Time.millis()
                    while (true) {
                        Groups.player.forEach {
                            Call.setHudText(it.con, "[red]距离投票结束还有:${(80 * 1000 - (Time.millis() - lastTime)) / 1000}/s")
                        }
                        if (Time.millis() - lastTime >= 80_000) {
                            votestop()
                            playerban()
                            delay(100)
                            return@loop
                        }
                        delay(100)
                    }
                }
                delay(300)
            }
        }
    }
}

//玩家分配
onEnable {
    launch {
        playerlist.forEach { allplayer->
            allplayer.sendMessage("[red]游戏即将开始")
        }
        delay(30000)
        val lastTime =  Time.millis()
        delay(100)
        var i = 1
        loop(Dispatchers.game) {
            var f = 1
            while (true) {
                for(allplayer in playerlist){
                    allplayer.addplayernumber(f)
                    f++
                }
                playerlist.forEach { allplayer ->
                    val badplayercount = (playercount / 4).toInt()
                    while (i <= badplayercount) {
                        i++
                        badplayer.add(allplayer)
                        allplayer.sendMessage("[red]你是猎杀(ma)者需要杀光所有人")
                        return@forEach
                    }
                    goodplayer.add(allplayer)
                    allplayer.sendMessage("[green]你是逃生者完成任务逃离这里")
                }
                while (true) {
                    delay(1000)
                }
            }
        }
    }
}
//暂时抛弃
/*
//玩家指定范围交流系统
listen<EventType.PlayerChatEvent>{
    var player = it.player
    var message = it.message
    player.sendMessage("[green]你说:[yellow]"+message)
    if(player.dead()) return@listen
    for(allplayer in playerlist) {
        if(allplayer.unit().within(player.unit().x,player.unit().y,150f)
            &&allplayer !=player) {
            allplayer.sendMessage("[green]你听到 [red]玩家${player.getplayernumber()}说:[]" + message)
        }
    }
    Vars.netServer.admins.addChatFilter { allplayer, message -> null }
}

 */
/*
//选择开启
//反新加玩家
listen<EventType.PlayerJoin>{
    val player = it.player
    player.sendMessage("你为后来加入者无法参加这次游戏")
    player.removeplayer(1)
    teams.changeTeam(player, teams.spectateTeam)
}
