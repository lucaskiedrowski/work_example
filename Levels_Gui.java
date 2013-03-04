package monkey.attack.revenge.gui;

import monkey.attack.revenge.Start_Activity;
import monkey.attack.revenge.database.Database;
import monkey.attack.revenge.database.Database_Methods;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import android.content.Intent;
import android.graphics.Typeface;


public class Levels_Gui extends SimpleBaseGameActivity
{

	// ==========================================
	// ==============CONSTANTS===================
	// ==========================================

	private static final float CAMERA_WIDTH = 480;
	private static final float CAMERA_HEIGHT = 320;

	// ==========================================
	// ==============FIELDS======================
	// ==========================================
	
	private BuildableBitmapTextureAtlas mBitmapTextureAtlas;
	
	private ITextureRegion mButtonTextureRegion;
	private ITextureRegion mButtonPressedTextureRegion;
	private ITextureRegion mPadlockTextureRegion;
	private ITextureRegion mStarTrueTextureRegion;
	private ITextureRegion mStarFalseTextureRegion;
	
	private Camera mCamera;
	private Scene mScene;
	private Font mFont;
	
	// number of first button which we create
	private int Level_Number = 1;
	
	// ==========================================
	// ==METHODS FOR/FROM SUPERCLASS/INTERFACES==
	// ==========================================

	@Override
	public EngineOptions onCreateEngineOptions() 
	{
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);		
		
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	}

	@Override
	protected void onCreateResources() 
	{
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 512, 512, TextureOptions.BILINEAR);
		
		this.mButtonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "small.png");
		this.mButtonPressedTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_hexagon_tiled.png");
		this.mPadlockTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "padlock.png");	
		this.mStarTrueTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "star_true.png");
		this.mStarFalseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "star_false.png");
		
		try {
			this.mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(1, 1, 1));
			this.mBitmapTextureAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}
		
		this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 48, Color.YELLOW_ABGR_PACKED_INT);
		this.mFont.load();

	}

	@Override
	protected Scene onCreateScene() 
	{
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.mScene = new Scene();
		
		this.mScene.setBackground(new Background(1f, 0.5f, 0.3f));

		// Attach Buttons to Scene with (stars + text) or (padlock)
		for(int i=0;i<3;i++)
			for(int j=0;j<7;j++)
				attachButton(mScene, j*60+10, i*60, Level_Number); 

		this.mScene.setTouchAreaBindingOnActionDownEnabled(true);
		
		return this.mScene;
	}

	// ==========================================
	// ==============METHODS=====================
	// ==========================================
	
	private void attachButton(Scene scene,final int x,final int y,final int level)
	{	
		/**
		 * @Sprite Button_Level - create button for each level 
		 * @Sprite Padlock - if level is locked
		 * @Text   Text - if level is unlocked 
		 */
		final ButtonSprite Button_Level = new ButtonSprite(x, y, this.mButtonTextureRegion, this.mButtonPressedTextureRegion, this.getVertexBufferObjectManager(), new OnClickListener() 
		{
			@Override
			public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{	
				runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						Start_Activity.setPref(getSharedPreferences(Start_Activity.FILE_WHICH_CLASS,0) ,Start_Activity.KEY_WHICH_LEVEL,level);
						StartNextGameBaseActivity();
					}
				});
			}
		});		
		final Sprite Padlock = new Sprite(x,y,(ITextureRegion)this.mPadlockTextureRegion,this.getVertexBufferObjectManager());
		final Text Text = new Text(x, y, this.mFont,convertInteger(Level_Number), this.getVertexBufferObjectManager());		
		
		/** 
		 * @Int 	which_land - choosen land by user 		@read from Shared Preferences
		 * @Int 	level_stars - collected stars by user 		@read from Database
		 * @String 	level_state - locked/unlocked 			@read from Database
		 */
		int which_land =Start_Activity.getPref(getSharedPreferences(Start_Activity.FILE_WHICH_CLASS,0) ,Start_Activity.KEY_WHICH_LAND);
		String land_table = null;
		switch(which_land) 
		{	
		case 1 : land_table = "Levelsland1"; break;
		case 2 : land_table = "Levelsland2"; break;
		case 3 : land_table = "Levelsland3"; break;	
		}
		int level_stars =Database_Methods.LevelStars(new Database(this),Level_Number);
		String level_state = Database_Methods.isLevelUnLocked(new Database(this),land_table,Level_Number);
		
		/*
		 Attach Children to Scene
		 */
		this.mScene.attachChild(Button_Level);
		
		if(level_state.compareTo("true") == 0)
		{
			for(int i=0;i<3;i++)	
				AttachStar(x+(i*15),y,level_stars>i);
			
			this.mScene.attachChild(Text);
			this.mScene.registerTouchArea(Button_Level);
		}
		else
		{
			this.mScene.attachChild(Padlock);
		}
		
		// Changed the Level_Number to create next Level_Button
		Level_Number++;	
	}
	
	private void AttachStar(int x,int y,boolean whichstar)
	{	
		/** 
		 * @Bitmap mStarTrueTextureRegion - if Star exists in Database
		 * @Bitmap mStarFalseTextureRegion - if Star do not exists in Database
		*/
		final Sprite Star;

		if(whichstar)
			Star= new Sprite(x, y,(ITextureRegion) this.mStarTrueTextureRegion, this.getVertexBufferObjectManager());
		else
			Star= new Sprite(x, y,(ITextureRegion) this.mStarFalseTextureRegion, this.getVertexBufferObjectManager());
	
		this.mScene.attachChild(Star);
	}

	private void StartNextGameBaseActivity()
	{
		/**
		 * @Class Start_Activity - switch between @GameBaseActivities 
		 * @Value VALUE_START_CHARACTER_GUI - state to start @Character_Gui (GameBaseActivity)
		 */
		
		Start_Activity.setPref(getSharedPreferences(Start_Activity.FILE_WHICH_CLASS,0) ,Start_Activity.KEY_WHICH_START,Start_Activity.VALUE_START_CHARACTER_GUI);
		this.finish();
		startActivity(new Intent(this, Start_Activity.class));
	}

	private static String convertInteger(int i) 
	{
		return Integer.toString(i);
	}

}
