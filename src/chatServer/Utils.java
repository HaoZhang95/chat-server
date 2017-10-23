package chatServer;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class Utils {
	public static String getTime(){
		Long timeStamp = System.currentTimeMillis();  //获取当前时间戳
        
        //SimpleDateFormat sdf=new SimpleDateFormat("yy/MM/dd HH:mm"); 
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(new Date(Long.parseLong(String.valueOf(timeStamp))));   // 时间戳转换成时间
        
        return time;
	}
}
