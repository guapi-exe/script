@file:Depends("coreMindustry/contentsTweaker", "修改核心单位,单位属性")

import coreLibrary.lib.with
import coreMindustry.ContentsTweaker
import coreMindustry.lib.broadcast
import mindustry.Vars
import wayzer.MapManager
import wayzer.MapRegistry
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
        delay(4000)
        try {
            contextScript<ContentsTweaker>().addPatch(
                name,
                Vars.dataDirectory.child("scripts").child("mapScript").child("$name.json").readString()
            )
        }catch (e: Throwable){
            json.delete()
            broadcast("[red]$e".with())
        }
        broadcast ( "[green]加载完成".with() )
        json.delete()///临时文件对吧！
    }
}


