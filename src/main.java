import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;



public class main extends JPanel{

    public static member currentPaintedMember=null;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int curXPos=0;
        int curYPos=0;
        member thisMember = null;
        // draw the rectangle here
        try {
            thisMember = (member) exchanger.exchange("Message received", 2000,TimeUnit.NANOSECONDS);
            currentPaintedMember=thisMember;
        } catch (InterruptedException | TimeoutException e) {
            thisMember=currentPaintedMember;
        }
        //System.out.println("Current member has fitness of " + thisMember.getFitness());
        if(thisMember!=null) {
            Integer[][] thisFloor = thisMember.getRoom();
            for (int y = 0; y < rowAmount; y++) {
                for (int z = 0; z < thisFloor[y].length; z++) {
                    System.out.print(thisFloor[y][z] + "|");
                }
                System.out.println();
                System.out.println("________________________");
            }
            System.out.println(thisMember.getFitness());
            System.out.println("++++++++++++");


            System.out.println("...");
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < rowAmount; col++) {
                    //double waveLength=(double)(580*(thisMember.getRoom()[col][row]/flavors));
                    if (col == rowAmount - 1 && thisMember.getRoom()[col].length - 1 < row) {
                        g.setColor(Color.gray);
                    } else if (thisMember.getRoom()[col][row] == 0) {
                        g.setColor(Color.white);
                    } else {
                        Color curColor = colors.get(thisMember.getRoom()[col][row]);
                        if (curColor == null) {
                            Integer[] getRGB = getRGB(thisMember.getRoom()[col][row]);
                            curColor = new Color(getRGB[0], getRGB[1], getRGB[2]);
                            colors.put(thisMember.getRoom()[col][row], curColor);
                        }
                        g.setColor(curColor);
                    }
                    g.fillRect(curXPos, curYPos, getWidth() / 10, getHeight() / rowAmount);
                    curYPos += getHeight() / rowAmount;
                }
                curYPos = 0;
                curXPos += getWidth() / 10;
                if (row == 9) {
                    curXPos = 0;
                }
            }
        }

    }


    @Override
    public Dimension getPreferredSize() {
        // so that our GUI is big enough
        return new Dimension(1000, 1000);
    }


    //modified from some fortran code I found
    public static Integer[] getRGB(double wavelength){
        double R=0.0;
        double B=0.0;
        double G=0.0;
        double SSS=0.0;
        if (wavelength >= 380 && wavelength < 440) {
            R = -(wavelength - 440.) / (440. - 350.);
            G = 0.0;
            B = 1.0;
        }else if (wavelength >= 440 && wavelength < 490) {
            R = 0.0;
            G = (wavelength - 440.) / (490. - 440.);
            B = 1.0;
        }else if(wavelength >= 490 && wavelength < 510) {
            R = 0.0;
            G = 1.0;
            B = -(wavelength - 510.) / (510. - 490.);
        }else if (wavelength >= 510 && wavelength < 580){
            R = (wavelength - 510.) / (580. - 510.);
            G = 1.0;
            B = 0.0;
        }else if (wavelength >= 580 && wavelength < 645) {
            R = 1.0;
            G = -(wavelength - 645.) / (645. - 580.);
            B = 0.0;
        }else if (wavelength >= 645 && wavelength <= 780) {
            R = 1.0;
            G = 0.0;
            B = 0.0;
        }else {
            R = 0.0;
            G = 0.0;
            B = 0.0;
        }

        if(wavelength >= 380 && wavelength < 420){
            SSS = 0.3 + 0.7 * (wavelength - 350) / (420 - 350);
        }else if(wavelength >= 420 && wavelength <= 700) {
            SSS = 1.0;
        }else if(wavelength > 700 && wavelength <= 780) {
            SSS = 0.3 + 0.7 * (780 - wavelength) / (780 - 700);
        }else {
            SSS = 0.0;
        }
        SSS *= 255;
        return new Integer[]{(int)(R*SSS), (int)(G*SSS), (int)(B*SSS)};
    }

    private static void createAndShowGui() {
        main mainPanel = new main();

        frame = new JFrame("Floor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        //show a new floor every fifth of a second
        Timer t = new Timer(50,
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mainPanel.repaint();
                    }
                });
        t.start();
        frame.setVisible(true);

    }

    private static final HashMap<Integer,Color>colors=new HashMap<>();
    private static int remainderRow;
    public static int rowAmount;
    public static JFrame frame;
    public static int flavors;
    public static int spaces;
    public static int machines;


    public static Exchanger exchanger = new Exchanger();
    //save 1 thread for UI
    public static ExecutorService service= Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors()-1);
    //the final row will hold the last of the spaces if need be, this number indicates how many spaces are in the row
    public static void main(String[] args) throws ExecutionException, InterruptedException{

        int cores = Runtime.getRuntime().availableProcessors();
        getSpaces(frame);
        getMachines(frame);
        getFlavors(frame);
        if (spaces%10==0){
            rowAmount=spaces/10;
            remainderRow=0;
        }else{
            rowAmount=(spaces/10)+1;
            remainderRow=spaces%10;
        }
        int roundedSpaces=rowAmount*10;

        //start the gui
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGui();
            }
        });

        //maximum amount of holes allowed, counts down to zero as holes are placed

        //System.out.println(fitnessMeasurment(floorSpace));
        //prints the floor matrix to ensure it is running right
        List<member> allFloors=getFirstGen();


        //get the two parents used for mutation
        List<member>parents=getParents(allFloors);
        member selectedMember=parents.get(0);
        member selectedMemberMate=parents.get(1);
        System.out.println("Fitness is: "+selectedMember.getFitness());
        System.out.println("Fitness of mate is "+selectedMemberMate.getFitness());

        //new generation a splice between the two parents
        List<member>childrenListSyn=childrenRun(parents);
        int i=0;
        //rinse and repeat for another 100 generations
        double selectedMemberFitness=0;
        while(i<100){
            parents=getParents(childrenListSyn);
            childrenListSyn=childrenRun(parents);
            i++;
        }
        service.shutdownNow();
    }

    public static List<member> getFirstGen() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(300);
        List<member>finalVal=new ArrayList<>();
        List<member>synList=Collections.synchronizedList(finalVal);
        Runnable roomCreation= () -> {
            member thisMember=new member();
            boolean onFinalRow= spaces <= 10;
            int maxHolesLeft=spaces-machines;

            Integer[][] floorSpace =new Integer[rowAmount][];
            for (int i=0;i<rowAmount;i++){
                if (!onFinalRow){
                    floorSpace[i]=new Integer[10];
                    for(int x=0;x<10;x++) {
                        //random number between 0 (reps a hole) and the amount of flavors
                        //random number between 0 (reps a hole) and the amount of flavors
                        double randomVal = ThreadLocalRandom.current().nextInt(1, flavors+1);
                        //scale the value based on amount of flavors;
                        double scaledVal=(randomVal/flavors)*400;
                        double wavelength =scaledVal+380;
                        floorSpace[i][x]=(int)wavelength;
                        thisMember.addLocation(x, i, floorSpace[i][x]);
                    }
                    if(i+1==rowAmount-1){
                        onFinalRow =true;
                    }
                }else{
                    if(remainderRow!=0) {
                        floorSpace[i] = new Integer[remainderRow];
                        for (int x = 0; x < remainderRow; x++) {
                            //random number between 0 (reps a hole) and the amount of flavors
                            double randomVal = ThreadLocalRandom.current().nextInt(1, flavors+1);
                            //scale the value based on amount of flavors;
                            double scaledVal=(randomVal/flavors)*400;
                            double wavelength =scaledVal+380;
                            floorSpace[i][x]=(int)wavelength;
                            thisMember.addLocation(x, i, floorSpace[i][x]);
                        }
                    }else{
                        floorSpace[i] = new Integer[10];
                        for (int x = 0; x < 10; x++) {
                            //random number between 0 (reps a hole) and the amount of flavors
                            double randomVal = ThreadLocalRandom.current().nextInt(1, flavors+1);
                            //scale the value based on amount of flavors;
                            double scaledVal=(randomVal/flavors)*400;
                            double wavelength =scaledVal+380;
                            floorSpace[i][x]=(int)wavelength;
                            thisMember.addLocation(x, i, floorSpace[i][x]);
                        }
                    }
                }
            }

            //randomly put in holes based on how many holes needed, don't repeat places that holes went before.
            ArrayList<Integer>filledSpaces=new ArrayList<>();
            List<Integer> syncFilledSpaces=Collections.synchronizedList(filledSpaces);

            for(int i=maxHolesLeft;i>0;i--){
                int assignedSpace= ThreadLocalRandom.current().nextInt(0, spaces);
                while(filledSpaces.contains(assignedSpace)){
                    assignedSpace= ThreadLocalRandom.current().nextInt(0, spaces);
                }
                syncFilledSpaces.add(assignedSpace);
                int yAxis=(assignedSpace/10);
                int xAxis=(assignedSpace%10);
                thisMember.removeValueFromHashmap(xAxis, yAxis, floorSpace[yAxis][xAxis]);
                floorSpace[yAxis][xAxis]=0;
                thisMember.addLocation(xAxis,yAxis,0);
            }
            thisMember.addRoom(floorSpace);

            //calculate fitness based on each individual member's closeness to others
            double totalFitness=0;
            int totalLikeDistances=0;
            for(int i=0;i<rowAmount;i++){
                for(int x=0;x<floorSpace[i].length;x++){
                    for(int y=0;y<rowAmount;y++){
                        for(int z=0;z<floorSpace[y].length;z++){
                            double curFitnessClose=0;
                            double curFitnessFar=0;
                            if(i!=y&&x!=z) {
                                double distance=Math.sqrt((Math.pow((x-z), 2)) + (Math.pow((i-y), 2)));
                                double flavorDifference=Math.abs(floorSpace[i][x]-floorSpace[y][z]);
                                if(flavorDifference<=100&&floorSpace[i][x]!=0&&floorSpace[y][z]!=0){
                                    totalFitness+=(1/distance);
                                }
                            }
                        }
                    }
                }
            }

            thisMember.changeFitness(totalFitness);
            synList.add(thisMember);
            latch.countDown();
        };
        List<Future<member>> allFloors;
        for(int i=0;i<300;i++){
            service.execute(roomCreation);
        }

        while (latch.getCount()>0) {
            latch.await();
        }
        return synList;


    }


    public static List<member> childrenRun(List<member>matingPool) throws InterruptedException, ExecutionException {
        //service=Executors.newWorkStealingPool();
        CountDownLatch latch = new CountDownLatch(300);
        List<member>childrenList=new ArrayList<>();
        List<member>childrenListSyn=Collections.synchronizedList(childrenList);
        Runnable createChildren=()->{
            //int crossOverPoint=ThreadLocalRandom.current().nextInt(0,rowAmount);
            //left side is initial parent, right side is the mate
            member thisMember=new member();
            member selectedMember;
            member finalSelectedMemberMate;
            boolean onFinalRow= spaces <= 10;
            Integer[][] childFloorSpace =new Integer[rowAmount][];
            for (int i=0;i<rowAmount;i++){
                selectedMember=matingPool.get(ThreadLocalRandom.current().nextInt(matingPool.size()));
                finalSelectedMemberMate=matingPool.get(ThreadLocalRandom.current().nextInt(matingPool.size()));
                if (!onFinalRow){
                    int crossOverPoint=5;
                    childFloorSpace[i]=new Integer[10];
                    for(int x=0;x<10;x++) {
                        //random number between 0 (reps a hole) and the amount of flavors
                        //random number between 0 (reps a hole) and the amount of flavors
                        if(x<=crossOverPoint){
                            //childFloorSpace[i][x] = ThreadLocalRandom.current().nextInt(1, flavors+1);
                            childFloorSpace[i][x]=selectedMember.getRoom()[i][x];
                            thisMember.addLocation(x, i, childFloorSpace[i][x]);
                        }else{
                            //childFloorSpace[i][x] = ThreadLocalRandom.current().nextInt(1, flavors+1);
                            childFloorSpace[i][x]= finalSelectedMemberMate.getRoom()[i][x];
                            thisMember.addLocation(x, i, childFloorSpace[i][x]);
                        }
                    }
                    if(i+1==rowAmount-1){
                        onFinalRow =true;
                    }
                }else{
                    if(remainderRow!=0) {
                        int crossOverPoint=ThreadLocalRandom.current().nextInt(0,remainderRow);
                        childFloorSpace[i] = new Integer[remainderRow];
                        for (int x = 0; x < remainderRow; x++) {
                            //random number between 0 (reps a hole) and the amount of flavors
                            if(x<=crossOverPoint){
                                //childFloorSpace[i][x] = ThreadLocalRandom.current().nextInt(1, flavors+1);
                                childFloorSpace[i][x]=selectedMember.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }else{
                                //childFloorSpace[i][x] = ThreadLocalRandom.current().nextInt(1, flavors+1);
                                childFloorSpace[i][x]= finalSelectedMemberMate.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }
                            //will only make the hole if the random boolean is true and we can afford to add more holes
                        }
                    }else{
                        childFloorSpace[i] = new Integer[10];
                        int crossOverPoint=ThreadLocalRandom.current().nextInt(0,10);
                        for (int x = 0; x < 10; x++) {
                            //random number between 0 (reps a hole) and the amount of flavors
                            double mutationProb=ThreadLocalRandom.current().nextDouble(0,1);
                            //random number between 0 (reps a hole) and the amount of flavors
                            if(x<=crossOverPoint){
                                //childFloorSpace[i][x] = ThreadLocalRandom.current().nextInt(1, flavors+1);
                                childFloorSpace[i][x]=selectedMember.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }else{
                                //childFloorSpace[i][x] = ThreadLocalRandom.current().nextInt(1, flavors+1);
                                childFloorSpace[i][x]= finalSelectedMemberMate.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }
                        }
                    }
                }
            }

            for(int i=0;i<spaces;i++){
                int value=ThreadLocalRandom.current().nextInt(100);
                //1 percent chance we mutate
                if(value==1){
                    int place1=ThreadLocalRandom.current().nextInt(spaces);
                    int place1X=(place1%10);
                    int place1Y=(place1/10);
                    int place2=ThreadLocalRandom.current().nextInt(spaces);
                    int place2X=(place2%10);
                    int place2Y=(place2/10);
                    int place1Val=childFloorSpace[place1Y][place1X];
                    thisMember.removeValueFromHashmap(place1X,place1Y,childFloorSpace[place1Y][place1X]);
                    childFloorSpace[place1Y][place1X]=childFloorSpace[place2Y][place2X];
                    thisMember.addLocation(place1X,place1Y,childFloorSpace[place1Y][place1X]);
                    thisMember.removeValueFromHashmap(place1X,place1Y,childFloorSpace[place2Y][place2X]);
                    childFloorSpace[place2Y][place2X]=place1Val;
                    thisMember.addLocation(place1X,place1Y,childFloorSpace[place2Y][place2X]);
                }

            }
            //now make sure there is the right number of holes
            //if there are too many holes, fill up as many as needed
            int numberOfHoles=0;
            if(thisMember.getHashmap().containsKey(0)) {
                numberOfHoles = thisMember.getHashmap().get(0).size();
            }
            if(numberOfHoles>(spaces-machines)){
                ArrayList<Integer[]>holesCoordinates=thisMember.getHashmap().get(0);
                int excessHoles=holesCoordinates.size()-(spaces-machines);
                while(excessHoles>0){
                    //randomly fill a chosen index value with an int until no more extra holes
                    int randomIndex=ThreadLocalRandom.current().nextInt(0,holesCoordinates.size());
                    Integer[] removedCoords =holesCoordinates.get(randomIndex);
                    int xVal=removedCoords[0];
                    int yVal=removedCoords[1];
                    thisMember.removeValueFromHashmap(xVal,yVal,0);
                    int newVal;
                    int place2=ThreadLocalRandom.current().nextInt(spaces);
                    int place2X=(place2%10);
                    int place2Y=(place2/10);
                    while (childFloorSpace[place2Y][place2X]==0){
                        place2=ThreadLocalRandom.current().nextInt(spaces);
                        place2X=(place2%10);
                        place2Y=(place2/10);
                    }
                    newVal=childFloorSpace[place2Y][place2X];
                    childFloorSpace[yVal][xVal]=newVal;
                    thisMember.addLocation(xVal,yVal,newVal);
                    excessHoles--;
                }
            }else if(numberOfHoles<(spaces-machines)){
                //not enough holes, choose random indices and put a hole in.
                ArrayList<Integer[]>holesCoordinates=thisMember.getHashmap().get(0);
                int excessSpots;
                if(thisMember.getHashmap().containsKey(0)) {
                    excessSpots = (spaces - machines) - holesCoordinates.size();
                }else{
                    excessSpots=spaces-machines;
                }
                int randomIndex;
                while(excessSpots>0) {
                    randomIndex = ThreadLocalRandom.current().nextInt(spaces);
                    int place1X = (randomIndex % 10);
                    int place1Y = (randomIndex / 10);
                    while (childFloorSpace[place1Y][place1X] == 0) {
                        randomIndex = ThreadLocalRandom.current().nextInt(spaces);
                        place1X = (randomIndex % 10);
                        place1Y = (randomIndex / 10);
                    }
                    int origVal=childFloorSpace[place1Y][place1X];
                    childFloorSpace[place1Y][place1X]=0;
                    thisMember.removeValueFromHashmap(place1X,place1Y,origVal);
                    thisMember.addLocation(place1X,place1Y,0);
                    excessSpots--;
                }

            }


            thisMember.addRoom(childFloorSpace);

            double totalFitness=0;
            double totalLikeDistances=0;
            for(int i=0;i<rowAmount;i++){
                for(int x=0;x<childFloorSpace[i].length;x++){
                    for(int y=0;y<rowAmount;y++){
                        for(int z=0;z<childFloorSpace[y].length;z++){
                            if(i!=y&&x!=z) {
                                double distance=Math.sqrt((Math.pow((x-z), 2)) + (Math.pow((i-y), 2)));
                                double flavorDifference=Math.abs(childFloorSpace[i][x]-childFloorSpace[y][z]);
                                if(flavorDifference<=100&&childFloorSpace[i][x]!=0&&childFloorSpace[y][z]!=0){
                                    totalFitness+=(1/distance);
                                }
                            }
                        }
                    }
                }
            }

            //totalFitness+=thisMember.getHashmap().keySet().size()*totalFitness;
            thisMember.changeFitness(totalFitness);
            childrenListSyn.add(thisMember);
            latch.countDown();
        };
        for(int i=0;i<300;i++){
            service.execute(createChildren);
        }
        while (latch.getCount()>0) {
            latch.await();
        }
        return childrenListSyn;

    }

    public static List<member> getParents(List<member> lastGen) throws InterruptedException, ExecutionException {
        List<member>selectionList=new ArrayList<>();
        List<member>selectionListSyn=Collections.synchronizedList(selectionList);
        List<member> parents=new ArrayList<>();
        final AtomicInteger selectionListIndex=new AtomicInteger(0);
        Callable<List<member>> selection=()->{
            List<member> thisMemberCount=new ArrayList<>();
            int curIndex;
            curIndex=selectionListIndex.get();
            selectionListIndex.getAndAdd(1);
            member curMember=lastGen.get(curIndex);
            int numberOfEntries=(int)(curMember.getFitness());
            //System.out.println("current index is: "+curIndex+" Fitness is: "+curMember.getFitness()+" Number of entries: "+numberOfEntries);
            for(int i=0;i<numberOfEntries;i++){
                thisMemberCount.add(curMember);
                selectionListSyn.add(curMember);
            }
            return thisMemberCount;
        };
        List<Callable<List<member>>>roulette=new ArrayList<>();
        for(int i=0;i<300;i++){
            roulette.add(selection);
        }
        service.invokeAll(roulette);
        member bestMember=selectionListSyn.get(0);
        for(int i=1;i<selectionListSyn.size();i++){
            if(selectionListSyn.get(i).getFitness()>bestMember.getFitness()){
                bestMember=selectionListSyn.get(i);
            }
        }
        String message=(String)exchanger.exchange(bestMember);
        return selectionListSyn;
    }


    public static void getSpaces(final JFrame frame){
        String input = (String) JOptionPane.showInputDialog(
                frame,
                "How Many Spaces?",
                "Swing Tester",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                ""
        );
        spaces=Integer.parseInt(input);
    }

    public static void getMachines(final JFrame frame){
        String input = (String) JOptionPane.showInputDialog(
                frame,
                "How Many machines?",
                "Swing Tester",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                ""
        );

        machines=Integer.parseInt(input);
    }

    public static void getFlavors(final JFrame frame){
        String input = (String) JOptionPane.showInputDialog(
                frame,
                "How Many flavors?",
                "Swing Tester",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                ""
        );

        flavors=Integer.parseInt(input);
    }
}