package com.hwh;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.WindowConstants;

import com.android.monkeyrunner.adb.AdbBackend;
import com.android.monkeyrunner.core.IMonkeyDevice;
import com.android.monkeyrunner.core.TouchPressType;
import com.android.monkeyrunner.recorder.MonkeyRecorderFrame;

public class HwhRunner {
	private static IMonkeyDevice device;
	private static AdbBackend adb;
	private static final Object LOCK = new Object();
	
	public static void main(String[] args) {
		if (adb == null){
			adb = new AdbBackend();
			//参数分别为自己定义的等待连接时间和设备id
			device = adb.waitForConnection();
		}
		
		HwhRecorderFrame frame = new HwhRecorderFrame(device);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                device.dispose();
                synchronized (LOCK) {
                    LOCK.notifyAll();
                }
            }
        });

        frame.setVisible(true);
        synchronized (LOCK) {
            try {
                LOCK.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
		
	}

}
