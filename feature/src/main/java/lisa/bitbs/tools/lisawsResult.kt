package lisa.bitbs.tools
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import kotlinx.serialization.PrimitiveKind
import java.lang.Exception
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType


class lisawsResult( var jsonArray: JsonArray<*>?) {



    fun get(table: Int, row: Int, col: String): String {
        var result: String




        try {
            /*Valida que la Tabla solicitada exista en el arreglo*/
            if(jsonArray!!.size < table )
            {
                result = "ERROR: Tabla inexistente"
            }
            else if((((jsonArray!![table] as JsonArray<*>)[0] as JsonArray<*>)[row] as JsonObject).containsKey(col) == false )
            {
                result = "ERROR: Columna inexistente"
            }
            else
            {
                result = (((jsonArray!![table] as JsonArray<*>)[0] as JsonArray<*>)[row] as JsonObject)[col].toString()
            }
            //((json.get(0)  as JsonArray<*>).get(0) as JsonArray<*>).int("Correct_Login")[0];
        }
        catch (ex: Exception)
        {
            result = ex.message.toString()
        }

        return result
    }
}