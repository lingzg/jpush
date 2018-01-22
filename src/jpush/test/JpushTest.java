package jpush.test;

import jpush.service.JpushService;

public class JpushTest {

	public static void main(String[] args) {
		JpushService.sendPush(JpushService.buildPushObject_android_and_ios());
//		JpushService.sendAll("祝大家新春快乐");
	}
}
