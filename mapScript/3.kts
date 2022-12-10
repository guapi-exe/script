@file:Depends("coreMindustry/utilNext", "调用菜单")
@file:Depends("coreMindustry/utilMapRule", "修改核心单位,单位属性")

package mapScript

import arc.graphics.Color
import arc.struct.ObjectIntMap
import arc.struct.ObjectMap
import arc.util.Align
import arc.util.Time
import coreLibrary.lib.ConfigBuilder.ConfigKey.Companion.toConfigValue
import coreLibrary.lib.util.loop
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import mindustry.Vars
import mindustry.content.*
import mindustry.entities.Units
import mindustry.game.EventType.UnitDrownEvent
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Iconc.*
import mindustry.gen.Player
import mindustry.type.StatusEffect
import mindustry.type.UnitType
import mindustry.world.Block
import mindustry.world.blocks.environment.Floor
import mindustry.world.blocks.power.Battery
import mindustry.world.blocks.liquid.LiquidBlock
import mindustry.world.blocks.defense.turrets.ItemTurret
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.blocks.campaign.Accelerator
import mindustry.world.blocks.campaign.LaunchPad
import mindustry.world.blocks.storage.CoreBlock
import mindustry.world.blocks.storage.CoreBlock.CoreBuild
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.random.Random

val menu = contextScript<coreMindustry.UtilNext>()


val playerMoney: ObjectIntMap<String> = ObjectIntMap()//个人贡献点
val worldx: ObjectIntMap<String> = ObjectIntMap()//
val turrettype: ObjectIntMap<String> = ObjectIntMap()//
val worldy: ObjectIntMap<String> = ObjectIntMap()//
val teams: ObjectIntMap<String> = ObjectIntMap()//
val playerturret: ObjectIntMap<String> = ObjectIntMap() //个人炮塔
val playerturretsalvo: ObjectIntMap<String> = ObjectIntMap()//salvo数量
val playerturretripple: ObjectIntMap<String> = ObjectIntMap()//ripple数量
val playerturretafflict: ObjectIntMap<String> = ObjectIntMap()//afflict数量
val playerturrettitan: ObjectIntMap<String> = ObjectIntMap()//titan数量
val cooldown: ObjectIntMap<String> = ObjectIntMap()//上交冷却
val teamMoney: ObjectMap<Team, Int> = ObjectMap()//团队资源点
val lastUnitType: ObjectMap<String, UnitType> = ObjectMap()//死前用的单位 还原所需要贡献点为单位容量*1.5

val copperToCoreHealth by lazy { Vars.state.rules.tags.getInt("@copperToCoreHealth", 8) }
val copperToUnitHealth by lazy { Vars.state.rules.tags.getInt("@copperToUnitHealth", 4) }
val oreGrowSpeed by lazy { Vars.state.rules.tags.getInt("@oreGrowSpeed", 16) }
val oreCostIncreaseTime = Vars.state.rules.tags.getInt("@oreCostIncreaseTime", 480) * 1000f

val T1ItemCap by lazy { Vars.state.rules.tags.getInt("@T1ItemCap", 60) }
val T2ItemCap by lazy { Vars.state.rules.tags.getInt("@T2ItemCap", 60) }
val T3ItemCap by lazy { Vars.state.rules.tags.getInt("@T3ItemCap", 120) }
val T4ItemCap by lazy { Vars.state.rules.tags.getInt("@T4ItemCap", 480) }
val T5ItemCap by lazy { Vars.state.rules.tags.getInt("@T5ItemCap", 1440) }
val T6ItemCap by lazy { Vars.state.rules.tags.getInt("@T6ItemCap", 2880) }

val startTeamMoney by lazy { Vars.state.rules.tags.getInt("@startTeamMoney", 250) }
val T2BaseNeed by lazy { Vars.state.rules.tags.getInt("@T2BaseNeed", 2400) }
val T3BaseNeed by lazy { Vars.state.rules.tags.getInt("@T3BaseNeed", 7200) }
val T4BaseNeed by lazy { Vars.state.rules.tags.getInt("@T4BaseNeed", 16800) }
val MaxBaseNeed by lazy { Vars.state.rules.tags.getInt("@MaxBaseNeed", 32000) }

val missile = arrayOf(
    UnitTypes.anthicus.weapons.get(0).bullet.spawnUnit,
    UnitTypes.quell.weapons.get(0).bullet.spawnUnit,
    UnitTypes.disrupt.weapons.get(0).bullet.spawnUnit
)
val Tier1units = arrayOf(
    UnitTypes.merui
)
val Tier2units = arrayOf(
    UnitTypes.atrax,
    UnitTypes.mace
)
val Tier3units = arrayOf(
    UnitTypes.cleroi,
    UnitTypes.spiroct,
    UnitTypes.locus
)
val Tier4units = arrayOf(
    UnitTypes.anthicus,
    UnitTypes.precept,
    UnitTypes.fortress
)
val Tier5units = arrayOf(
    UnitTypes.scepter,
    UnitTypes.vanquish,
    UnitTypes.tecta
)
val Tier6units = arrayOf(
    UnitTypes.reign,
    UnitTypes.corvus,
    UnitTypes.conquer,
    UnitTypes.collaris
)

val startTime by lazy { Time.millis() }

fun Player.checkCooldown(): Boolean{
    return cooldown.get(uuid()) <= Time.timeSinceMillis(startTime)
}
fun Player.setCooldown(time: Float){
    cooldown.put(uuid(), (Time.timeSinceMillis(startTime) + time * 1000).toInt())
}
fun Player.addturretsalvo(amount: Int){
    return playerturretsalvo.put(uuid(),playerturretsalvo.get(uuid())+amount)
}
fun Player.addturretripple(amount: Int){
    return playerturretripple.put(uuid(),playerturretripple.get(uuid())+amount)
}
fun Player.addturretafflict(amount: Int){
    return playerturretafflict.put(uuid(),playerturretafflict.get(uuid())+amount)
}
fun Player.addturrettitan(amount: Int){
    return playerturrettitan.put(uuid(),playerturrettitan.get(uuid())+amount)
}
fun Player.addMoney(amount: Int){
    if (amount <= 0) return
    setCooldown((Math.pow(log10(amount.toDouble()), 4.00) / 2).toFloat())
    playerMoney.put(uuid(), playerMoney.get(uuid()) + amount)
    teamMoney.put(team(), teamMoney.get(team()) + amount)
    if (teamMoney.get(team()) <= MaxBaseNeed) return
    teamMoney.put(team(), MaxBaseNeed)
    playerMoney.put(uuid(), playerMoney.get(uuid()) + amount)
}
fun Player.removeMoney(amount: Int){
    playerMoney.put(uuid(),playerMoney.get(uuid()) - amount)
}
fun Player.removeturretsalvo(amount: Int){
    playerturretsalvo.put(uuid(),playerturretsalvo.get(uuid()) - amount)
}
fun Player.removeturretripple(amount: Int){
    playerturretripple.put(uuid(),playerturretripple.get(uuid()) - amount)
}
fun Player.removeturretafflict(amount: Int){
    playerturretafflict.put(uuid(),playerturretafflict.get(uuid()) - amount)
}
fun Player.removeturrettitan(amount: Int){
    playerturrettitan.put(uuid(),playerturrettitan.get(uuid()) - amount)
}
fun Player.getMoney(): Int {
    return playerMoney.get(uuid())
}
fun Player.getturretsalvo(): Int {
    return playerturretsalvo.get(uuid())
}
fun Player.getturretripple(): Int {
    return playerturretripple.get(uuid())
}fun Player.getturretafflict(): Int {
    return playerturretafflict.get(uuid())
}
fun Player.getturrettitan(): Int {
    return playerturrettitan.get(uuid())
}
fun Player.checkMoney(amount: Int): Boolean {
    return playerMoney.get(uuid()) >= amount
}
fun Player.checkturretsalvo(amount: Int): Boolean {
    return playerturretsalvo.get(uuid()) >= amount
}
fun Player.checkturretripple(amount: Int): Boolean {
    return playerturretripple.get(uuid()) >= amount
}
fun Player.checkturretafflict(amount: Int): Boolean {
    return playerturretafflict.get(uuid()) >= amount
}
fun Player.checkturrettitan(amount: Int): Boolean {
    return playerturrettitan.get(uuid()) >= amount
}

fun mindustry.gen.Unit.maxShield(): Float{
    if (type == UnitTypes.mega) return (health * 1.5f).coerceAtMost(maxHealth * 1.5f)
    return (health * 2.5f).coerceAtMost(maxHealth * 2.5f)
}

fun itemDrop(amount: Int, x :Float, y :Float,maxRange: Float = 16f){
    if (amount <= 0) return
    var dorpAmount = amount
    val dropX = x - maxRange / 2 + Random.nextFloat() * maxRange
    val dropY = y - maxRange / 2 + Random.nextFloat() * maxRange
    val dorpTime = Time.millis()
    launch(Dispatchers.game){
        while(true){
            delay(200)
            if (Time.millis() - dorpTime >= 30_000) break
            val units = buildList {
                Units.nearby(null, dropX, dropY, 54 * 8f) {
                    add(it)
                }
            }
            if (units.isEmpty()) continue
            units.forEach{
                if (it.within(dropX, dropY, it.hitSize + 4) && it.stack.amount < it.itemCapacity()){
                    if (it.stack.amount + dorpAmount > it.itemCapacity()) {
                        dorpAmount -= it.itemCapacity() - it.stack.amount
                        it.addItem(Items.copper, it.itemCapacity())
                    }else{
                        it.addItem(Items.copper,dorpAmount)
                        return@launch
                    }

                }
                Call.label(
                    it.player?.con ?: return@forEach, "$itemCopper * $dorpAmount\n[lightgray]${((Time.millis() - dorpTime) / 1000)}s/30s", 0.21f,
                    dropX, dropY
                )
            }
        }
    }
}


fun Player.upgrade(u: UnitType){
    val unit = unit()
    unit(u.spawn(team(), unit).apply {
        spawnedByCore = true
        if (!(u in Tier6units)) {
            if (type != UnitTypes.horizon && type != UnitTypes.locus && type != UnitTypes.precept && type != UnitTypes.vanquish && type != UnitTypes.conquer) {
                apply(StatusEffects.freezing, Float.MAX_VALUE)
            }
            apply(StatusEffects.electrified, Float.MAX_VALUE)
            apply(StatusEffects.sapped, Float.MAX_VALUE)
            if (type == UnitTypes.mega || type == UnitTypes.obviate)
                apply(StatusEffects.muddy, Float.MAX_VALUE)
        }
        apply(StatusEffects.invincible, 4f * 60)
        shield = maxShield()
    })
    if (!(u in Tier6units)) lastUnitType.put(uuid(),u)
}

fun CoreBuild.upgrade(type: Block, text: String = ""){
    Vars.world.tile(Vars.world.width() - 3,Vars.world.height() - 3).setNet(Blocks.coreNucleus, team, 0)//临时核心保存核心资源
    tile.setNet(type, team, 0)
    Vars.world.tile(Vars.world.width() - 3,Vars.world.height() - 3).setNet(Blocks.air)
    broadcast("$text".with("text" to text), quite = true)
}

suspend fun Player.upgradeMenu() {
    if (lastUnitType.get(uuid()) != null && unit().type == UnitTypes.flare && lastUnitType.get(uuid()) != UnitTypes.flare){
        menu.sendMenuBuilder<Unit>(
            this, 30_000, "是否还原${lastUnitType.get(uuid()).emoji()}？",
            """
            [cyan]你之前的单位${lastUnitType.get(uuid()).emoji()}虽然已经阵亡 但是我们依然可以还原他
            [lightgray]还原死前用的单位所需要贡献点为那个单位容量*0.5(无法还原t6)
            你的贡献点：${getMoney()}
            预计消耗${(lastUnitType.get(uuid()).itemCapacity * 0.5).toInt()}
            [red]如果贡献点无法还原,那么你依然可以还原,但下次死亡你将无法再次还原此单位
            [lightgray]点了否也无法还原哦
        """.trimIndent()
        ) {
            add(listOf(
                "[green]是" to {
                    upgrade(lastUnitType.get(uuid()))
                    if (checkMoney((lastUnitType.get(uuid()).itemCapacity * 0.5).toInt()))
                        removeMoney((lastUnitType.get(uuid()).itemCapacity * 0.5).toInt())
                    else
                        lastUnitType.put(uuid(),UnitTypes.flare)
                },
                "[red]否" to {
                    lastUnitType.put(uuid(),UnitTypes.flare)
                    upgradeMenu()
                }
            ))
        }
    }else{
        menu.sendMenuBuilder<Unit>(
            this, 30_000, "升级菜单",
            """
            [cyan]单位物品容量满即可升级
            [lightgray]你需要${this.unit().stack.amount}/${this.unit().itemCapacity()}[white]$itemCopper[lightgray]来升级
            [cyan]队伍资源点：${teamMoney.get(team())}  个人贡献点：${getMoney()}
            [yellow]靠近核心即可上交资源 上交资源增加队伍资源点和个人贡献点
            [lightgray]还原死前用的单位所需要贡献点为那个单位容量*1.5
        """.trimIndent()
        ) {
            fun addUnitUpgrade(type: UnitType, needTeamMoney: Int = 0) {
                if (unit().stack().amount < unit().itemCapacity() || teamMoney.get(team()) < needTeamMoney) return
                val name = "升级为 ${type.emoji()}"
                add(listOf(name to suspend {
                    if (unit().stack().amount < unit().itemCapacity())//打开菜单后可能又会不满足条件
                        sendMessage("[red]单位背包资源不足")
                    else if (teamMoney.get(team()) < needTeamMoney)
                        sendMessage("[red]核心资源点不足")
                    else upgrade(type)
                }))
            }
            when(unit().type){
                //T1 -> T2
                UnitTypes.merui -> {
                    addUnitUpgrade(UnitTypes.mace)
                    addUnitUpgrade(UnitTypes.atrax)
                }
                //T2 -> T3
                UnitTypes.mace -> {
                    addUnitUpgrade(UnitTypes.cleroi)
                    addUnitUpgrade(UnitTypes.locus)
                }
                UnitTypes.atrax -> {
                    addUnitUpgrade(UnitTypes.spiroct)
                    addUnitUpgrade(UnitTypes.spiroct)
                }
                //T3 -> T4
                UnitTypes.spiroct -> {
                    addUnitUpgrade(UnitTypes.fortress, T2BaseNeed)
                    addUnitUpgrade(UnitTypes.anthicus, T2BaseNeed)
                }
                UnitTypes.cleroi -> {
                    addUnitUpgrade(UnitTypes.fortress, T2BaseNeed)
                    addUnitUpgrade(UnitTypes.anthicus, T2BaseNeed)
                }
                UnitTypes.locus -> {
                    addUnitUpgrade(UnitTypes.precept, T2BaseNeed)
                    addUnitUpgrade(UnitTypes.anthicus, T2BaseNeed)
                }
                //T4 -> T5
                UnitTypes.fortress-> {
                    addUnitUpgrade(UnitTypes.scepter, T3BaseNeed)
                    addUnitUpgrade(UnitTypes.vanquish, T3BaseNeed)
                }
                UnitTypes.anthicus -> {
                    addUnitUpgrade(UnitTypes.scepter, T3BaseNeed)
                    addUnitUpgrade(UnitTypes.vanquish, T3BaseNeed)
                }
                UnitTypes.precept -> {
                    addUnitUpgrade(UnitTypes.tecta, T3BaseNeed)
                    addUnitUpgrade(UnitTypes.vanquish, T3BaseNeed)
                }

                //T5 -> T6
                UnitTypes.reign -> {
                    addUnitUpgrade(UnitTypes.collaris, T4BaseNeed)
                    addUnitUpgrade(UnitTypes.conquer, T4BaseNeed)
                }
                UnitTypes.vanquish -> {
                    addUnitUpgrade(UnitTypes.reign, T4BaseNeed)
                    addUnitUpgrade(UnitTypes.conquer, T4BaseNeed)
                }
                UnitTypes.tecta -> {
                    addUnitUpgrade(UnitTypes.corvus, T4BaseNeed)
                    addUnitUpgrade(UnitTypes.collaris, T4BaseNeed)
                }
            }
            if (checkCooldown()){
                if (unit().stack.amount != 0 && unit().within(core().x,core().y,Vars.itemTransferRange)) add(listOf(
                    "上交全部资源${unit().stack.amount}" to {
                        if (unit().stack.amount != 0)
                            addMoney(unit().stack.amount)
                        unit().stack.amount = 0
                    },
                    "上交1/4资源${(unit().stack.amount / 4).toInt()}" to {
                        if (unit().stack.amount != 0)
                            addMoney((unit().stack.amount / 4).toInt())
                        unit().stack.amount -= (unit().stack.amount / 4).toInt()
                        upgradeMenu()
                    }
                )) else if (unit().stack.amount != 0) add(listOf(
                    "远程上交全部资源(50%手续费)\n向下取整\n${unit().stack.amount}->${(unit().stack.amount / 2).toInt()}" to {
                        addMoney((unit().stack.amount / 2).toInt())
                        unit().stack.amount = 0
                    },
                    "上交1/4资源(50%手续费)\n向下取整\n${(unit().stack.amount / 4).toInt()}->${(unit().stack.amount / 8).toInt()}" to {
                        addMoney((unit().stack.amount / 8).toInt())
                        unit().stack.amount -= (unit().stack.amount / 4).toInt()
                        upgradeMenu()
                    }
                ))
            }else add(listOf("[red]上交CD中!\n[white]还有${(cooldown.get(uuid()) - Time.timeSinceMillis(startTime)) / 1000}s" to {upgradeMenu()}))

            add(listOf(
                "取消" to {}
            ))
            if (admin && Groups.player.size() <= 6){
                //测试用
                add(listOf(
                    "[red]<ADMIN>装填背包" to {unit().stack.amount = unit().itemCapacity()},
                    "[red]<ADMIN>上交1k资源" to {
                        addMoney(1000)
                        setCooldown(0f)
                    }
                ))
                add(listOf(
                    "[red]<ADMIN>白做1k贡献" to {removeMoney(1000)},
                    "[red]<ADMIN>丢失1k资源点" to {teamMoney.put(team(),teamMoney.get(team()) - 1000)}
                ))
            }
        }
    }
}
suspend fun Player.buyturretMenu(){
    menu.sendMenuBuilder<Unit>(
        this,30_000,"是否放置炮塔",
        """
           [lightgray]建造炮塔需求一定费用
           你的贡献点：${getMoney()}
           团队贡献点：${teamMoney.get(unit().team())}
        """.trimIndent()
    ) {
        add(listOf(
            "[green]齐射，你拥有${getturretsalvo()}" to {
                if(!checkMoney(200))
                    sendMessage("[red]贡献点不足")
                else
                    removeMoney(200)
                    addturretsalvo(1)
            },
            "[green]浪涌，你拥有${getturretripple()}" to {
                if(!checkMoney(400))
                    sendMessage("[red]贡献点不足")
                else
                    removeMoney(400)
                    addturretripple(1)
            }
        ))
        add(listOf(
            "[green]泰坦，你拥有${getturrettitan()}" to {
                if(!checkMoney(1000))
                    sendMessage("[red]贡献点不足")
                else
                    removeMoney(1000)
                addturrettitan(1)
            },
            "[green]烟花，你拥有${getturretafflict()}" to {
                if(!checkMoney(1000))
                    sendMessage("[red]贡献点不足")
                else
                    removeMoney(1000)
                addturretafflict(1)
            }
        ))
    }
}
suspend fun Player.buildMenu(){
    menu.sendMenuBuilder<Unit>(
        this,30_000,"是否放置炮塔",
        """
           你的贡献点：${getMoney()}
           你的齐射：${getturretsalvo()}
           你的浪涌：${getturretripple()}
           你的泰坦：${getturrettitan()}
           你的烟花：${getturretafflict()}
           团队贡献点：${teamMoney.get(unit().team())}
        """.trimIndent()
    ){
        add(listOf(
            "[green]选择齐射," to {
                var tile1 = Vars.world.tiles.getn(
                    (unit().tileX()).toInt(),
                    (unit().tileY()).toInt()
                )
                if (checkturretsalvo(1)) {
                    removeturretsalvo(1)
                    tile1.setNet(Blocks.salvo,Player.create().team(), 3)
                }
            },
            "[green]选择浪涌" to {
                var tile2 = Vars.world.tiles.getn(
                    (unit().tileX()).toInt(),
                    (unit().tileY()).toInt()
                )
                if (checkturretripple(1)) {
                    removeturretripple(1)
                    tile2.setNet(Blocks.ripple,Player.create().team(), 3)
                }
            }
        ))
        add(listOf(
            "[green]选择泰坦" to {
                var tile1 = Vars.world.tiles.getn(
                    (unit().tileX()).toInt(),
                    (unit().tileY()).toInt()
                )
                if (checkturrettitan(1)) {
                    removeturrettitan(1)
                    tile1.setNet(Blocks.titan,Player.create().team(), 3)

                }
            },
            "[green]选择烟花" to {
                var tile2 = Vars.world.tiles.getn(
                    (unit().tileX()).toInt(),
                    (unit().tileY()).toInt()
                )
                if (checkturretafflict(1)) {
                    removeturretafflict(1)
                    tile2.setNet(Blocks.afflict,Player.create().team(), 3)
                }
            }
        ))
    }

}
suspend fun Player.buffMenu(){
    if (unit().type in Tier6units){
        sendMessage("[red]Tier6单位无法购买buff")
        return
    }
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "buff菜单",
        """
            [yellow]使用贡献点购买/清除 buff/debuff
            [lightgray]仅debuff可以使用未上交的矿物支付(优先使用未上交的矿物)
            [cyan]队伍资源点：${teamMoney.get(team())}  个人贡献点：${getMoney()}
            [red]Tier6单位无法购买属性
        """.trimIndent()
    ) {
        var allBuffCost = 0

        fun addEffect(effect: StatusEffect, cost: Int): Pair<String, suspend () -> Unit>? {
            return if (!unit().hasEffect(effect)) {
                allBuffCost += cost
                "添加 ${effect.emoji()}\n${cost}贡献点" to suspend {
                    if (!checkMoney(cost))
                        sendMessage("[red]贡献点不足")
                    else {
                        removeMoney(cost)
                        unit().apply(effect, Float.MAX_VALUE)
                    }
                }
            } else null
        }

        fun removeEffect(effect: StatusEffect, cost: Int): Pair<String, suspend () -> Unit>? {
            return if (unit().hasEffect(effect)) {
                allBuffCost += cost
                "移除 ${effect.emoji()}\n${cost}矿物(优先)/贡献点" to suspend {
                    if (!checkMoney(cost) && unit().stack.amount < cost)
                        sendMessage("[red]贡献点或矿物不足")
                    else {
                        if (unit().stack.amount >= cost)
                            unit().stack.amount -= cost
                        else
                            removeMoney(cost)
                        unit().unapply(effect)
                    }
                }
            } else null
        }

        //buff增减
        val debuffList = listOfNotNull(
            removeEffect(StatusEffects.electrified, (unit().itemCapacity() * 0.25f).toInt()),
            removeEffect(StatusEffects.sapped, (unit().itemCapacity() * 0.25f).toInt()),
            removeEffect(StatusEffects.freezing, (unit().itemCapacity() * 0.25f).toInt())
        )
        if (debuffList.any()) this += debuffList
        val buffList = listOfNotNull(
            addEffect(StatusEffects.overclock, (unit().itemCapacity() * 0.4f).toInt()),
            addEffect(StatusEffects.overdrive, (unit().itemCapacity() * 0.8f).toInt()),
            addEffect(StatusEffects.boss, (unit().itemCapacity() * 0.8f).toInt())
        )
        if (buffList.any()) this += buffList

        if (allBuffCost > 0) {
            add(listOf("一键购买所有buff选项\n${allBuffCost}贡献点" to suspend {
                if (!checkMoney(allBuffCost))
                    sendMessage("[red]贡献点不足")
                else {
                    removeMoney(allBuffCost)
                    unit().apply {
                        unapply(StatusEffects.electrified)
                        unapply(StatusEffects.sapped)
                        unapply(StatusEffects.freezing)
                        apply(StatusEffects.overclock, Float.MAX_VALUE)
                        apply(StatusEffects.overdrive, Float.MAX_VALUE)
                        apply(StatusEffects.boss, Float.MAX_VALUE)
                    }
                }
                Unit
            }
            ))
        }
        add(listOf(
            "取消" to {},
            "升级商店" to {upgradeMenu()}
        ))
    }
}
/*
suspend fun Player.infoMenu() {
    menu.sendMenuBuilder<Unit>(
        this, 30_000, "单位详细",
        """
            查看各个单位的属性和可升级兵种
        """.trimIndent()
    ) {
        fun showInfo(type: UnitType, upgradeType1: UnitType? = null, upgradeType2: UnitType? = null, upgradeType3: UnitType? = null): Pair<String, suspend () -> Unit> {
            return "${type.emoji()}" to suspend {
                val text = buildString{
                    appendLine("${type.emoji()}${type.localizedName}${type.emoji()}")
                    appendLine("生命值${type.health}/护甲值${type.armor}")
                    type.weapons.each {
                        appendLine(" ${it.name}")
                        appendLine("  ${it.reload / 60f}s 射击一次")
                        if (it.shootStatus != StatusEffects.none)
                            appendLine("  ${it.shootStatus.emoji()}${it.shootStatus}射击效果/${it.shootStatusDuration / 60f}s")
                        appendLine("   ${it.bullet.damage}伤害")
                        if (it.bullet.splashDamage > 0) {
                            appendLine("   ${it.bullet.splashDamage}范围伤害")
                            appendLine("   ${it.bullet.splashDamageRadius}范围伤害范围")
                        }
                        if (it.bullet.spawnUnit != null){
                            appendLine("  ${it.bullet.spawnUnit.emoji()}${it.bullet.spawnUnit.weapons.get(0).bullet.splashDamage}导弹伤害")
                            if (it.bullet.spawnUnit.weapons.get(0).bullet.status != StatusEffects.none)
                                appendLine("   ${it.bullet.spawnUnit.weapons.get(0).bullet.status.emoji()}${it.bullet.spawnUnit.weapons.get(0).bullet.status}导弹效果/${it.bullet.spawnUnit.weapons.get(0).bullet.statusDuration / 60f}s")
                        }
                        if (it.bullet.status != StatusEffects.none) {
                            appendLine("   ${it.bullet.status.emoji()}${it.bullet.status}子弹效果/${it.bullet.statusDuration / 60f}s")
                        }
                        if (it.bullet.fragBullet != null){
                            appendLine("   ${it.bullet.fragBullets}个分裂子弹")
                            appendLine("    ${it.bullet.fragBullet.damage}伤害")
                            if (it.bullet.fragBullet.splashDamage > 0) {
                                appendLine("    ${it.bullet.fragBullet.splashDamage}范围伤害")
                                appendLine("    ${it.bullet.fragBullet.splashDamageRadius}范围伤害范围")
                            }
                        }
                    }
                    if (upgradeType1 != null)
                        appendLine("${type.emoji()} -> ${upgradeType1.emoji()}")
                    if (upgradeType2 != null)
                        appendLine("${type.emoji()} -> ${upgradeType2.emoji()}")
                    if (upgradeType3 != null)
                        appendLine("${type.emoji()} -> ${upgradeType3.emoji()}")
                }
                sendMessage(text)
            }
        }
    }
}

 */


onEnable {
    contextScript<coreMindustry.UtilMapRule>().apply {
        //核心属性
        registerMapRule((Blocks.coreShard as CoreBlock)::unitType) { UnitTypes.merui }
        registerMapRule((Blocks.coreShard as CoreBlock)::health) { 8000 }
        registerMapRule((Blocks.coreFoundation as CoreBlock)::unitType) { UnitTypes.merui }
        registerMapRule((Blocks.coreFoundation as CoreBlock)::health) { 16000 }
        registerMapRule((Blocks.coreNucleus as CoreBlock)::unitType) { UnitTypes.merui }
        registerMapRule((Blocks.coreNucleus as CoreBlock)::health) { 24000 }

        //T1
        //merui
        var unitType = UnitTypes.merui
        registerMapRule(unitType::itemCapacity) { T1ItemCap }
        registerMapRule(unitType::armor) { 0f }
        registerMapRule(unitType::health) { 200f }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.corroded }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 5f * 60 }

        //T2
        //mace,atrax

        unitType = UnitTypes.mace
        registerMapRule(unitType::itemCapacity) { T2ItemCap }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.corroded }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 4f * 60 }
        registerMapRule(unitType::health) { 120f }
        registerMapRule(unitType::armor) { 0f }

        unitType = UnitTypes.atrax
        registerMapRule(unitType::itemCapacity) { T2ItemCap }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.corroded }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 4f * 60 }
        registerMapRule(unitType::health) { 120f }
        registerMapRule(unitType::armor) { 0f }


        //T3
        //cleroi,oxynoe,locus,spiroct
        unitType = UnitTypes.cleroi
        registerMapRule(unitType::itemCapacity) { T3ItemCap }
        registerMapRule(unitType::armor) { 0f }
        registerMapRule(unitType::health) { 280f }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.corroded }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 5f * 60 }

        unitType = UnitTypes.oxynoe
        registerMapRule(unitType::itemCapacity) { T3ItemCap }
        registerMapRule(unitType::health) { 280f }
        registerMapRule(unitType.weapons.get(0).bullet::collidesAir) { true }
        registerMapRule(unitType.weapons.get(0).bullet::damage) { 17f }
        registerMapRule(unitType.weapons.get(0).bullet::buildingDamageMultiplier) { 0.7f }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 1.5f * 60 }
        registerMapRule(unitType.weapons.get(2).bullet::damage) { 6f }
        registerMapRule(unitType::armor) { 0f }

        unitType = UnitTypes.locus
        registerMapRule(unitType::itemCapacity) { T3ItemCap }
        registerMapRule(unitType::health) { 400f }
        registerMapRule(unitType::armor) { 5f }
        registerMapRule(unitType.weapons.get(0)::rotateSpeed) { Float.MAX_VALUE }
        registerMapRule(unitType.weapons.get(0).bullet::damage) { 24f }

        unitType = UnitTypes.spiroct
        registerMapRule(unitType::itemCapacity) { T3ItemCap }
        registerMapRule(unitType::armor) { 0f }
        registerMapRule(unitType::health) { 200f }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.corroded }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 5f * 60 }


        //T4
        //fortress,precept,anthicus
        unitType = UnitTypes.fortress
        registerMapRule(unitType::itemCapacity) { T4ItemCap }
        registerMapRule(unitType::armor) { 0f }
        registerMapRule(unitType::health) { 550f }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.corroded }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 5f * 60 }


        unitType = UnitTypes.precept
        registerMapRule(unitType::itemCapacity) { T4ItemCap }
        registerMapRule(unitType::health) { 750f }
        registerMapRule(unitType::armor) { 7f }
        registerMapRule(unitType.weapons.get(0).bullet::damage) { 85f }
        registerMapRule(unitType.weapons.get(0)::rotateSpeed) { Float.MAX_VALUE }
        registerMapRule(unitType.weapons.get(0).bullet.fragBullet::damage) { 15f }
        registerMapRule(unitType.weapons.get(0).bullet.fragBullet::buildingDamageMultiplier) { 2f }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.sporeSlowed }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 1.6f * 60 }

        unitType = UnitTypes.anthicus
        registerMapRule(unitType::itemCapacity) { T4ItemCap }
        registerMapRule(unitType.weapons.get(0).bullet.spawnUnit::itemCapacity) { Int.MAX_VALUE }
        registerMapRule(unitType.weapons.get(0).bullet.spawnUnit.weapons.get(0).bullet::splashDamage) { 110f }
        registerMapRule(unitType.weapons.get(0).bullet.spawnUnit::lifetime) { 2.4f * 60 }
        registerMapRule(unitType.weapons.get(0).bullet.spawnUnit::health) { 60f }
        registerMapRule(unitType.weapons.get(0).bullet.spawnUnit::rotateSpeed) { 5f }
        registerMapRule(unitType::health) { 550f }
        registerMapRule(unitType::armor) { 0f }


        //T5
        //vanquish,tecta,scepter

        unitType = UnitTypes.vanquish
        registerMapRule(unitType::itemCapacity) { T5ItemCap }
        registerMapRule(unitType::armor) { 12f }
        registerMapRule(unitType::health) { 1500f }
        registerMapRule(unitType.weapons.get(0)::rotateSpeed) { Float.MAX_VALUE }
        registerMapRule(unitType.weapons.get(0).bullet.fragBullet::status) { StatusEffects.sporeSlowed }
        registerMapRule(unitType.weapons.get(0).bullet.fragBullet::statusDuration) { 2.4f * 60 }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.wet }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 4.8f * 60 }
        registerMapRule(unitType.weapons.get(1).bullet::status) { StatusEffects.shocked }
        registerMapRule(unitType.weapons.get(1).bullet::buildingDamageMultiplier) { 3f }

        unitType = UnitTypes.tecta
        registerMapRule(unitType::itemCapacity) { T5ItemCap }
        registerMapRule(unitType::armor) { 0f }
        registerMapRule(unitType::health) { 800f }
        registerMapRule(unitType.weapons.get(0)::shootStatus) { StatusEffects.slow }
        registerMapRule(unitType.weapons.get(0)::shootStatusDuration) { 2f * 60 }
        registerMapRule(unitType.weapons.get(1)::shootStatus) { StatusEffects.slow }
        registerMapRule(unitType.weapons.get(1)::shootStatusDuration) { 2f * 60 }
        registerMapRule(unitType.weapons.get(0).bullet::splashDamage) { 45f }

        unitType = UnitTypes.scepter
        registerMapRule(unitType::itemCapacity) { T5ItemCap }
        registerMapRule(unitType::armor) { 0f }
        registerMapRule(unitType::health) { 800f }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.corroded }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 5f * 60 }


        //T6
        //reiqn,corvus,conquer,collaris
        unitType = UnitTypes.reign
        registerMapRule(unitType::itemCapacity) { T6ItemCap }
        registerMapRule(unitType::armor) { 0f }
        registerMapRule(unitType::health) { 2800f }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.corroded }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 5f * 60 }

        unitType = UnitTypes.corvus
        registerMapRule(unitType::itemCapacity) { T6ItemCap }
        registerMapRule(unitType::armor) { 0f }
        registerMapRule(unitType::health) { 2800f }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.corroded }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 5f * 60 }

        unitType = UnitTypes.conquer
        registerMapRule(unitType::itemCapacity) { T6ItemCap }
        registerMapRule(unitType::armor) { 15f }
        registerMapRule(unitType::health) { 3000f }
        registerMapRule(unitType.weapons.get(0)::rotateSpeed) { Float.MAX_VALUE }
        registerMapRule(unitType.weapons.get(0).bullet::status) { StatusEffects.sporeSlowed }
        registerMapRule(unitType.weapons.get(0).bullet::statusDuration) { 4.8f * 60 }

        unitType = UnitTypes.collaris
        registerMapRule(unitType::itemCapacity) { T6ItemCap }
        registerMapRule(unitType::armor) { 0f }
        registerMapRule(unitType::health) { 2400f }
        registerMapRule(unitType.weapons.get(0)::shootStatus) { StatusEffects.unmoving }
        registerMapRule(unitType.weapons.get(0)::shootStatusDuration) { 3f * 60 }

    }
    Vars.state.rules.apply{
        unitCap = 999999
        fire = false
        bannedUnits.add(UnitTypes.flare)
        bannedUnits.add(UnitTypes.merui)
    }
    Vars.state.teams.getActive().each{
        teamMoney.put(it.team, startTeamMoney)
        launch(Dispatchers.game){
            delay(5000L)
            it.core().health += 20000f
        }
    }
    loop(Dispatchers.game){
        Groups.player.each{ p ->
            var text = ""
            text += buildString {
                val unit = p.unit() ?: return@buildString
                if (unit.maxHealth <= 1) return@buildString//玩家即使不操控任何单位 也会有一个血量上限0.5的单位给玩家操控
                appendLine("---${unit.type.emoji()}---[yellow]每个铜能恢复单位${copperToUnitHealth}点血量")
                append("[green]$add")
                repeat((unit.health / unit.maxHealth * 10).toInt()) {
                    append("[green]|")
                }
                repeat(10 - (unit.health / unit.maxHealth * 10).toInt()) {
                    append("[red]|")
                }
                append("[white]${unit.health}/${unit.maxHealth}|")
                append("[yellow]${modeSurvival}")
                repeat((unit.shield / unit.maxShield() * 10).toInt()) {
                    append("[green]|")
                }
                repeat(10 - (unit.shield / unit.maxShield() * 10).toInt()) {
                    append("[red]|")
                }
                appendLine("[white]${unit.shield}/${unit.maxShield()}")
                appendLine("$itemCopper[white]${unit.stack.amount}/${unit.itemCapacity()}[yellow]$defense[white] ${unit.stack.amount * copperToUnitHealth}/${unit.itemCapacity() * copperToUnitHealth}")
            }
            text += buildString {
                val core = p.core() ?: return@buildString
                appendLine("---${core.block.emoji()}---[yellow]每个资源点能恢复核心${copperToCoreHealth}点血量")
                append("[cyan]核心资源点贮藏:${teamMoney.get(p.team())}")
                if (p.checkMoney(1)) appendLine("   [cyan]个人贡献点:${p.getMoney()}[purple]") else appendLine("[purple]")
                when (core.block) {
                    Blocks.coreShard -> appendLine("核心等级:1(单位Tier 3) 下一级所需资源点：$T2BaseNeed")
                    Blocks.coreFoundation -> appendLine("核心等级:2(单位Tier 4) 下一级所需资源点：$T3BaseNeed")
                    Blocks.coreNucleus ->
                        if (teamMoney.get(p.team()) <= T4BaseNeed) appendLine("核心等级:3(单位Tier 5) 下一级所需资源点：$T4BaseNeed") else appendLine("核心等级:4(单位Tier 6) 已经满级")
                }
            }
            text += buildString {
                state.teams.getActive().joinToString("\n") {
                    val core = it.core() ?: return@joinToString ""
                    append("[#${it.team.color}]${it.team.name}[white]${core.block.emoji()}   ${teamMoney.get(it.team)}")
                    if (teamMoney.get(it.team) <=  T4BaseNeed) appendLine("") else appendLine("   [gold]TIER6允许升级")
                }
                append("[red]起床战争cs,使用电池购买炮塔，发射台周围可以放置炮塔，中心可以购买buff")
            }
            Call.infoPopup(
                p.con, text, 0.51f,
                Align.topLeft, 350, 0, 0, 0
            )
        }
        c
    }
    loop(Dispatchers.game) {
        val time = (Time.timeSinceMillis(startTime) / oreCostIncreaseTime).toInt()
        val amount = Random.nextInt(time + 1, time + 3)
        delay(1000)
        itemDrop(amount, 250*8f, 15*8f)
        itemDrop(amount, 250*8f, 485*8f)
        itemDrop(amount, 15*8f, 250*8f)
        itemDrop(amount, 485*8f, 250*8f)
        itemDrop(10, 250*8f, 250*8f)
        itemDrop(3, 250*8f, 375*8f)
        itemDrop(3, 250*8f, 125*8f)
        itemDrop(3, 125*8f, 250*8f)
        itemDrop(3, 375*8f, 250*8f)
        itemDrop(6, 125*8f, 375*8f)
        itemDrop(6, 375*8f, 375*8f)
        itemDrop(6, 125*8f, 125*8f)
        itemDrop(6, 375*8f, 125*8f)
        itemDrop(10, 180*8f, 250*8f)
        itemDrop(10, 320*8f, 250*8f)
        itemDrop(10, 250*8f, 180*8f)
        itemDrop(10, 250*8f, 320*8f)

        Groups.unit.each{
            //单位边界穿梭
            if (it.x / 8 >= Vars.world.width() - 1) it.set(24f, it.y)
            if (it.x / 8 <= 1) it.set(Vars.world.width() * 8f - 24, it.y)
            if (it.y / 8 >= Vars.world.height() - 1) it.set(it.x, 24f)
            if (it.y / 8 <= 1) it.set(it.x, Vars.world.height() * 8f - 24)
            //单位回血
            val use = ((it.maxHealth - it.health) / copperToUnitHealth).toInt()
                .coerceAtMost(it.stack().amount)
            if (use > 0) {
                it.stack.amount -= use
                it.health += use * copperToUnitHealth
                itemDrop(ceil(use * 0.7).toInt(), it.x, it.y, it.hitSize * 4f)
            }
            //核心附近的flare无敌
            val core = it.core() ?: return@each
            if (it.type == UnitTypes.flare && it.within(core.x, core.y, core.hitSize() * 1.5f))
                it.apply(StatusEffects.invincible, 5f * 60)
        }
        Groups.bullet.each{
            //子弹边界穿梭
            val vel = it.vel
            if (it.x / 8 >= Vars.world.width() - 1) it.set(8f,it.y)
            if (it.x / 8 <= 0) it.set(Vars.world.width() * 8f - 8,it.y)
            if (it.y / 8 >= Vars.world.height() - 1) it.set(it.x,8f)
            if (it.y / 8 <= 0) it.set(it.x,Vars.world.height() * 8f - 8)
            it.vel = vel
        }
        state.teams.getActive().each { data ->
            val core = data.core() ?: return@each
            //核心升降级
            when (core.block) {
                Blocks.coreShard ->
                    if (teamMoney.get(data.team) >= T2BaseNeed)
                        core.upgrade(Blocks.coreFoundation, "[#${data.team.color}]${data.team.name}[white]已经完成了次代核心升级")
                Blocks.coreFoundation ->
                    if (teamMoney.get(data.team) >= T3BaseNeed)
                        core.upgrade(Blocks.coreNucleus, "[#${data.team.color}]${data.team.name}[white]已经完成了终代核心升级\n" +
                                "[red]将队伍资源点填充到 $T4BaseNeed 即可开放tier6单位升级！")
                    else if (teamMoney.get(data.team) < T2BaseNeed)
                        core.upgrade(Blocks.coreShard, "[#${data.team.color}]${data.team.name}[white]的资源不足以支撑次代核心运作,降级为初代核心")
                Blocks.coreNucleus ->
                    if (teamMoney.get(data.team) < T3BaseNeed)
                        core.upgrade(Blocks.coreFoundation, "[#${data.team.color}]${data.team.name}[white]的资源不足以支撑终代核心运作,降级为次代核心")
            }
        }
        yield()
    }
    loop(Dispatchers.game){
        state.teams.getActive().each { data ->
            val core = data.core() ?: return@each
            if (core.health >= core.maxHealth - copperToCoreHealth && core.health < core.maxHealth * 1.5)
            //核心护盾
                core.health += core.maxHealth * 0.0125f
            else{
                //核心常规回血
                val use = ((core.maxHealth - core.health) / copperToCoreHealth).toInt()
                    .coerceAtMost(teamMoney.get(data.team))
                if (use > 0) {
                    teamMoney.put(data.team, teamMoney.get(data.team) - use)
                    core.health += use * copperToCoreHealth
                    itemDrop(ceil(use * 0.7).toInt(), core.x, core.y, core.hitSize() * 2)
                    Call.effect(Fx.explosion,core.x,core.y, 0f, Color.red)
                }
            }
        }
        Groups.unit.each {
            //单位护盾
            if (it.shield < it.maxShield()) {
                //中毒阻回盾
                if (!it.hasEffect(StatusEffects.corroded)) it.shield += it.maxShield() * 0.015f
                it.shield += it.maxShield() * 0.005f
            }
            if (it.shield >= it.maxShield()){
                it.shield = it.maxShield()
            }
        }
        delay(1000)
    }
    loop(Dispatchers.game){
        delay(1000)
        repeat(oreGrowSpeed){
            val tile = Vars.world.tiles.getn(
                Random.nextInt(0, world.width()),
                Random.nextInt(0, world.height())
            )
            if (tile.floor() == Blocks.stone)
                tile.setNet(Blocks.copperWall, Team.crux, 0)
        }
    }
}

listen<EventType.TapEvent> {
    val player = it.player
    if (player.dead()) return@listen
    if ((it.tile.block() is Accelerator)
        && player.unit().within(it.tile.worldx(),it.tile.worldy(),100f))
        launch(Dispatchers.game) { player.buffMenu() }
}

listen<EventType.TapEvent> {
    val player = it.player
    if (player.dead()) return@listen
    if ((it.tile.block() is Battery )
        && player.unit().within(it.tile.worldx(),it.tile.worldy(),100f))
        launch(Dispatchers.game) { player.buyturretMenu() }
}
listen<EventType.TapEvent> {
    val player = it.player
    if (player.dead()) return@listen
    if ((it.tile.block() is LaunchPad )
        && (player.unit().within(it.tile.worldx(),it.tile.worldy(),150f))
        && !(player.unit().within(it.tile.worldx(),it.tile.worldy(),70f)))
        launch(Dispatchers.game) { player.buildMenu() }
}

listen<EventType.TapEvent> {
    val player = it.player
    if (player.dead()) return@listen
    if (player.unit().within(it.tile.worldx(),it.tile.worldy(),100f)) {
        var teams = player.team()
        var tile1 = Vars.world.tiles.getn(
            (it.tile.worldx()).toInt()/8,
            (it.tile.worldy()).toInt()/8
        )
        var tile3 = Vars.world.tiles.getn(
            ((it.tile.worldx())-1).toInt()/8,
            ((it.tile.worldy())-1).toInt()/8
        )
        var tile6 = Vars.world.tiles.getn(
            ((it.tile.worldx())-1).toInt()/8,
            ((it.tile.worldy())+0).toInt()/8
        )
        var tile9 = Vars.world.tiles.getn(
            ((it.tile.worldx())-0).toInt()/8,
            ((it.tile.worldy())-1).toInt()/8
        )
        if (tile1.floor() == Blocks.tar
            && tile1.block() != Blocks.junction){
            if(player.checkMoney(1)){
                player.removeMoney(1)
                tile1.setNet(Blocks.junction, teams, 3)
                tile3.setNet(Blocks.junction, teams, 3)
                tile6.setNet(Blocks.junction, teams, 3)
                tile9.setNet(Blocks.junction, teams, 3)
            }
        }
    }
}

listen<EventType.TapEvent> {
    val player = it.player
    if (player.dead()) return@listen
    if ((it.tile.block() is CoreBlock && it.tile.team() == player.team())
        && player.unit().within(it.tile.worldx(),it.tile.worldy(),100f))

        launch(Dispatchers.game) { player.upgradeMenu() }
}
listen<EventType.UnitDestroyEvent> { u ->
    if (u.unit.type in missile)
        itemDrop(u.unit.stack.amount, u.unit.x, u.unit.y)
    else {
        var amount = u.unit.stack.amount + u.unit.itemCapacity() * 0.4f
        if (u.unit.hasEffect(StatusEffects.overclock)) amount += u.unit.itemCapacity() * 0.1f
        if (u.unit.hasEffect(StatusEffects.overdrive)) amount += u.unit.itemCapacity() * 0.2f
        if (u.unit.hasEffect(StatusEffects.boss)) amount += u.unit.itemCapacity() * 0.2f
        if (u.unit.hasEffect(StatusEffects.freezing)) amount -= u.unit.itemCapacity() * 0.1f
        if (u.unit.hasEffect(StatusEffects.sapped)) amount -= u.unit.itemCapacity() * 0.1f
        if (u.unit.hasEffect(StatusEffects.electrified)) amount -= u.unit.itemCapacity() * 0.1f
        itemDrop(amount.toInt(), u.unit.x, u.unit.y)
    }
}

