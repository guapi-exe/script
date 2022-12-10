@file:Depends("coreMindustry/contentsTweaker", "修改核心单位,单位属性")

import coreLibrary.lib.with
import coreMindustry.ContentsTweaker
import coreMindustry.lib.broadcast
import coreMindustry.lib.listen
import mindustry.Vars
import mindustry.game.EventType
import wayzer.MapManager
import wayzer.MapRegistry
import java.io.File
var name: String = ""

onEnable {
    launch{
        val contextScript =  Vars.state.map.description()
        val contextScriptget = contextScript.subSequence(contextScript.indexOf("{",0,true),contextScript.lastIndexOf("]"))
        val map = MapRegistry.nextMapInfo(MapManager.current)
        name = map.id.toString()
        val json = Vars.dataDirectory.child("scripts").child("mapScript").child("$name.json").file()
        if(!json.isFile){ json.createNewFile() }
        json.appendText(contextScriptget as String)
        broadcast("[green]文件写入成功".with())
        delay(6000)
        contextScript<ContentsTweaker>().addPatch(
            name,
            Vars.dataDirectory.child("scripts").child("mapScript").child("$name.json").readString()
        )
        broadcast ( "${contextScriptget}加载完成".with() )
        json.delete()
    }
}

listen<EventType.GameOverEvent> {
    val json = File("/root/config/scripts/mapScript/$name.json")
    if(!json.isFile){ json.delete() }
}

