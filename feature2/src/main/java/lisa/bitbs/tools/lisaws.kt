package lisa.bitbs.tools


import com.beust.klaxon.JsonArray
import com.beust.klaxon.Parser
import com.beust.klaxon.Parser.Companion.default
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Optional
import org.jetbrains.anko.getStackTraceString
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


@ImplicitReflectionSerializer
class lisaws {

    private var ws = "https://lisaws.bitbs.com/sqlserver.asmx/ExecuteSQLtoJSONMobile?"

    data class wsResponse(val key: Int, @Optional val value: String = "")
    //data class wsResponse(val index: String, val value: String) : Pair(String.javaClass, String.javaClass)

    var jsonarray: JsonArray<*>? = null

    fun executeSQLtoJSON(sql: String): lisawsResult /*JsonArray<*>?*/ {
        var url = ws + "ServerKey=A4DEBF07-CD2A-433E-A521-EA7D2A907B00&SQL=" + URLEncoder.encode(StringBuffer().append("$sql").toString(), "UTF-8")
        var json = ""

        try{
            if(url != "")
            {



                val connection = URL(url.toString()).openConnection() as HttpURLConnection
                connection.connect()
                println(connection.responseCode)
                println(connection.getHeaderField("Content-Type"))
                json = connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
                println("TOY")
                if(json.contains('{') && json.contains('}'))
                {
                    println("json0 $json")
                    json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1)
                    println("json1 $json")
                    val jsonBuilder = StringBuilder(json)

                    emptyList()
                    val parser: Parser = (this.default(pathMatchers, passedLexer, streaming))()
                    jsonarray = parser.parse(jsonBuilder) as JsonArray<*>


//                    val jsonarray: JsonArray = JSON.parse(json)
                    //val jsonarray: List<wsResponse> = mapper. .readValue<List<lisaws.wsResponse>>(content = json)

                    println(jsonarray)

//                    jsonarray.forEach {
//                        println(it)
//                    }
                }
                else
                {
                    println(json)
                }

                return lisawsResult(jsonarray)
            }
        }
        catch(ex: Exception)
        {
            println(ex.getStackTraceString())
            println(ex.message)
            return null as lisawsResult
        }
        finally {
            println(json)
            return lisawsResult(jsonarray)
        }
    }
}