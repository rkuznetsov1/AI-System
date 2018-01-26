package com.mygdx.panzer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Влада on 03.12.2017.
 */

public class ProcessScreen extends ScreenAdapter {

    public enum ProcessState {
        RUN,
        PAUSE,
        FINISHED
    }

    private PanzerProject game;
    private MapManager mapManager;

    private OrthogonalTiledMapRenderer mapRenderer;
    private ShapeRenderer shapeRenderer;

    private PanzerHUD hud;
    public Panzer panzer;

    /*
    --------------------------------------------------------------------------------------------------------------------
     */

    final int WIDTH = 1920;
    final int HEIGHT = 1080;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private FitViewport viewport;
    private Stage stage;
    private Skin skin;
    /*private TextField velocityField;
    private PanzerProject process;
    private TextField sensorField;
    private TextField angleField;
    private SelectBox<String> mapField;
    private Texture enableSensorsTexture;
    private Texture disableSensorsTexture;
    private Image enableSensors;
    private Image applyButton;
    private Texture applyButtonTexture;
    private Texture labelTexture;
    private Image label;
    private BitmapFont font;
    private boolean isEnableSensors = Settings.isDrawsensors();*/

    /*
    --------------------------------------------------------------------------------------------------------------------
     */

    public ProcessScreen(PanzerProject game) {

        camera = new OrthographicCamera();
        camera.position.set(WIDTH / 2, HEIGHT / 2, 0);
        camera.update();
        viewport = new FitViewport(WIDTH, HEIGHT, camera);
        viewport.apply(true);
        batch = new SpriteBatch();
        this.stage = new Stage(viewport, batch);
        skin = new Skin(Gdx.files.internal("HUD/uiskin.json"));
        skin.getFont("default-font").getData().setScale(2);

        this.game = game;
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.position.set(Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 2, 0);
        camera.update();
        viewport = new FitViewport(Settings.WORLD_WIDTH, Settings.WORLD_HEIGHT, camera);
        viewport.apply(true);
        batch = new SpriteBatch();
        game.setProcessState(ProcessState.PAUSE);
    }

    @Override
    public void show() {
        mapManager = MapManager.getInstance();
        Map currentMap = new Map("maps/" + Settings.getMapname() /*+ ".tmx"*/);
        mapManager.setMap(currentMap);
        panzer = new Panzer(Settings.getStartAngle());
        MapManager.getInstance().setPanzer(panzer);
        mapRenderer = new OrthogonalTiledMapRenderer(mapManager.getMap().getTiledMap(), batch);
        mapRenderer.setView(camera);
        hud = new PanzerHUD(game, camera, viewport, batch);
    }

    @Override
    public void render(float delta) {
        switch (game.getProcessState()) {
            case RUN:
                panzer.updatePosition(delta);
                break;
            case PAUSE:
                panzer.reset();
                for (Sensor sensor: panzer.getSensors()) {
                    sensor.reset();
                }
                break;
            default:
                break;
        }
        clearScreen();
        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);
        mapRenderer.render();
        if (Settings.isDrawsensors()) {
            drawSensors();
            drawDebug();
        }
        if (hud.isPanzInFinish())
            game.setProcessState(ProcessState.FINISHED);
        panzer.draw(batch, delta);
        hud.render(delta);
    }


    // рендерим прямоугольники физических обьектов
    private void drawDebug() {
        Polygon panzer = mapManager.getPanzer().getPhysBody();
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Array<Polygon> polygonPhysObjects = mapManager.getMap().getPolygonPhysObjects();

        for (Polygon polygon : polygonPhysObjects) {
            shapeRenderer.polygon(polygon.getTransformedVertices());
        }

        shapeRenderer.polygon(panzer.getTransformedVertices());
        shapeRenderer.end();
    }

    private void drawSensors()
    {
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Sensor sensor: panzer.getSensors()) {
            Vector2 endPoint = null;
            if (sensor.seeingObject()) {
                endPoint = new Vector2(sensor.getIntersectPoint());
            } else {
                endPoint = new Vector2(sensor.getSensorEnd());
            }
            shapeRenderer.line(sensor.getSensorBegin(), endPoint);
        }
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose () {
        batch.dispose();
        panzer.dispose();
        mapManager.getMap().dispose();
        hud.dispose();
    }
}

