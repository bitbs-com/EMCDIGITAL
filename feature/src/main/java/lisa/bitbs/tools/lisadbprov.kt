package lisa.bitbs.tools

import com.beust.klaxon.JsonArray
import kotlinx.serialization.ImplicitReflectionSerializer


@ImplicitReflectionSerializer
class lisadbprov {

    fun sp_ERP_SEC_User_Login_Check(iCompanyId: Int, sNickName: String, sPassword: String, ip: String = "", MachineName: String = "", CompanyDsc: String = "") : lisawsResult /*JsonArray<*>?*/ {

        var sql = "SELECT CompanyId, CompanyDsc AS A FROM Company ORDER BY 2 "

//        sql += "exec sp_ERP_SEC_User_Login_Check @iCompanyId = $iCompanyId ,  ";
//        sql += "@sNickName ='$sNickName' , @sPassword ='$sPassword', ";
//        sql += "@Ip = '$ip', @MachineName = '$MachineName', ";
//        sql += "@CompanyDsc ='$CompanyDsc'; ";

        return lisaws().executeSQLtoJSON(sql = sql);
    }
}