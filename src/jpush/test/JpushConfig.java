package jpush.test;

public class JpushConfig {

	public static final String APP_KEY = "74f1457156eea04954ebace9";      // 必填
	 
	public static final String MASTER_SECRET = "8b6ce7c45dc52084330fd107";// 必填，每个应用都对应一个masterSecret
		
	public static final boolean APNS_PRODUCTION = false;//是否生产环境
	
	 /* 
     * 保存离线的时长。秒为单位。最多支持10天（864000秒）。 
     * 0 表示该消息不保存离线。即：用户在线马上发出，当前不在线用户将不会收到此消息。 
     * 此参数不设置则表示默认，默认为保存1天的离线消息（86400秒 ）。
     */
	public static final long TIME_TO_LIVE = 60 * 60 * 24;
}
