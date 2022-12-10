import coreLibrary.DBApi
import coreLibrary.lib.util.loop
import kotlinx.coroutines.delay
import mindustry.game.EventType
import mindustry.gen.Groups
import mindustry.gen.Player

fun Player.updateName() {
    name = "玩家".with(
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
        Groups.player.forEach { it.updateName() }
    }
}
