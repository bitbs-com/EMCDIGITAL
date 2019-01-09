package lisa.bitbs.tools

import java.io.FileNotFoundException
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


class lisaws {

    private var ws = "https://lisaws.bitbs.com/sqlserver.asmx/ExecuteSQLtoJSON?"

    fun executeSQLtoJSON(sql: String): String {
        var url = ws + "ServerKey=A4DEBF07-CD2A-433E-A521-EA7D2A907B00&SQL=" + URLEncoder.encode(StringBuffer().append("$sql").toString(), "UTF-8")


        try{
            if(url.toString() != "")
            {

                val connection = URL(url.toString()).openConnection() as HttpURLConnection
                connection.connect()
                println(connection.responseCode)
                println(connection.getHeaderField("Content-Type"))
                var json = connection.inputStream.use { it.reader().use { reader -> reader.readText() } }

                if(json.contains('{') && json.contains('}'))
                {
                    json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1)
                    /*
                    val jsonObj = JSONObject(jSon))
                    val foodJson = jsonObj.getJSONArray("Foods")
                    for (i in 0..foodJson!!.length() - 1) {
                        val categories = FoodCategoryObject()
                        val name = foodJson.getJSONObject(i).getString("FoodName")
                        categories.name = name
                    }
                    */
                }

                return json
            }
        }
        catch(ex: FileNotFoundException)
        {
            return ""
        }
        finally {
            return ""
        }
    }
}