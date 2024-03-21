package physics;
import java.awt.Color;

public class Particle {
    public double x, y;
    public double lastx, lasty;
    public double ax = 0, ay = 0;
    public double vx = 0, vy = 0;
    public double mass;

    public int radius;
    public Color color;
    public double temperature;
    
    double probabilityOfTransfer = 0.94;
    double floorTempGain = 100;
    double maxTemp = 200;

    public Particle(double xpos, double ypos, int radiusValue, double massValue, Color colorValue){
        x = xpos;
        y = ypos;
        lastx = x;
        lasty = y;

        radius = radiusValue;
        mass = massValue;
        color = colorValue;
        temperature = 0;
    }

    public void update(double dt){ //velocity verlet
        vx = x - lastx;
        vy = y - lasty;
        lastx = x;
        lasty = y;
        x = x + vx + ax * dt * dt;
        y = y + vy + ay * dt * dt;
    }

    public double getVelocityX(){
        return vx;
    }

    public double getVelocityY(){
        return vy;
    }

    public void slowdown(double factor){
        lastx += factor * (x - lastx);
        lasty += factor * (y - lasty);
    }

    public void invertVelocity(){
        lastx = x + vx;
        lasty = y + vy;
    }

    public void stop(){ //or slowdown(1)
        lastx = x;
        lasty = y;
    }

    public void applyForce(double fx, double fy){ //f = ma therefore a = f/m
        this.ax += fx/mass; 
        this.ay += fy/mass;
    }
   
    public void accelerate(double fx, double fy){ //for things like gravity
        this.ax = fx; 
        this.ay = fy;
    }

    public boolean enforceBoundaryConditionCircular(double bx, double by, double r){
        double offsetx = x - bx;
        double offsety = y - by;
        double distance = Math.hypot(offsetx, offsety);
        if (distance > (r - radius)){
            x = bx + offsetx/distance * (r - radius);
            y = by + offsety/distance * (r - radius);
            return true;
        }
        return false;
    }

    public boolean enforceBoundaryCondition(double width, double height, double padding){
        boolean collision = false;
        if (x < padding){
            x = padding;
            lastx = x + vx;
            collision = true;

        } else if (x > width - padding){
            x = width - padding;
            lastx = x + vx;
            collision = true; }

        if (y < padding){
            // y = padding;
            // lasty = y + vy;
            // collision = true;
        } else if (y > height - padding){
            y = height - padding;
            lasty = y + vy;
            collision = true;
            temperature += floorTempGain;
            if (temperature > maxTemp){temperature = maxTemp;}
            }

        return collision;
    }

    public boolean solveCollision(Particle p) {
        double offx = x - p.x; 
        double offy = y - p.y;
        double distance = Math.hypot(offx, offy);
        if (distance < (radius + p.radius) && distance != 0) { // intersection, move p out of this
            double overshoot = ((radius + p.radius) - distance);
            double massRatio = p.mass/mass; //account for momentum
            //massRatio = 1;

            x += massRatio * 0.5 * overshoot * offx/distance;
            y += massRatio * 0.5 * overshoot * offy/distance;

            p.x -= 1/massRatio * 0.5 * overshoot * offx/distance;
            p.y -= 1/massRatio * 0.5 * overshoot * offy/distance;

            if (Math.random() > probabilityOfTransfer){
            p.temperature = 0.5 * temperature + 0.5 * p.temperature;
            temperature = 0.5 * p.temperature + 0.5 * temperature;
            }

            return true;
        }
        return false;
    }

    // public void update(double dt){ //stormer verlet
    //     double tx = x;
    //     double ty = y;
    //     //vx = x - lastx;
    //     //vy = y - lasty;
    //     x = x * 2 - lastx + this.ax * dt * dt;
    //     y = y * 2 - lasty + this.ay * dt * dt;
    //     lastx = tx;
    //     lasty = ty;
    // }
}