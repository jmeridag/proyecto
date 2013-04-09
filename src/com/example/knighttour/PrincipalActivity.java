package com.example.knighttour;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.controller.MultiTouch;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.opengl.GLES20;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;


public class PrincipalActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener, IOnAreaTouchListener, IPinchZoomDetectorListener, IScrollDetectorListener, IOnMenuItemClickListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static int CAMERA_WIDTH;
	private static int CAMERA_HEIGHT;
	
	//Constantes del menu popup
	protected static final int MENU_RESET = 0;
	protected static final int MENU_QUIT = MENU_RESET + 1;

	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mCircleFaceTextureRegion;
	//Para el boton opciones
	private BitmapTextureAtlas mBitmapTextureAtlas2;
	private TiledTextureRegion mCircleOptionsTextureRegion;
	
	private PhysicsWorld mPhysicsWorld;
	private Scene mScene;
	private Body body;
	
	//Para el zoom y la camara
	private ZoomCamera mZoomCamera;
	private SurfaceScrollDetector mScrollDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	
	//Boton para las opciones
	//private Sprite bOpciones;
	
	//Fuente de la letra
	private Font mFont;
	
	//Para el popup menu
	protected MenuScene mMenuScene;
	protected Scene firstScene;
	private BitmapTextureAtlas mMenuTexture;
	protected ITextureRegion mMenuResetTextureRegion;
	protected ITextureRegion mMenuQuitTextureRegion;
	

	// ===========================================================
	// Constructors
	// ===========================================================
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	public EngineOptions onCreateEngineOptions() {
		Toast.makeText(this, "Touch the screen to add objects. Touch an object to shoot it up into the air.", Toast.LENGTH_LONG).show();

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		CAMERA_WIDTH = metrics.widthPixels;
		CAMERA_HEIGHT = metrics.heightPixels;
		//final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		//Declaramos la posicion de la camara
		this.mZoomCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		//Definimos la posicion de la camara y su sentido
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mZoomCamera);
		
		//Comprobamos que el movil tiene pantalla multi-tactil y mostramos en pantalla
		if(MultiTouch.isSupported(this)) {
			if(MultiTouch.isSupportedDistinct(this)) {
				Toast.makeText(this, "MultiTouch detected --> Both controls will work properly!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "MultiTouch detected, but your device has problems distinguishing between fingers.\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
		}

		return engineOptions;

		//return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		mBitmapTextureAtlas = new BitmapTextureAtlas(getTextureManager(), 178, 178);
		mBitmapTextureAtlas2 = new BitmapTextureAtlas(getTextureManager(), 64, 64);
		mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, "Chess.png", 0, 0, 1, 1); // 178x178
		mCircleOptionsTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas2, this, "bopciones2.png", 0, 0, 1, 1); // 64x64
		mBitmapTextureAtlas.load();
		mBitmapTextureAtlas2.load();
//		
//		//Para cargar una nueva fuente del tipo de letra
//		final ITexture fontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
//		this.mFont = FontFactory.createFromAsset(this.getFontManager(), fontTexture, this.getAssets(), "Plok.ttf", 48, true, android.graphics.Color.WHITE);
//		this.mFont.load();
		
		//Para cargar la imagen de los menus
		this.mMenuTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
		this.mMenuResetTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mMenuTexture, this, "menu_reset.png", 0, 0);
		this.mMenuQuitTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mMenuTexture, this, "menu_quit.png", 0, 50);
		this.mMenuTexture.load();
		
		
		
		
		
	}

	public Scene onCreateScene() {

		mScene = new Scene();
		this.mScene.setOnAreaTouchTraversalFrontToBack();
		HUD hud = new HUD();
		mZoomCamera.setHUD(hud);
		
		mScene.setBackground(new Background(0.5f, 0.5f, 0.5f));
		
		mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);

		mScene.registerUpdateHandler(mPhysicsWorld); //Si se quita no se mueve.
		
		//Cargamos el menu
		this.createMenuScene();

		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 20, CAMERA_WIDTH, 40, mEngine.getVertexBufferObjectManager());
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH-20, 40, mEngine.getVertexBufferObjectManager());
		final Rectangle left = new Rectangle(0, 0, 40, CAMERA_HEIGHT-20, mEngine.getVertexBufferObjectManager());
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 20, 0, 40, CAMERA_HEIGHT, mEngine.getVertexBufferObjectManager());

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(1, 1, 1);
		PhysicsFactory.createBoxBody(mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);	
		
		final float centerX = (CAMERA_WIDTH - mCircleFaceTextureRegion.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - mCircleFaceTextureRegion.getHeight()) / 2;
		final AnimatedSprite face = new AnimatedSprite(centerX, centerY, mCircleFaceTextureRegion, mEngine.getVertexBufferObjectManager());
		
		//Botón de opciones
		
		final Sprite bOpciones = new Sprite(20, 20, mCircleOptionsTextureRegion, mEngine.getVertexBufferObjectManager()){

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
					case TouchEvent.ACTION_DOWN:
						if(mScene.hasChildScene()){
							mScene.back();
						}
						else{
							mScene.setChildScene(mMenuScene,false,true,true);
						}
						break;
				}
				return true;
			}
			
		};

		
		body = PhysicsFactory.createCircleBody(mPhysicsWorld, face, BodyType.DynamicBody, wallFixtureDef);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
		face.setUserData(body);
		mScene.attachChild(ground);
		mScene.attachChild(roof);
		mScene.attachChild(left);
		mScene.attachChild(right);
		
		
		
		
		mScene.registerTouchArea(face);
		hud.registerTouchArea(bOpciones);
		mScene.attachChild(face);
		hud.attachChild(bOpciones);
		
		this.mScrollDetector = new SurfaceScrollDetector( this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		
		mScene.setOnAreaTouchListener(this);
		this.mScene.setOnSceneTouchListener(this);
		mScene.setTouchAreaBindingOnActionDownEnabled(true); //Sirve para que el dedo al salir del sprite continue moviendose.		
		mScene.setTouchAreaBindingOnActionMoveEnabled(true);
		
		return mScene;
	}

	private void createMenuScene() {
		// TODO Auto-generated method stub
		
		this.mMenuScene = new MenuScene(this.mZoomCamera);

		final SpriteMenuItem resetMenuItem = new SpriteMenuItem(MENU_RESET, this.mMenuResetTextureRegion, this.getVertexBufferObjectManager());
		resetMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuScene.addMenuItem(resetMenuItem);

		final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT, this.mMenuQuitTextureRegion, this.getVertexBufferObjectManager());
		quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuScene.addMenuItem(quitMenuItem);

		this.mMenuScene.buildAnimations();

		this.mMenuScene.setBackgroundEnabled(false);

		this.mMenuScene.setOnMenuItemClickListener(this);
	
		
	}

	public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {

	}

	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerationSensor(this);
	}

	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, ITouchArea pTouchArea, float pTouchAreaLocalX, float pTouchAreaLocalY) {

		if(pSceneTouchEvent.isActionMove()) {
			final AnimatedSprite face = (AnimatedSprite) pTouchArea;
			
	        final Body faceBody = (Body)face.getUserData();
	        
            faceBody.setTransform(pSceneTouchEvent.getX() / 32, pSceneTouchEvent.getY() / 32,  0);
            
            return true;
		}
		
		//Si pulsamos sobre el obejoto cambiamos su tamaño al doble
		if(pSceneTouchEvent.isActionDown()){
			final AnimatedSprite face = (AnimatedSprite) pTouchArea;
			face.setScale(2);
			return true;
		}
		//Si dejamos de pulsar, el tamaño vuelve a su estado original
		if(pSceneTouchEvent.isActionUp()){
			final AnimatedSprite face = (AnimatedSprite) pTouchArea;
	        face.setScale(1);
			return true;
		}
		return false;
	}
	
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        
		this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
		
		if(this.mPinchZoomDetector.isZooming()) {
			
			this.mScrollDetector.setEnabled(false);
			
		} else {
			if(pSceneTouchEvent.isActionDown()) {
				this.mScrollDetector.setEnabled(true);
			}	
			
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}
		return true;
        
	}

	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		
	}

	@Override
	public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		
		this.mPinchZoomStartedCameraZoomFactor = this.mZoomCamera.getZoomFactor();
		
	}

	@Override
	public void onPinchZoom(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pTouchEvent, float pZoomFactor) {
		// TODO Auto-generated method stub
		
		this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
	}

	@Override
	public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pTouchEvent, float pZoomFactor) {
		// TODO Auto-generated method stub
		
		this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
		
	}

	@Override
	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		final float zoomFactor = this.mZoomCamera.getZoomFactor();
		this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
		
	}
	

	@Override
	public void onScroll(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
		final float zoomFactor = this.mZoomCamera.getZoomFactor();
		this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
		
	}

	@Override
	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
		final float zoomFactor = this.mZoomCamera.getZoomFactor();
		this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
		
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
			float pMenuItemLocalX, float pMenuItemLocalY) {
		// TODO Auto-generated method stub
		switch(pMenuItem.getID()) {
		case MENU_RESET:
			/* Restart the animation. */
			//Toast.makeText(this, "Has salido del menu enhorabuena", Toast.LENGTH_SHORT).show();
			

			/* Remove the menu and reset it. */
			this.mScene.clearChildScene();
			this.mMenuScene.reset();
			return true;
		case MENU_QUIT:
			/* End Activity. */
			this.finish();
			return true;
		default:
			return false;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
