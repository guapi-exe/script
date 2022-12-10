
@file:Depends("coreMindustry/utilNext", "调用菜单")
@file:Depends("coreMindustry/utilMapRule", "修改核心单位,单位属性")
package mapScript

import arc.struct.ObjectIntMap
import arc.util.Align
import arc.util.Time
import coreLibrary.lib.util.loop
import coreMindustry.lib.sendMessage
import mindustry.content.*
import mindustry.entities.Units
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Iconc.*
import mindustry.gen.Player
import mindustry.world.blocks.campaign.LaunchPad
import mindustry.world.blocks.storage.CoreBlock
import coreLibrary.DBApi
import coreLibrary.lib.util.loop
import coreMindustry.lib.game
import kotlinx.coroutines.*
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.content.StatusEffects
import mindustry.content.UnitTypes
import mindustry.core.NetServer
import mindustry.game.EventType
import mindustry.net.Administration
import mindustry.world.blocks.campaign.LaunchPad.LaunchPadBuild
import mindustry.world.blocks.storage.CoreBlock.CoreBuild
import java.util.*



val menu = contextScript<coreMindustry.UtilNext>()

val playerMoney: ObjectIntMap<String> = ObjectIntMap()
val playervote: ObjectIntMap<String> = ObjectIntMap()//
val playernumber: ObjectIntMap<String> = ObjectIntMap()
val playervoted: ObjectIntMap<String> = ObjectIntMap()
val kills: ObjectIntMap<String> = ObjectIntMap()//
val destory: ObjectIntMap<String> = ObjectIntMap()//
val playerteam: ObjectIntMap<String> = ObjectIntMap()//
val getplayer: ObjectIntMap<String> = ObjectIntMap()//
val playerchance: ObjectIntMap<String> = ObjectIntMap()//
val playergetvote = ArrayList<Int>()
val badplayer = ArrayList<Player>()
val goodplayer = ArrayList<Player>()
val haveplayer = ArrayList<Player>()
var Start: Boolean = false
var VoteStart: Boolean = false
var Stop: Boolean = false
var GameOver: Boolean = false
val teams = mutableMapOf<String, Team>()
customLoad(::teams, teams::putAll)
val spectateTeam = Team.all[255]!!
var playercount = Groups.player.toList().count()
val LaunchPadON: ObjectIntMap<LaunchPadBuild> = ObjectIntMap()//运行情况
fun LaunchPadBuild.ON(): Int { return LaunchPadON[this] }
fun LaunchPadBuild.removeON(amount: Int) { LaunchPadON.put(this, ON() - amount) }
fun LaunchPadBuild.addON(amount: Int) { LaunchPadON.put(this, ON() + amount)}
val LaunchPadZT: ObjectIntMap<LaunchPadBuild> = ObjectIntMap()//运行情况
fun LaunchPadBuild.ZT(): Int { return LaunchPadZT[this] }
fun LaunchPadBuild.removeZT(amount: Int) { LaunchPadZT.put(this, ON() - amount) }
fun LaunchPadBuild.addZT(amount: Int) { LaunchPadZT.put(this, ON() + amount)}
fun taskcount(): Int{
    var i = 0
    for (b in Groups.build) {
        if(b.block() == Blocks.launchPad){
            if((b as LaunchPadBuild).ON() == 1)
                i++
        }
    }
    return i
}
fun Player.addplayervoted(amount: Int){
    return playervoted.put(uuid(),playervoted.get(uuid())+amount)
}
fun Player.adddestory(amount: Int){
    return destory.put(uuid(),destory.get(uuid())+amount)
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
fun Player.addplayerchance(amount: Int){
    return playerchance.put(uuid(),playerchance.get(uuid())-playerchance.get(uuid())+amount)
}
fun Player.addplayernumber(amount: Int){
    return playernumber.put(uuid(),playernumber.get(uuid())-playernumber.get(uuid())+amount)
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
fun Player.getdestory():Int {
    return destory.get(uuid())
}
fun Player.getplayerchance():Int {
    return playerchance.get(uuid())
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
fun Player.setplayer(){
    return getplayer.put(uuid(),getplayer.get(uuid()) - getplayer.get(uuid()))
}
fun Player.removeplayerchance(amount: Int){
    return playerchance.put(uuid(),playerchance.get(uuid()) - amount)
}
fun Player.removedestory(amount: Int){
    return destory.put(uuid(),destory.get(uuid()) - amount)
}
fun Player.removeplayernumber(amount: Int){
    return playernumber.put(uuid(),playernumber.get(uuid()) - amount)
}
fun Player.removekills(amount: Int){
    return kills.put(uuid(),kills.get(uuid()) - amount)
}
"""py:guapi
    qq群:411102215
    感谢wz，与klp
""".trimMargin()
//玩家队伍系统
data class ChangeTeamEvent(val player: Player, var team: Team) : Event {
    override val handler: Event.Handler get() = Companion
    companion object : Event.Handler()
}
onEnable {
    val backup = netServer.assigner
    netServer.assigner = NetServer.TeamAssigner { p,g ->
        ChangeTeamEvent(p, randomTeam(p,g)).emit().team.also {
            teams[p.uuid()] = it
        }
    }
    onDisable { netServer.assigner = backup }
}
fun ChangeTeam(p: Player, team: Team = randomTeam(p)) {
    val newTeam = ChangeTeamEvent(p, team).emit().team
    teams[p.uuid()] = newTeam
    p.team(newTeam)
}
val allTeam: Set<Team>
    get() = state.teams.getActive().mapTo(mutableSetOf()) { it.team }.apply {
    }
fun randomTeam(player: Player, group: Iterable<Player> = Groups.player): Team {
    val allTeam = allTeam
    if (teams[player.uuid()]?.run { this != spectateTeam && this !in allTeam } == true)
        teams.remove(player.uuid())
    return allTeam.shuffled()
        .minByOrNull { group.count { p -> p.team() == it && player != p } }
        ?: state.rules.defaultTeam
}

//玩家投票ui
fun playerui(amount: Int, x :Float, y :Float){
    if (amount <= 0) return
    var playervoted = amount
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        while(true){
            if(!VoteStart) break
            val units = buildList {
                Units.nearby(null,dropX,dropY,999f){
                    add(it)
                }
            }
            units.forEach{
                Call.label(
                    it.player?.con ?: return@forEach, "[green]${playervoted}", 0.42f,
                    dropX, dropY)
            }
        }
    }
}

//玩家死亡ui
fun playerdead(x :Float, y :Float,number :Int){
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        while(true){
            delay(200)
            if (Time.millis() - dorpTime >= 500_000) break
            val units = buildList {
                Units.nearby(null, dropX, dropY, 54 * 8f) {
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

//玩家任务
fun Player.playertask(x :Float, y :Float,task: LaunchPadBuild){
    val dropX = x
    val dropY = y
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        var i = 0
        while(i<=playercount){
            i--
            delay(200)
            if (Time.millis() - dorpTime >= 1_000) break
            if(task.ON() == 0) {
                if(task.ZT() == 1)
                    Call.label(con ?: return@launch, "[green]任务点正在修复", 0.21f,
                        dropX, dropY
                    )
                Call.label(con ?: return@launch, "[green]任务点被破坏", 0.21f,
                    dropX, dropY
                )

            }
            else{
                if(task.ZT() == 1)
                    Call.label(con ?: return@launch, "[green]任务点正在破坏", 0.21f,
                        dropX, dropY
                    )
                Call.label(con ?: return@launch, "[green]任务点完整", 0.21f,
                    dropX, dropY
                )
            }
        }
    }
}

//玩家修复系统
suspend fun Player.xiufuMenu(Launchpad: LaunchPadBuild){
    menu.sendMenuBuilder<Unit>(
        this,30_000, "[red]是否开始修复在此期间将无法移动",
        """
            注意你的周围
        """.trimIndent()
    ){
        if(Launchpad.ZT() != 1) {
            if (Launchpad.ON() <= 0) {
                this += listOf(
                    "[green]确定" to {
                        unit().apply(StatusEffects.unmoving, 1000f)
                        val lastTime = Time.millis()
                        Launchpad.addZT(1)
                        while (true) {
                            if (Time.millis() - lastTime >= 10_000) {
                                Launchpad.addON(1)
                                for (allplayer in Groups.player) {
                                    allplayer.sendMessage("位于${Launchpad.x},${Launchpad.x}的任务点被修复")
                                }
                                Launchpad.removeZT(1)
                                break
                            }
                            delay(100)
                        }
                    }, "[red]否" to {
                    })
            } else {
                this += listOf(
                    "这个不需要修复" to {
                    }
                )
            }
        } else{
            this += listOf(
                "这个正在修复" to {
                }
            )
        }
    }
}

//猎杀者破坏系统
suspend fun Player.destoryMenu(Launchpad: LaunchPadBuild) {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]",
        """
            花费3s破坏这个
        """.trimIndent()
    ) {
        if(Launchpad.ZT() != 1) {
            if (Launchpad.ON() == 1) {
                this += listOf(
                    "[green]确定" to {
                        unit().apply(StatusEffects.unmoving, 300f)
                        val lastTime = Time.millis()
                        adddestory(1)
                        destorydelay()
                        Launchpad.addZT(1)
                        while (true) {
                            if (Time.millis() - lastTime >= 3_000) {
                                Launchpad.removeON(1)
                                for (allplayer in Groups.player) {
                                    allplayer.sendMessage("位于${Launchpad.x},${Launchpad.x}的任务点被破坏")
                                }
                                Launchpad.removeZT(1)
                                break
                            }
                            delay(100)
                        }
                        delay(100)
                    }, "[red]否" to {
                    }
                )
            } else {
                this += listOf(
                    "这个不需要破坏" to {
                    }
                )
            }
        }else{
            this += listOf(
                "这个正在破坏" to {
                }
            )
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

//提示ui
suspend fun Player.voteMenu() {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]注意:你每局只有一次机会",
        """
           请在3思考你确定报警
        """.trimIndent()
    ){
        add(listOf(
            "[green]确定" to {
                Start = true
                removeplayerchance(1)
            }, "[red]否" to {
            }
        ))
    }
}

//playerdestorydelay
fun Player.destorydelay(){
    launch(Dispatchers.game) {
        val lastTime = Time.millis()
        if (getdestory() >= 1) {
            delay(100)
            sendMessage("[red]猎杀破坏技能进入冷却80s后冷却完毕")
            while (true) {
                if (Time.millis() - lastTime >= 40_000) {
                    adddestory(-1)
                    sendMessage("[green]猎杀者破坏技能冷却完毕")
                    return@launch
                }
                delay(100)
            }
        }
        delay(100)
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
                if (Time.millis() - lastTime >= 40_000) {
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
        for(allplayer in Groups.player.toList()){
            allplayer.addplayervote(1)
        }
        while(true){
            for (allplayer in Groups.player) {
                if(VoteStart == false) return@launch
                allplayer.unit().set((((Math.sin(((360/Groups.player.count())*allplayer.getplayernumber()).toDouble())*10f)+200f)*8).toFloat()
                    ,(((Math.cos(((360/Groups.player.count())*allplayer.getplayernumber()).toDouble())*10f)+200f)*8).toFloat())
                allplayer.unit().apply(StatusEffects.unmoving,500f)
                delay(20)
            }
        }
    }
}

//vote stop
fun votestop(){
    launch(Dispatchers.game){
        Groups.player.forEach { allplayer->
            if(allplayer.checkplayervotes(1)) {
                allplayer.removeplayervotes(1)
                allplayer.sendMessage("[red]投票结束")
            }
        }
    }
}


//badplayer destory
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    badplayer.forEach{bad->
        if((player == bad)
            &&(bad.within(it.tile.worldx(),it.tile.worldy(),100f))
            &&it.tile.block() is LaunchPad){
                launch{player.destoryMenu(it.tile.build as LaunchPadBuild)}
                return@listen
        }
    }
}

//逃生者装置修复系统
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    goodplayer.forEach { good ->
        if ((player == good)
            && (good.within(it.tile.worldx(), it.tile.worldy(), 100f))
            && it.tile.block() is LaunchPad) {
            launch{player.xiufuMenu(it.tile.build as LaunchPadBuild)}
            return@listen
        }
    }
}

//弃票
listen<EventType.TapEvent>{
    val player = it.player
    if (player.dead()) return@listen
    if ((it.tile.block() is CoreBlock)
        &&player.getplayervotes() == 1) {
        player.removeplayervotes(1)
        player.sendMessage("[green]你已经弃票")
        Groups.player.forEach { allplayer->
            allplayer.sendMessage("玩家${player.getplayernumber()}已经弃票")
        }
        return@listen
    }
}

//玩家报警
listen<EventType.TapEvent>{
val player = it.player
if (player.dead()) return@listen
if ((player.unit().within(it.tile.worldx(), it.tile.worldy(), player.unit().hitSize * 3f))
    &&(player.getplayerchance() == 1)){
    launch(Dispatchers.game){player.voteMenu()}
    }
}

//投票
listen<EventType.TapEvent> {
    val player = it.player
    if (player.dead()) return@listen
    if (player.checkplayervotes(1)) {
        for (otherplayer in Groups.player.toList()) {
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

//猎杀者杀人ui
suspend fun Player.badPlayermenu(number: Int,otherplayer: Player){
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "[red]注意:选择无法更改",
        """
            你可更换的玩家编号为${number}
            你可以通过伪造自己身份和尸体信息来迷惑其他人
            你有80s的杀人冷却注意
        """.trimIndent()
    ) {
        add(listOf(
            "[green]确定" to {
                Groups.player.forEach{
                    playerdead(otherplayer.x(),otherplayer.y(),getplayernumber())
                }
                removeplayernumber(getplayernumber())
                addplayernumber(number)
            },
            "[green]否" to {
                Groups.player.forEach {
                    playerdead(otherplayer.x(),otherplayer.y(),number)
                }
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
            for(otherplayer in Groups.player.toList()){
                if(otherplayer.unit().within(player.mouseX(),player.mouseY(),10f)
                    &&player != otherplayer
                    &&otherplayer.unit().within(player.unit().x,player.unit().y,100f)) {
                    if(otherplayer.unit().within(200f*8f,200f*8f,200f)){
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
                    launch(Dispatchers.game){player.badPlayermenu(number,otherplayer)}
                    otherplayer.removeplayernumber(otherplayer.getplayernumber())//玩家死亡移除编号
                    player.addkills(1)//猎杀者移除一次杀人机会
                    player.killdelay()
                    if(otherplayer in badplayer)
                        badplayer.remove(otherplayer)
                    else
                        goodplayer.remove(otherplayer)
                    otherplayer.clearUnit()
                    ChangeTeam(otherplayer, Team.get(255))
                    return@listen
                }
            }
        }
    }
}

//踢人系统
fun playerban() {
    launch(Dispatchers.game) {
        for (allplayer in Groups.player.toList()) {
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
            Groups.player.forEach { it.sendMessage("多人票数相同投票结束") }
            return@launch
        }
        for (allplayer in Groups.player.toList()) {
            if (allplayer.getplayervoted() == largest) {
                allplayer.removeplayer(1)
                for(otherplayer in Groups.player.toList()){
                    otherplayer.sendMessage("[green]${allplayer.name}被票出")
                }
                allplayer.removeplayervoted(allplayer.getplayervoted())
                if(allplayer in badplayer)
                    badplayer.remove(allplayer)
                else
                    goodplayer.remove(allplayer)
                allplayer.clearUnit()
                ChangeTeam(allplayer, Team.get(255))
            }
        }
    }
}

//玩家匿名系统加反重生
fun Player.updateName() {
    name = "[green]玩家${this.getplayernumber()}".with(
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
        loop(Dispatchers.game){
            delay(5)
            Groups.player.forEach { it.updateName() }
            for(allplayer in Groups.player.toList()){
                allplayer.unit().apply(StatusEffects.disarmed, 1000f)
                if(allplayer.getplayers() < 0 && allplayer.team() != Team.get(255)) {
                    allplayer.clearUnit()
                    ChangeTeam(allplayer, Team.get(255))
                    allplayer.clearUnit()
                }
            }
        }
    }
}

//playertask used
onEnable{
    launch{
        loop(Dispatchers.game){
            delay(700)
            for (b in Groups.build) {
                if(b.block() == Blocks.launchPad){
                    Groups.player.each { allplayer->
                        allplayer.playertask(b.x,b.y,b as LaunchPadBuild)
                    }
                }
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
            delay(30000)
            while (true) {
                Groups.player.forEach {allplayer->
                    Call.setHudText(allplayer.con, "[yellow]点击自己报警以开始快速投票\n[green]距离下一次自动投票开始还有:${(400 * 1000 - (Time.millis() - lastTime)) / 1000}/s")
                    for(allplayer in Groups.player) {
                        if(allplayer.team() != Team.get(255))
                        ChangeTeam(allplayer, Team.get(allplayer.getplayernumber() + 2))
                    }
                }
                delay(100)
                if(Time.millis()-lastTime>=400_000||Start == true) {
                    VoteStart = true
                    voteStrat()
                    Groups.player.forEach { allplayer ->
                        launch{ allplayer.startvoteMenu() }
                    }
                    delay(100)
                    val lastTime = Time.millis()
                    while (true) {
                        Groups.player.forEach {allplayer->
                            Call.setHudText(allplayer.con, "[yellow]点击核心弃票\n[red]距离投票结束还有:${(80 * 1000 - (Time.millis() - lastTime)) / 1000}/s")
                            ChangeTeam(allplayer,Team.get(1))
                        }
                        var i = 0
                        for(allplayer in Groups.player){
                            if(allplayer.getplayervotes() == 0) {
                                i++
                                if(i >= Groups.player.toList().count()) {
                                    for (allplayer in Groups.player) {
                                        allplayer.sendMessage("[yellow]全体玩家投票投票快速结束")
                                    }
                                    Start = false
                                    VoteStart = false
                                    votestop()
                                    playerban()
                                    delay(100)
                                    return@loop
                                }
                            }
                        }
                        if (Time.millis() - lastTime >= 80_000) {
                            Start = false
                            VoteStart = false
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
        Groups.player.forEach { allplayer->
            allplayer.sendMessage("[red]游戏即将开始")
        }
        delay(30000)
        var i = 1
        val lastTime =  Time.millis()
        loop(Dispatchers.game) {
            var f = 1
            while (true) {
                for(allplayer in Groups.player.toList()){
                    allplayer.addplayernumber(f)
                    f++
                }
                Groups.player.forEach { allplayer ->
                    val badplayercount = (Groups.player.toList().count() / 4).toInt()
                    while (i <= badplayercount) {
                        i++
                        badplayer.add(allplayer)
                        allplayer.sendMessage("[red]你是猎杀(ma)者需要杀光所有人")
                        allplayer.addplayerchance(1)
                        return@forEach
                    }
                    goodplayer.add(allplayer)
                    allplayer.sendMessage("[green]你是逃生者完成任务逃离这里")
                    allplayer.addplayerchance(1)
                }
                while (true) {
                    delay(100)
                }
            }
        }
    }
}


//暂时抛弃
//选择开启
//反新加玩家

listen<EventType.PlayerJoin>{
    val player = it.player
    player.sendMessage("你为后来加入者无法参加这次游戏")
    player.removeplayer(1)
    ChangeTeam(player, Team.get(255))
}



onEnable {
    val filter = Administration.ChatFilter { player, msg ->
        if (player.team() == Team.get(255)) {
            player.sendMessage("[yellow]观察者不能发言你已被禁言")
            for(player255 in Groups.player){
                if(player255.team() == Team.get(255))
                    player255.sendMessage(msg)
            }
            null
        } else msg
    }
    netServer.admins.chatFilters.add(filter)
    onDisable {
        netServer.admins.chatFilters.remove(filter)
    }
}
onEnable {
    contextScript<coreMindustry.UtilMapRule>().apply {
        registerMapRule((Blocks.coreFoundation as CoreBlock)::unitType) { UnitTypes.merui }
        var unitType = UnitTypes.merui
        registerMapRule(unitType::fogRadius) { 0f }
    }
}
onEnable{
    launch{
        delay(30000)
        val lastTime = Time.millis()
        loop(Dispatchers.game) {
            delay(10)
            if(Stop) return@loop
            Groups.player.each { p ->
                var text = ""
                text += buildString {
                    val unit = p.unit() ?: return@buildString
                    if (badplayer.count() <= goodplayer.count()) {
                        if (badplayer.count() != 0) {
                            if (p in badplayer) {
                                appendLine("[red]你是猎杀者")
                                appendLine("小心别被盯上哦，你在核心附件无法杀人因为哪里是安全区")
                                appendLine("你的杀人和破坏都有冷却哦！")
                                appendLine("你可以替换掉你杀死的玩家的编号和队伍")
                            } else {
                                appendLine("[green]你是逃生者")
                                appendLine("你的任务很简单揪出内鬼，修复装置")
                            }
                            appendLine("[yellow]你拥有:${p.getplayerchance()}次紧急投票机会")
                            appendLine("[yellow]你已经获得:${p.getplayervoted()}票")
                            appendLine("[yellow]你拥有:${p.getplayervotes()}票")
                            appendLine("[yellow]飞船完整度:${taskcount()}/8")
                            if ((taskcount() / 8f)<= 0.5f) {
                                appendLine("[red]飞船完整度低于1/2将在${(200 * 1000 - (Time.millis() - lastTime)) / 1000}/s坠毁")
                                if (Time.millis() - lastTime >= 200_000) {
                                    GameOverMod()
                                }
                            }
                            else {
                                appendLine("[yellow]飞船完整度大于1/2目前良好")
                                val lastTime = Time.millis()
                            }
                        } else {
                            appendLine("[green]逃生者胜利")
                            GameOverMod()
                        }
                    } else {
                        appendLine("[red]猎杀者胜利")
                        GameOverMod()
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

fun GameOverMod(){
    launch(Dispatchers.game){
        while(true) {
            delay(2000)
            Groups.player.forEach { allplayer ->
                allplayer.sendMessage("[red]本局游戏结束感谢你的游玩\nby:guapi")
            }
            VoteStart = false
            Start = false
            for(allplayer in Groups.player)
                allplayer.setplayer()
            var tile = Vars.world.tiles.getn(
                200,
                200
            )
            tile.setNet(Blocks.air)
            while(true){
                delay(100)
            }
        }
        delay(500)
    }
}
