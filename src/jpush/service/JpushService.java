package jpush.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.SMS;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import jpush.test.JpushConfig;

public class JpushService {

	protected static final Logger LOG = LoggerFactory.getLogger(JpushService.class);

	private static JPushClient jpushClient = new JPushClient(JpushConfig.MASTER_SECRET, JpushConfig.APP_KEY);

	public static void sendPush(PushPayload payload) {
		payload.resetOptionsApnsProduction(JpushConfig.APNS_PRODUCTION);
		payload.resetOptionsTimeToLive(JpushConfig.TIME_TO_LIVE);
		try {
			System.out.println(payload.toString());
			PushResult result = jpushClient.sendPush(payload);
			System.out.println("-------------------------------------");
			System.out.println(result);
			LOG.info("Got result - " + result);
		} catch (APIConnectionException e) {
			LOG.error("Connection error. Should retry later. ", e);
		} catch (APIRequestException e) {
			LOG.error("Error response from JPush server. Should review and fix it. ", e);
			LOG.info("HTTP Status: " + e.getStatus());
			LOG.info("Error Code: " + e.getErrorCode());
			LOG.info("Error Message: " + e.getErrorMessage());
			LOG.info("Msg ID: " + e.getMsgId());
		}
	}

	public static void sendAll(String alert) {
		PushPayload payload = PushPayload.alertAll(alert);
		sendPush(payload);
	}

	public static void sendAll(String alert, String title, Map<String, String> extras) {
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.all())
				.setNotification(Notification.android(alert, title, extras)).build();
		sendPush(payload);
	}

	// message：应用内消息。或者称作：自定义消息，透传消息。
	// 此部分内容不会展示到通知栏上，JPush SDK 收到消息内容后透传给 App。需要 App 自行处理。
	// iOS 平台上，此部分内容在推送应用内消息通道（非APNS）获取。Windows Phone 暂时不支持应用内消息。
	public static void sendAllMessage(String message) {
		PushPayload payload = PushPayload.messageAll(message);
		sendPush(payload);
	}

	public static void sendAndroid(String alert, String title, Map<String, String> extras) {
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.android()).setAudience(Audience.all())
				.setNotification(Notification.android(alert, title, extras)).build();
		sendPush(payload);
	}

	public static void sendIos(String alert, int badge, String sound, Map<String, String> extras) {
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.ios())
				.setAudience(
						Audience.all())
				.setNotification(Notification.newBuilder().addPlatformNotification(IosNotification.newBuilder()
						.setAlert(alert).setBadge(badge).setSound(sound).addExtras(extras).build()).build())
				.build();
		sendPush(payload);
	}

	// 快捷地构建推送对象：所有平台，所有设备，内容为 ALERT 的通知。
	public static PushPayload buildPushObject_all_all_alert(String alert) {
		return PushPayload.alertAll(alert);
	}

	// 构建推送对象：所有平台，推送目标是别名为 "alias1"，通知内容为 ALERT。
	public static PushPayload buildPushObject_all_alias_alert(String alert, String alias) {
		return PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias(alias))
				.setNotification(Notification.alert(alert)).build();
	}

	// 构建推送对象：平台是 Android，目标是 tag 为 "tag1" 的设备，内容是 Android 通知 ALERT，并且标题为 TITLE。
	public static PushPayload buildPushObject_android_tag_alertWithTitle(String alert, String alias, String title) {
		return PushPayload.newBuilder().setPlatform(Platform.android()).setAudience(Audience.tag(alias))
				.setNotification(Notification.android(alert, title, null)).build();
	}

	// 构建推送对象：平台是 iOS，推送目标是 "tag1", "tag_all" 的交集，推送内容同时包括通知与消息 - 通知信息是 ALERT，
	// 角标数字为 5，通知声音为 "happy"，并且附加字段 from = "JPush"；消息内容是 MSG_CONTENT。通知是 APNs推送通道的，
	// 消息是 JPush 应用内消息通道的。APNs 的推送环境是“生产”（如果不显式设置的话，Library 会默认指定为开发）
	public static PushPayload buildPushObject_ios_tagAnd_alertWithExtrasAndMessage(String alert, String msg_content) {
		return PushPayload.newBuilder().setPlatform(Platform.ios()).setAudience(Audience.tag_and("tag1", "tag_all"))
				.setNotification(Notification.newBuilder()
						.addPlatformNotification(IosNotification.newBuilder().setAlert(alert).setBadge(5)
								.setSound("happy").addExtra("from", "JPush").build())
						.build())
				.setMessage(Message.content(msg_content))
				.setOptions(Options.newBuilder().setApnsProduction(true).build()).build();
	}

	public static PushPayload buildPushObject_android_and_ios() {
		return PushPayload.newBuilder().setPlatform(Platform.android_ios()).setAudience(Audience.tag("tag1"))
				.setNotification(Notification.newBuilder().setAlert("alert content")
						.addPlatformNotification(AndroidNotification.newBuilder().setTitle("Android Title").build())
						.addPlatformNotification(
								IosNotification.newBuilder().incrBadge(1).addExtra("extra_key", "extra_value").build())
						.build())
				.build();
	}

	// 构建推送对象：平台是 Andorid 与 iOS，推送目标是 （"tag1" 与 "tag2" 的并集）交（"alias1" 与 "alias2"的并集），
	// 推送内容是 - 内容为 MSG_CONTENT 的消息，并且附加字段 from = JPush。
	public static PushPayload buildPushObject_ios_audienceMore_messageWithExtras(String msg_content) {
		return PushPayload.newBuilder().setPlatform(Platform.android_ios())
				.setAudience(Audience.newBuilder().addAudienceTarget(AudienceTarget.tag("tag1", "tag2"))
						.addAudienceTarget(AudienceTarget.alias("alias1", "alias2")).build())
				.setMessage(Message.newBuilder().setMsgContent(msg_content).addExtra("from", "JPush").build()).build();
	}

	// 构建推送对象：推送内容包含SMS信息
	public static PushPayload buildPushObject_all_all_alertWithSms(String alert) {
		SMS sms = SMS.content("Test SMS", 10);
		return PushPayload.alertAll(alert, sms);
	}
	
	public static PushPayload buildPushObject_all_registrationId_alert(String alert,String... registrationId) {
		return PushPayload.newBuilder().setPlatform(Platform.all())
				.setAudience(Audience.registrationId(registrationId))
				.setNotification(Notification.alert(alert))
				.build();
	}

}
