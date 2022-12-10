@file:Depends("coreMindustry")
@file:Depends("wayzer/maps", "获取地图信息")
@file:Depends("wayzer/map/mapInfo", "显示地图信息", soft = true)
@file:Import("mapScript.lib.*", defaultImport = true)

package mapScript

import arc.util.Interval
import arc.util.Log
import arc.util.serialization.Jval
import mindustry.Vars
import mindustry.gen.Groups
import java.io.File
import java.net.URL
import kotlin.system.exitProcess

val URL = "xem8k5/TheDevil-MapScripts"
val script = Vars.state.rules.tags.get("@mapScript")
val moduleId = id
var inRunning: ScriptInfo? = null
fun loadMapScript(id: String): Boolean {
    val script = ScriptManager.getScriptNullable(id)?.scriptInfo
    if (script == null) {
        launch(Dispatchers.gamePost) {
            broadcast("[red]该服务器不存在对应地图脚本，请联系管理员: {id}".with("id" to id))
        }
        return false
    }
    inRunning = script
    runBlocking {
        ScriptManager.disableScript(script,"插件更新")
        delay(3000)
        ScriptManager.enableScript(script, true)
    }
    launch(Dispatchers.gamePost) {
        if (script.enabled)
            broadcast("[yellow]加载地图特定脚本完成: {id}".with("id" to script.id))
        else
            broadcast(
                "[red]地图脚本${id}加载失败，请联系管理员: {reason}"
                    .with("id" to script.id, "reason" to script.failReason.orEmpty())
            )
    }
    return script.enabled
}

var updateCallback: (() -> Unit)? = null
suspend fun download(url: String, file: File): Int = withContext(Dispatchers.IO) {
    val steam = URL(url).openStream()
    val buffer = ByteArray(128 * 1024)//128KB
    val logInterval = Interval()
    var len = 0
    steam.use { input ->
        file.outputStream().use { output ->
            while (isActive) {
                val i = input.read(buffer)
                if (i == -1) break
                output.write(buffer, 0, i)
                len += i
                if (logInterval[60f])
                    logger.info("Downloaded ${len / 1024}KB")
            }
        }
    }
    return@withContext len
}
listen<EventType.PlayerChatEvent> {
    if (it.message.equals("更新插件", true) && it.player.admin) {
        launch {
            Groups.player.forEach { p ->
                p.sendMessage("插件准备更新")
            }
            try {
                val txt = URL("https://api.github.com/repos/$URL/releases").readText()
                val json = Jval.read(txt).asArray().first()
                val asset = json.get("assets").asArray().find {
                    it.getString("name", "").contains("$script.kts", ignoreCase = true)
                } ?: error("can't find asset")
                val url = asset.getString("browser_download_url", "")
                try {
                    update(url)
                    cancel()
                } catch (e: Throwable) {
                    logger.warning("下载更新失败: $e")
                    e.printStackTrace()
                }
            } catch (e: Throwable) {
                logger.warning("获取更新数据失败: $e")
            }
        }
    }
}

suspend fun update(url: String) {
    Log.info("正在从 $url 下载")
    val dest = File("/root/config/scripts/mapScript/$script.kts")
    val tmp = dest.resolveSibling(".kts.temp")
    Groups.player.forEach { p->
        p.sendMessage("${tmp.path}")
    }
    val size = try {
        download(url, tmp)
    } catch (e: Throwable) {
        tmp.delete()
        throw e
    }
    Log.info("下载完成: ${size / 1024}KB")
    updateCallback = {
        Groups.player.forEach {
            it.kick("[yellow]服务器更新到新版本")
        }

        Thread.sleep(100L)
        dest.outputStream().use { output ->
            tmp.inputStream().use { it.copyTo(output) }
        }
        tmp.delete()
        exitProcess(2)
    }
    val script = Vars.state.rules.tags.get("@mapScript")
    loadMapScript("mapScript/"+script)
}