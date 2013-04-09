package com.example.knighttour;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.Scene;

public class PopupScene extends CameraScene {
	
	public PopupScene(final Camera pCamera, final Scene pParentScene, final float pDurationSeconds, final Runnable pRunnable){
		super(pCamera);
		
		this.setBackgroundEnabled(false);
		pParentScene.setChildScene(this, false, true, true);
		this.registerUpdateHandler(new TimerHandler(pDurationSeconds, new ITimerCallback() {
			
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				// TODO Auto-generated method stub
				PopupScene.this.unregisterUpdateHandler(pTimerHandler);
				pParentScene.clearChildScene();
				if(pRunnable != null){
					pRunnable.run();
				}
			}
		}));
	
	
	}
		
}
