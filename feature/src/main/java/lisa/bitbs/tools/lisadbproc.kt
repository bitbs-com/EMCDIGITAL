package lisa.bitbs.tools

import android.support.v4.content.res.TypedArrayUtils.getString
import com.bitbs.sandra.emcdigital.feature.R

class lisadbproc {

    fun sp_ERP_SEC_User_Login_Check(iCompanyId: Int, sNickName: String, sPassword: String, ip: String = "", MachineName: String = "", CompanyDsc: String = "") : String {

        var sql = ""

        sql += "exec sp_ERP_SEC_User_Login_Check @iCompanyId = $iCompanyId ,  ";
        sql += "@sNickName ='$sNickName' , @sPassword ='$sPassword', ";
        sql += "@Ip = '$ip', @MachineName = '$MachineName', ";
        sql += "@CompanyDsc ='$CompanyDsc'; ";

        return lisaws().executeSQLtoJSON(sql = sql);
    }
}