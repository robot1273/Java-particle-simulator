import java.awt.Color;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import graphics.Display;

import physics.Particle;
import physics.GridPartition;

public class VerletIntergration {

    private Particle[] particles;

    private GridPartition grid;
    private Display display;

    private ExecutorService executor;

    //nice presets:
    // 10 000 particles, 15 sub steps, 180 partitions, 0 energy loss, 5 threads, gravity mode

    /// ------------------------------------ Main parameters ------------------------------------ ///
    int screenw = 800, screenh = 800;

    int numParticles = 6000;
    int particleSize = 3;
    int randomParticleSize = 2; //random scaling variance

    double motionBlurIntensity = 0.3;
    //performance
    double fps = 60; // Higher fps tends to yield more accuracy if there is no lag
    double timeScale = 1; // Speed of the simulation
    int subSteps = 16; // The faster the simulaton or the more particles there are, the larger subSteps should be for stability
    int numPartitions = 100; //Number of spacial partitions to split elements into

    //multithreading improves stability considerably but not performance all that much
    int numThreads = 0; //set to zero to disable threading (MUST BE A MULTIPLE OF PARTITIONS!!!)

    double drag = 1; //air resistance
    double energyLoss = 0.00; //Amount of speed to lose on each collision, 0 = none, 1 = all
    double wallEnergyLoss = 0.05; //Amount of speed to lose on each wall collision, 0 = none, 1 = all

    boolean deterministic = false; // Determine wether to keep a constant dt or do a constant speed simulation 
    //^ increases stability, only deterministic when no random elements are used

    boolean doVelocityVisualisation = false; //cool effect to see energy transfer
    int boundaryMode = 1; //0 = No boundary 1 = rectangular boundary (buggy) 2 = circular boundary
    int gravityMode = 0; //0 = normal gravity 1 = move to center
    double gravity = 0.5;

    double velocityLimit = 40; //set to 0 to not restrict velocity, improves stability
    double velocitySlowdownFactor = 0.9; //When attempting to restrict velocity, how much to slow down

    double gracePeriod = 1000; //ms of time to have velocity limit to get simulation into stable state
    boolean gracePeriodEnded = false;

    double cursorStrength = 30; //mass of cursor particle
    int cursorSize = 10; //radius of cursor particle

    boolean doFireSimulation = false;
    double temperatureLoss = 0.995;
    double temperatureForceModifierVertical = 1;
    double temperatureForceModifierHorizontal = 0;
    /// ----------------------------------------------------------------------------------------- ///

    public static void main(String[] args) {
        new VerletIntergration();
    }

    /// -------------------------------- START OF MAIN -------------------------------- //

    public VerletIntergration() {
        display = new Display(screenw, screenh);
        grid = new GridPartition(numPartitions * screenh/screenw, numPartitions);

        if (numThreads != 0){executor = Executors.newFixedThreadPool(numThreads);}

        double startTime = System.currentTimeMillis();
        double gametime = 0, lastTick = 0;

        double dt = timeScale / subSteps; // Default deltatime is for deterministic simulation (has best stability)
        double previousFps = 0;

        if (velocityLimit == 0){velocityLimit = 999999;} //Set velocitylimit to 999 if "disabled" (when zero)

        particles = new Particle[numParticles];

        for (float i = 0; i < numParticles; ++i) { // setup particles in a spiral to fill circle
            double r = (i-1) * 300 / numParticles;
            double theta = i / Math.PI / 2;
            double a = Math.cos(theta) * r;
            double b = Math.sin(theta) * r;
            Color c = HSVtoRGB(i/numParticles, 1, 1);

            double size = particleSize+ Math.random()*randomParticleSize;
            particles[(int) i] = new Particle(400 + a, 400 + b, (int) size, 10 + size ,c);
        }

        //particles[numParticles-1] = new Particle(400, 400, 25, 500 , Color.WHITE);

        //particles[1] = new Particle(310, 400, (int) 30, 5, 1, Color.WHITE);
        //particles[2] = new Particle(420, 400, (int) 30, 5, 1, Color.WHITE);


        if (!gracePeriodEnded) {energyLoss += 0.05;} //Setup grace period by greatly increasing energy loss

        // main loop
        while (true) {
            lastTick = gametime;
            gametime = (System.currentTimeMillis() - startTime);

            while ((gametime - lastTick) < 1000 / fps) { // tick frames
                gametime = (System.currentTimeMillis() - startTime);
            }

            if (!deterministic) {
                dt = (gametime - lastTick) / (1000 / fps) * timeScale / subSteps;
            } // Constant speed simulation, dynamic dt
            
            if (gametime > gracePeriod && !gracePeriodEnded){ //End grace period
                System.out.println("grace period ended");
                gracePeriodEnded = true;
                energyLoss -= 0.05;}

            update(dt); // perform physics
            
            // ---------- rendering ---------- //
            display.fill(new Color(0, 0, 0, (int) (255*(1-motionBlurIntensity)))); //motion blur effect with low alpha :)
            
            for (Particle p : particles) { // draw particles
                if (p != null && p != particles[0]){
                    Color c = p.color;
                    if (doVelocityVisualisation){
                    double v = Math.hypot(p.getVelocityX(), p.getVelocityY())*255;
                    if (v>255){ v = 255;}
                    c = new Color((int)v, 0, 0);}
                    
                    if (doFireSimulation){
                        double tempValue = p.temperature*155, g = p.temperature*75 + 0, b = p.temperature*55 + 0;
                        if (tempValue > 255){ tempValue = 255;}
                        if (g > 255){g = 255;}
                        if (b > 255){b = 255;}
                        c = new Color((int)tempValue, (int)g, (int)b);}
            
                    //display.drawCircle((int) (p.lastx - p.getVelocityX()*100), (int) (p.lasty - p.getVelocityY()*100), 5, Color.RED);
                    display.drawCircle((int) p.x, (int) p.y, p.radius, c);

                }
            }
            
            double currentFps = (Math.round(1000 / (gametime - lastTick)) + previousFps)/2;
            previousFps = currentFps;
            display.drawText("FPS: " + currentFps, 10, 20, Color.WHITE);
            display.drawText("Particles Rendered: " + numParticles, 10, 32, Color.WHITE);

            display.update();
        }
    }

    public void updateCursorParticle(){
        double mx = display.getMouseX(), my = display.getMouseY();
        particles[0] = new Particle(mx, my, cursorSize, cursorStrength, Color.WHITE); //invisible particle following mouse to interact with simulation 
        particles[0].temperature = 1000;
    }

    /// -------------------------------- END OF MAIN LOOP -------------------------------- //

    public void updateParticles(double dt){ //TODO nothing i just want a little blue bar to show me where this is
        for (Particle p : particles) {
            double ox = -(p.x - 400)/500;
            double oy = -(p.y - 400)/500;

            //p.accelerate(mxf, myf);
            if (gravityMode == 0){p.accelerate(0, gravity);} else {p.accelerate(ox, oy);}
            
            p.applyForce(-(p.x - p.lastx)*drag, -(p.y - p.lasty)*drag); //apply "air resitance"
            
            double verticalStrength = p.temperature*temperatureForceModifierVertical;
            double horizontalStrength = p.temperature*temperatureForceModifierHorizontal;
            if (doFireSimulation){
                if (gravityMode == 0){
                    double dir = 1;
                    if (Math.random() > 0.5){dir*=-1;};
                    p.applyForce(Math.random()*dir*horizontalStrength, -verticalStrength);
                } else {
                    p.applyForce(-ox * verticalStrength, -oy * verticalStrength);
                }
            }
            
            p.temperature *= temperatureLoss;

            p.update(dt);
            

            if (p != particles[0] && boundaryMode != 0){
                boolean collision = false;
                if (boundaryMode == 1){collision = p.enforceBoundaryCondition(screenw, screenh, 20);} else{
                collision = p.enforceBoundaryConditionCircular(screenw/2, screenh/2, 300);}
                if (collision) {p.slowdown(wallEnergyLoss);}
            }
            
            if ((Math.pow(p.getVelocityX(), 2) + Math.pow(p.getVelocityY(), 2)) > velocityLimit){
                p.slowdown(velocitySlowdownFactor);;
            }
        }
    }

    public void update(double dt){
        for (int i = 0; i < subSteps; ++i) {
            updateParticles(dt);
            updateCursorParticle();
            partitionParticles();
            
            if (numThreads == 0){solveCollisions();
            } else {solveCollisionsThreaded();}
        }

    }

    public void solveUnitCollisions(Particle p, int partitionIndex){
        try {
        GridPartition.GridUnit unit = grid.partition[partitionIndex];
        for (int particleIndex2 : unit.objects){
            Particle p2 = particles[particleIndex2];
            if (p != p2 && p2 != null) {
                boolean collision = p.solveCollision(p2);
                if (collision) {
                p.slowdown(energyLoss);
                p2.slowdown(energyLoss);}
            }
        }
        } catch (Exception e) {}
    }

    public void solveCollisions(){
        int i = 0;
        for (GridPartition.GridUnit unit : grid.partition){
            for (int particleIndex : unit.objects){
                Particle p = particles[particleIndex];
                solveUnitCollisions(p, i); //all adjacent units
                solveUnitCollisions(p, i + 1);
                solveUnitCollisions(p, i - 1);
                solveUnitCollisions(p, i + grid.height);
                solveUnitCollisions(p, i + grid.height + 1);
                solveUnitCollisions(p, i + grid.height - 1);
                solveUnitCollisions(p, i - grid.height);
                solveUnitCollisions(p, i - grid.height + 1);
                solveUnitCollisions(p, i - grid.height - 1);
            }
            ++i;
        }
    }

    public void solveCollisionsThreaded(){
        CountDownLatch latch = new CountDownLatch(numThreads);
        //executor = Executors.newFixedThreadPool(numThreads);

        for (int thread = 0; thread < numThreads; ++thread){
            final int threadID = thread;
            executor.submit(() -> {solveCollisionsThreadedRange(threadID, latch);});
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        
        // executor.shutdown();
        // try{
        //     executor.awaitTermination(17, TimeUnit.MILLISECONDS);
        // } catch (Exception e) {}
    }

    public void solveCollisionsThreadedRange(int thread, CountDownLatch latch){
        int threadDomainHeight = grid.height/numThreads;
        for (int y = thread * threadDomainHeight; y < (thread + 1) * threadDomainHeight; ++y){
            for (int x = 0; x < grid.width; ++x){
                int i = x * grid.width + y ; 
                GridPartition.GridUnit unit = grid.partition[i];
                for (int particleIndex : unit.objects){
                    Particle p = particles[particleIndex];
                    solveUnitCollisions(p, i); //all adjacent units
                    solveUnitCollisions(p, i + 1);
                    solveUnitCollisions(p, i - 1);
                    solveUnitCollisions(p, i + grid.height);
                    solveUnitCollisions(p, i + grid.height + 1);
                    solveUnitCollisions(p, i + grid.height - 1);
                    solveUnitCollisions(p, i - grid.height);
                    solveUnitCollisions(p, i - grid.height + 1);
                    solveUnitCollisions(p, i - grid.height - 1);
                }
                ++i;
            }
        }
        latch.countDown();
    }

    public void partitionParticles(){
        grid.clear();
        
        int index = 0;
        for (Particle p : particles){
            if (1 < p.x && p.x < screenw - 1 && 1 < p.y && p.y < screenh - 1 ){ //if on screen
                int partitionX = (int) p.x/(screenw/grid.width);
                int partitionY = (int) p.y/(screenh/grid.height);
                grid.addItem(partitionX, partitionY, index);
            }
            ++ index; 
        }
    }

    public static Color HSVtoRGB(double hue, double saturation, double value) {
        int h = (int)(hue * 6);
        double f = hue * 6 - h;
        double p = value * (1 - saturation);
        double q = value * (1 - f * saturation);
        double t = value * (1 - (1 - f) * saturation);
    
        switch (h) {
          case 0: return rgb(value, t, p);
          case 1: return rgb(q, value, p);
          case 2: return rgb(p, value, t);
          case 3: return rgb(p, q, value);
          case 4: return rgb(t, p, value);
          case 5: return rgb(value, p, q);
          default: return null;
        }
    }

    public static Color rgb(double r, double g, double b) {
        int rs = ((int)(r * 255));
        int gs = ((int)(g * 255));
        int bs = ((int)(b * 255));
        return new Color(rs, gs, bs);
    }
}
