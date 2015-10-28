package com.crawler;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

/**
 * 
 * @author jituo Testcase 的执行顺序,字母顺序执行，大写字母优先. private
 *         的test方法不会调用。没有以test开始的方法不会调用。 Test开头的方法不管用。一定要小写的test
 */
public class WeichatCrawler extends UiAutomatorTestCase {

	public static final String CRAWDATA_PATH = "/sdcard/CrawlData/";
	public static final String WEICHAT_DATA_PATH = "/sdcard/tencent/MicroMsg/WeiXin/";

	private static final int CRAW_COUNT_LIMITS = 5;
	private static int sCrawlCounter = 0;

	public void testWeichatCrawler() throws UiObjectNotFoundException {
		// prepareInit();
		// 先启动模拟一个地理位置
		UiObject mockLoaction = null;
		do {
			mockLoaction = new UiObject(new UiSelector().text("获取数据"));
			crawLoaction(mockLoaction);
			sleep(1500);
		} while (mockLoaction.isEnabled());

		// 退出地区切换程序
		UiDevice.getInstance().pressBack();
		sleep(1000);
		UiObject find = new UiObject(new UiSelector().text("ok"));
		find.click();
	}

	//
	private void crawLoaction(UiObject mockLoaction) throws UiObjectNotFoundException {

		sCrawlCounter = 0;
		System.out.print("--------- begin \n");
		// 先启动模拟一个地理位置
		mockLoaction.click();// 通过点击界面按钮触发模拟
		sleep(1000);
		// 打开附近的人，发现->附近的人
		UiObject discover = new UiObject(new UiSelector().text("发现"));
		discover.click();
		sleep(500);// 中间停顿是为了模拟人的操作习惯
		UiObject nearbyList = new UiObject(new UiSelector().text("附近的人"));
		nearbyList.click();
		// 附近的人列表加载需要时间，这里等待一下
		waitForNearBylistLoadFinished();
		sleep(500);
		UiDevice.getInstance().pressBack();
		//
		UiObject nearbyList2 = new UiObject(new UiSelector().text("附近的人"));
		nearbyList2.click();
		waitForNearBylistLoadFinished();

		UiScrollable list = new UiScrollable(new UiSelector().className("android.widget.ListView"));
		// 开始遍历list,滑动一下,爬一次
		do {
			crawlOnePage();
		} while (list.scrollForward() && sCrawlCounter < CRAW_COUNT_LIMITS);
		// 退出微信
		UiDevice.getInstance().pressBack();
		sleep(300);
		UiDevice.getInstance().pressBack();
	}

	// 爬取一个页面
	private void crawlOnePage() throws UiObjectNotFoundException {
		System.out.print("--------- crawl page Started!\n");
		UiObject peopleList = new UiObject(new UiSelector().className("android.widget.ListView"));
		int count = peopleList.getChildCount();
		for (int i = 2; (i < count) && (sCrawlCounter < CRAW_COUNT_LIMITS); i++) {
			UiObject child = peopleList.getChild(new UiSelector().index(i));
			if (child.exists()) {
				sleep(600);
				crawlOnePeople(child);
			}
		}
		System.out.print("--------- crawl page end!\n");
	}

	private void crawlOnePeople(UiObject people) throws UiObjectNotFoundException {

		people.clickAndWaitForNewWindow();

		sCrawlCounter++;
		UiObject image = new UiObject(new UiSelector().description("头像"));
		// 个人信息
		JSONObject peopleDesc = new JSONObject();
		try {
			UiObject imageNeibor = image.getFromParent(new UiSelector().index(1));
			UiObject nChild = imageNeibor.getChild(new UiSelector().index(0));
			UiObject nickName = nChild.getChild(new UiSelector().index(0));
			peopleDesc.put("name", nickName.getText());
			// File dir = new File(CRAWDATA_PATH + nickName.getText());
			// if (!dir.exists()) {
			// dir.mkdir();
			// }
			// File file = new File(CRAWDATA_PATH + nickName.getText() +
			// "/PersonInfo.txt");
			// if (dir.exists()) {
			// try {
			// dir.createNewFile();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// }

			UiObject sexy = nickName.getFromParent(new UiSelector().index(1));
			if (sexy.exists())
				peopleDesc.put("sexy", sexy.getContentDescription());
			//
			UiObject list = new UiObject(new UiSelector().className("android.widget.ListView"));
			UiObject area = list.getChild(new UiSelector().index(3)).getChild(new UiSelector().index(0))
					.getChild(new UiSelector().index(0)).getChild(new UiSelector().index(1))
					.getChild(new UiSelector().index(0)).getChild(new UiSelector().index(0));
			if (area.exists())
				peopleDesc.put("area", area.getText());
			//
			UiObject personalSignature = new UiObject(new UiSelector().text("个性签名"));
			if (personalSignature.exists()) {
				UiObject signature = list.getChild(new UiSelector().index(4)).getChild(new UiSelector().index(0))
						.getChild(new UiSelector().index(0)).getChild(new UiSelector().index(1))
						.getChild(new UiSelector().index(0)).getChild(new UiSelector().index(0));
				peopleDesc.put("signature", signature.getText());
			}

			System.out.println(peopleDesc.toString());
		} catch (JSONException e) {

		}
		// FileOutputStream out;
		// try {
		// out = new FileOutputStream(file);
		// out.write(peopleDesc.getBytes());
		// out.flush();
		// out.close();
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		UiObject sayHello = new UiObject(new UiSelector().text("打招呼"));
		if (sayHello.exists()) {
			UiDevice.getInstance().pressBack();
			return;
		}
		UiObject adLogo = new UiObject(new UiSelector().textStartsWith("网页由"));
		if (adLogo.exists()) {
			UiDevice.getInstance().pressBack();
		}

	}

	// 等待附近的人list加载完毕
	private void waitForNearBylistLoadFinished() {
		UiObject loadingProgress1 = null;
		UiObject loadingProgress2 = null;
		do {
			sleep(1000);
			loadingProgress1 = new UiObject(new UiSelector().text("正在确定您的位置"));
			loadingProgress2 = new UiObject(new UiSelector().text("正在查找附近的人"));
		} while (loadingProgress1.exists() || loadingProgress2.exists());
	}

	private void prepareInit() {
		// 清空上次的爬行数据
		File dir = new File(CRAWDATA_PATH);
		if (dir.exists()) {
			try {
				deleteDir(CRAWDATA_PATH);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		dir.mkdirs();
	}

	private void deleteDir(String filepath) throws IOException {
		File f = new File(filepath);
		if (f.exists() && f.isDirectory()) {
			if (f.listFiles().length == 0) {
				f.delete();
			} else {
				File delFile[] = f.listFiles();
				int i = f.listFiles().length;
				for (int j = 0; j < i; j++) {
					if (delFile[j].isDirectory()) {
						deleteDir(delFile[j].getAbsolutePath());
					}
					delFile[j].delete();
				}
			}
		}
	}
}
