package pinball;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.google.gson.Gson;
import pinball.DTO.BodiesDTO;
import pinball.DTO.BodyDTO;
import com.badlogic.gdx.physics.box2d.joints.MotorJoint;
import com.badlogic.gdx.physics.box2d.joints.MotorJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

import javax.swing.JComponent;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class PinballComponent extends JComponent {

    private long time;

    private static final float WIDTH = 1000;
    private static final float HEIGHT = 1500;

    private static final float CORNER_LENGTH = 175;
    private static final float BASE_LENGTH = 350;
    private static final float FLIPPER_LENGTH = 120;

    private static final float BOX_TO_SCREEN = 10f;
    private static final float SCREEN_TO_BOX = 1f / BOX_TO_SCREEN;

    private final int radius = 30;

    private final World world;
    private ArrayList<Body> bodies = new ArrayList<>();
    private BodiesDTO bodiesDTO;

    private ArrayList<RevoluteJoint> flipperJoints = new ArrayList<>();

    PinballComponent()
    {
        world = new World(new Vector2(0, 9.8f), false);


        Gson gson = new Gson();

        try (Reader reader = new FileReader("bodies.json"))
        {
            bodiesDTO = gson.fromJson(reader, BodiesDTO.class);
            for(BodyDTO bodyDTO : bodiesDTO.getBodies())
            {
                bodies.add(createBody(bodyDTO));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    private Body createBody(BodyDTO bodyDTO)
    {
        Body body = null;

        switch(bodyDTO.getShapeType())
        {
            case BOX:
                body = createWall(bodyDTO.getCoordinates(), bodyDTO.getLength(), bodyDTO.getHeight());
                break;
            case LINE:
                body = createDiagonalLine(bodyDTO.getCoordinates(), bodyDTO.getLength(), bodyDTO.getAngle());
                break;
            case CIRCLE:
                body = createBall(bodyDTO.getCoordinates(), bodyDTO.getRadius());
                break;
            case FLIPPER:
                body = createFlipper(bodyDTO.getCoordinates(), bodyDTO.getJointCoordinates(), bodyDTO.getLength(), bodyDTO.getAngle());
                break;
        }
        return body;
    }

    private Body createWall(float[] coordinates, float length, float height)
    {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(new Vector2(coordinates[0] * SCREEN_TO_BOX, coordinates[1] * SCREEN_TO_BOX));
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body wall = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(length * SCREEN_TO_BOX, height * SCREEN_TO_BOX);

        fixtureDef.shape = shape;
        fixtureDef.restitution = 1;
        wall.createFixture(fixtureDef);
        return wall;

    }

    private Body createDiagonalLine(float[] coordinates, float length, int angle)
    {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(new Vector2(coordinates[0] * SCREEN_TO_BOX, coordinates[1] * SCREEN_TO_BOX));
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.angle = angle * (MathUtils.PI/180);
        Body line = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(length * SCREEN_TO_BOX, 1);
        fixtureDef.shape = shape;
        fixtureDef.restitution = 1;
        line.createFixture(fixtureDef);
        return line;
    }

    private Body createBall(float[] coordinates, float radius)
    {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(coordinates[0] * SCREEN_TO_BOX, coordinates[1] * SCREEN_TO_BOX));
        Body ball = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(radius * SCREEN_TO_BOX);
        fixtureDef.shape = shape;
        fixtureDef.restitution = 1;
        ball.createFixture(fixtureDef);

        return ball;
    }

    private Body createFlipper(float[] coordinates, float[] jointCoordinates, float length, int angle)
    {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(new Vector2(coordinates[0] * SCREEN_TO_BOX, coordinates[1] * SCREEN_TO_BOX));
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.angle = angle * (MathUtils.PI/180);
        Body flipper = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(length * SCREEN_TO_BOX, 1);
        fixtureDef.shape = shape;
        fixtureDef.restitution = 1;
        flipper.createFixture(fixtureDef);

        BodyDef babyBodyDef = new BodyDef();
        babyBodyDef.position.set(new Vector2(jointCoordinates[0] * SCREEN_TO_BOX, jointCoordinates[1] * SCREEN_TO_BOX));
        babyBodyDef.type = BodyDef.BodyType.StaticBody;
        babyBodyDef.angle = angle * (MathUtils.PI/180);
        Body babyBody = world.createBody(babyBodyDef);

        FixtureDef bbfixtureDef = new FixtureDef();
        PolygonShape bbshape = new PolygonShape();
        bbshape.setAsBox(length * SCREEN_TO_BOX, 1);
        bbfixtureDef.shape = shape;
        bbfixtureDef.restitution = 1;
        babyBody.createFixture(bbfixtureDef);

        flipperJoints.add(createFlipperJoint(flipper, babyBody));

        return flipper;
    }

    // private MotorJoint createFlipperJoint(Body base, Body flipper)
    private RevoluteJoint createFlipperJoint(Body base, Body flipper)
    {
        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
        revoluteJointDef.initialize(base, flipper, flipper.getPosition());

        //the below code attempts to enforce limits on rotation about the joint, as well as powering it with a motor,
        //which remains disabled until changeFlipper() is called (see below line 263)
        revoluteJointDef.motorSpeed = (float)Math.PI * 2;
        revoluteJointDef.maxMotorTorque = 10;

        revoluteJointDef.enableMotor = false;

        revoluteJointDef.enableLimit = true;
        revoluteJointDef.lowerAngle = -.01f;
        revoluteJointDef.upperAngle = .01f;

        RevoluteJoint joint = (RevoluteJoint) world.createJoint(revoluteJointDef);

        //playing around with the motor
//        joint.enableMotor(true);
//        joint.setMaxMotorTorque(1);
//        joint.setLimits(-.78f, .78f);
//        joint.enableLimit(true);

        //maybe it should be a motor joint?
//        MotorJointDef motorJointDef = new MotorJointDef();
//        motorJointDef.maxForce = 100f;
//        motorJointDef.maxTorque = 100f;
//        motorJointDef.angularOffset = .5f;
//        motorJointDef.initialize(base, flipper);
//        MotorJoint joint = (MotorJoint) world.createJoint(motorJointDef);

        return joint;
    }


    //All this drawing code is severely repetitive and deserves to be refactored.
    //But hey, once the renderer comes around, we won't need it anyway.
    @Override
    protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);

        long currentTime = System.currentTimeMillis();
        world.step((currentTime - time)/1000f, 6, 2);
        time = currentTime;


        BodyDTO[] _bodiesDTO = bodiesDTO.getBodies();

        for(int ix = 0; ix < this.bodies.size(); ++ix)
        {
            BodyType next = _bodiesDTO[ix].getBodyType();
            Body body = this.bodies.get(ix);
            Vector2 position = body.getPosition();
            switch(next.getBodyName())
            {
                case "WALL":
                    float length = _bodiesDTO[ix].getLength();
                    float height = _bodiesDTO[ix].getHeight();
                    graphics.fillRect((int)(position.x * BOX_TO_SCREEN),
                            (int)(position.y * BOX_TO_SCREEN), (int)length, (int)height);
                    break;

                case "BOTTOM_CORNER":
                    graphics.drawLine((int)(position.x * BOX_TO_SCREEN),
                    (int)(position.y * BOX_TO_SCREEN),
                    (int)((position.x * BOX_TO_SCREEN) + BASE_LENGTH * Math.cos(body.getAngle())),
                    (int)((position.y * BOX_TO_SCREEN) + BASE_LENGTH * Math.sin(body.getAngle())));
                    break;

                case "TOP_CORNER":
                    graphics.drawLine((int)(position.x * BOX_TO_SCREEN),
                    (int)(position.y * BOX_TO_SCREEN),
                    (int)((position.x * BOX_TO_SCREEN) + CORNER_LENGTH * Math.cos(body.getAngle())),
                    (int)((position.y * BOX_TO_SCREEN) + CORNER_LENGTH * Math.sin(body.getAngle())));
                    break;
                case "FLIPPER":
                    graphics.drawLine((int)(position.x * BOX_TO_SCREEN),
                    (int)(position.y * BOX_TO_SCREEN),
                    (int)((position.x * BOX_TO_SCREEN) + FLIPPER_LENGTH * Math.cos(body.getAngle())),
                    (int)((position.y * BOX_TO_SCREEN) + FLIPPER_LENGTH * Math.sin(body.getAngle())));
                    break;
                case "BALL":
                    graphics.fillOval((int) (position.x * BOX_TO_SCREEN - radius),
                    (int) (position.y * BOX_TO_SCREEN - radius),radius * 2, radius * 2);
                    break;
            }
        }

        repaint();
    }
    void changeFlipper(boolean left)
    {
        if (left)
        {
//            leftFlipper.setAngularVelocity(-5);
            flipperJoints.get(0).enableMotor(true); //this doesn't appear to do anything...
        }
        else
        {
//            rightFlipper.setAngularVelocity(5);
            flipperJoints.get(1).enableMotor(true); //this doesn't appear to do anything...
        }
    }
}
