import java.lang.reflect.Member;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class main {


    private static int remainderRow;
    public static int rowAmount;
    public static int flavors;
    public static int cores = Runtime.getRuntime().availableProcessors();
    public static int spaces;
    public static int machines;
    //save 1 thread for UI
    public static int coresForProcessing=cores-1;
    public static ExecutorService service= Executors.newWorkStealingPool(coresForProcessing);
    //the final row will hold the last of the spaces if need be, this number indicates how many spaces are in the row
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors();
        //save 1 thread for UI
        int coresForProcessing=cores-1;//Executors.newFixedThreadPool(coresForProcessing);
        Scanner scanner=new Scanner(System.in);
        System.out.println("How many spaces?");
        spaces=scanner.nextInt();
        System.out.println("How many Machines?");
        machines=scanner.nextInt();
        System.out.println("How many flavors?");
        flavors=scanner.nextInt();
        if (spaces%10==0){
            rowAmount=spaces/10;
            remainderRow=0;
        }else{
            rowAmount=(spaces/10)+1;
            remainderRow=spaces%10;
        }
        //maximum amount of holes allowed, counts down to zero as holes are placed

        //System.out.println(fitnessMeasurment(floorSpace));
        List<Future<member>> allFloors=getFirstGen();
        for(int i=0;i<300;i++){
            Future<member> future=allFloors.get(i);
            member member=future.get();
            Integer[][] thisFloor=member.getRoom();
            if(i==299){
                System.out.println("Final floor");
            }
            for(int y=0;y<rowAmount;y++){
                for(int x=0;x<thisFloor[y].length ;x++){
                    System.out.print(thisFloor[y][x]+"|");
                }
                System.out.println();
                System.out.println("________________________");
            }
            System.out.println(member.getFitness());
            System.out.println("++++++++++++");
        }
        List<member>selectionList=new ArrayList<>();
        List<member>selectionListSyn=Collections.synchronizedList(selectionList);
        ExecutorService selectionservice=Executors.newFixedThreadPool(coresForProcessing-1);
        final AtomicInteger selectionListIndex=new AtomicInteger(0);
        List<Future<member>> finalAllFloors = allFloors;
        Callable<member> selection=()->{
            int curIndex;
            curIndex=selectionListIndex.get();
            selectionListIndex.getAndAdd(1);
            Future<member> curMemberFuture= finalAllFloors.get(curIndex);
            member curMember=curMemberFuture.get();
            int numberOfEntries=(int)Math.floor(100*curMember.getFitness());
            System.out.println("current index is: "+curIndex+" Fitness is: "+curMember.getFitness()+" Number of entries: "+numberOfEntries);
            for(int i=0;i<numberOfEntries;i++){
                selectionListSyn.add(curMember);
            }
            return curMember;
        };
        List<Callable<member>>roulette=new ArrayList<>();
        for(int i=0;i<300;i++){
            roulette.add(selection);
        }
        //simple way to just make sure everything gets finished before anything else happens.
        List<Future<member>>allFutures=service.invokeAll(roulette);
        System.out.println(selectionListSyn.size());
        member selectedMember=selectionListSyn.get(ThreadLocalRandom.current().nextInt(0, selectionListSyn.size()));
        member selectedMemberMate=selectionListSyn.get(ThreadLocalRandom.current().nextInt(0, selectionListSyn.size()));
        do{
            selectedMemberMate=selectionListSyn.get(ThreadLocalRandom.current().nextInt(0, selectionListSyn.size()));
        }while (selectedMember.getFitness()==selectedMemberMate.getFitness());
        System.out.println("Fitness is: "+selectedMember.getFitness());
        System.out.println("Fitness of mate is "+selectedMemberMate.getFitness());

        member finalSelectedMemberMate = selectedMemberMate;
        List<member>childrenList=new ArrayList<>();
        List<member>childrenListSyn=Collections.synchronizedList(childrenList);
        Callable <member> createChildren=()->{
            int crossOverPoint=ThreadLocalRandom.current().nextInt(0,rowAmount);
          //left side is initial parent, right side is the mate
            member thisMember=new member();
            boolean onFinalRow= spaces <= 10;
            Integer[][] childFloorSpace =new Integer[rowAmount][];
            for (int i=0;i<rowAmount;i++){
                if (!onFinalRow){
                    childFloorSpace[i]=new Integer[10];
                    for(int x=0;x<10;x++) {
                        //random number between 0 (reps a hole) and the amount of flavors
                        if(x<=crossOverPoint){
                            childFloorSpace[i][x]=selectedMember.getRoom()[i][x];
                            thisMember.addLocation(x, i, childFloorSpace[i][x]);
                        }else{
                            childFloorSpace[i][x]= finalSelectedMemberMate.getRoom()[i][x];
                            thisMember.addLocation(x, i, childFloorSpace[i][x]);
                        }
                        thisMember.addLocation(x, i, childFloorSpace[i][x]);
                    }
                    if(i+1==rowAmount-1){
                        onFinalRow =true;
                    }
                }else{
                    if(remainderRow!=0) {
                        childFloorSpace[i] = new Integer[remainderRow];
                        for (int x = 0; x < remainderRow; x++) {
                            //random number between 0 (reps a hole) and the amount of flavors
                            if(x<=crossOverPoint){
                                childFloorSpace[i][x]=selectedMember.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }else{
                                childFloorSpace[i][x]= finalSelectedMemberMate.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }
                            //will only make the hole if the random boolean is true and we can afford to add more holes
                        }
                    }else{
                        childFloorSpace[i] = new Integer[10];
                        for (int x = 0; x < 10; x++) {
                            //random number between 0 (reps a hole) and the amount of flavors
                            if(x<=crossOverPoint){
                                childFloorSpace[i][x]=selectedMember.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }else{
                                childFloorSpace[i][x]= finalSelectedMemberMate.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }
                        }
                    }
                }
            }
            thisMember.addRoom(childFloorSpace);

            ArrayList<Double> averageLengths=new ArrayList<>();
            for (Integer key: thisMember.getHashmap().keySet()) {
                ArrayList<Integer[]> coords=thisMember.getHashmap().get(key);
                //determine the fitness based on the highest proximity of a machine compared to the other machines
                double curFitness=0;
                double currentBestAverage;
                if(coords.size()>1&&key!=0){
                    double thisAverage=0;
                    for(int i=0;i<coords.size();i++){
                        for (Integer[] coord : coords) {
                            //maybe change this so we get the best average distance of key and average those best averages together with the affinity metric...we'll see how it goes first
                            double distance = Math.sqrt((Math.pow((coords.get(i)[0] - coord[0]), 2)) + (Math.pow((coords.get(i)[1] - coord[1]), 2)));
                            thisAverage += distance;
                        }
                    }
                    thisAverage=(thisAverage/(coords.size()*(coords.size()-1)))/key;
                    averageLengths.add(thisAverage);
                }
            }
            double averageBetweenTwoSameValues=0.0;
            for (Double averageLength : averageLengths) {
                averageBetweenTwoSameValues += averageLength;
            }
            averageBetweenTwoSameValues=averageBetweenTwoSameValues/averageLengths.size();
            thisMember.changeFitness(1/averageBetweenTwoSameValues);
            childrenListSyn.add(thisMember);
         return thisMember;
        };

        tasks=new ArrayList<>();
        List<Future<member>> allFloorsholder;
        for(int i=0;i<300;i++){
            tasks.add(createChildren);
        }

        allFloorsholder=service.invokeAll(tasks);
        System.out.println("CHILDREN: ");
        for(int i=0;i<300;i++){
            member member=childrenListSyn.get(i);
            Integer[][] thisFloor=member.getRoom();
            if(i==299){
                System.out.println("Final floor");
            }
            for(int y=0;y<rowAmount;y++){
                for(int x=0;x<thisFloor[y].length ;x++){
                    System.out.print(thisFloor[y][x]+"|");
                }
                System.out.println();
                System.out.println("________________________");
            }
            System.out.println(member.getFitness());
            System.out.println("++++++++++++");
        }

    }

    public static List<Future<member>> getFirstGen() throws InterruptedException {
        Callable<member> roomCreation= () -> {
            member thisMember=new member();
            boolean onFinalRow= spaces <= 10;
            int maxHolesLeft=spaces-machines;

            Integer[][] floorSpace =new Integer[rowAmount][];
            for (int i=0;i<rowAmount;i++){
                if (!onFinalRow){
                    floorSpace[i]=new Integer[10];
                    for(int x=0;x<10;x++) {
                        //random number between 0 (reps a hole) and the amount of flavors
                        floorSpace[i][x]=ThreadLocalRandom.current().nextInt(1, flavors+1);
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
                            floorSpace[i][x] = ThreadLocalRandom.current().nextInt(1, flavors+1);
                            thisMember.addLocation(x, i, floorSpace[i][x]);
                            //will only make the hole if the random boolean is true and we can afford to add more holes
                        }
                    }else{
                        floorSpace[i] = new Integer[10];
                        for (int x = 0; x < 10; x++) {
                            //random number between 0 (reps a hole) and the amount of flavors
                            floorSpace[i][x] =ThreadLocalRandom.current().nextInt(1, flavors+1);
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

            //calculate fitness using f(x)=1/((x+1)^2)
            ArrayList<Double> averageLengths=new ArrayList<>();
            for (Integer key: thisMember.getHashmap().keySet()) {
                ArrayList<Integer[]> coords=thisMember.getHashmap().get(key);
                //determine the fitness based on the highest proximity of a machine compared to the other machines
                double curFitness=0;
                double currentBestAverage;
                if(coords.size()>1&&key!=0){
                    double thisAverage=0;
                    for(int i=0;i<coords.size();i++){
                        for (Integer[] coord : coords) {
                            //maybe change this so we get the best average distance of key and average those best averages together with the affinity metric...we'll see how it goes first
                            double distance = Math.sqrt((Math.pow((coords.get(i)[0] - coord[0]), 2)) + (Math.pow((coords.get(i)[1] - coord[1]), 2)));
                            thisAverage += distance;
                        }
                    }
                    thisAverage=(thisAverage/(coords.size()*(coords.size()-1)))/key;
                    averageLengths.add(thisAverage);
                }
            }
            double averageBetweenTwoSameValues=0.0;
            for (Double averageLength : averageLengths) {
                averageBetweenTwoSameValues += averageLength;
            }
            averageBetweenTwoSameValues=averageBetweenTwoSameValues/averageLengths.size();
            thisMember.changeFitness(1/averageBetweenTwoSameValues);
            return thisMember;
        };
        List<Callable<member>>tasks=new ArrayList<>();
        List<Future<member>> allFloors;
        for(int i=0;i<300;i++){
            tasks.add(roomCreation);
        }

        allFloors=service.invokeAll(tasks);
        return allFloors;


    }


    public static List<Member> childrenRun(member finalSelectedMemberMate, member selectedMember){
        List<member>childrenList=new ArrayList<>();
        List<member>childrenListSyn=Collections.synchronizedList(childrenList);
        Callable <member> createChildren=()->{
            int crossOverPoint=ThreadLocalRandom.current().nextInt(0,rowAmount);
            //left side is initial parent, right side is the mate
            member thisMember=new member();
            boolean onFinalRow= spaces <= 10;
            Integer[][] childFloorSpace =new Integer[rowAmount][];
            for (int i=0;i<rowAmount;i++){
                if (!onFinalRow){
                    childFloorSpace[i]=new Integer[10];
                    for(int x=0;x<10;x++) {
                        //random number between 0 (reps a hole) and the amount of flavors
                        if(x<=crossOverPoint){
                            childFloorSpace[i][x]=selectedMember.getRoom()[i][x];
                            thisMember.addLocation(x, i, childFloorSpace[i][x]);
                        }else{
                            childFloorSpace[i][x]= finalSelectedMemberMate.getRoom()[i][x];
                            thisMember.addLocation(x, i, childFloorSpace[i][x]);
                        }
                        thisMember.addLocation(x, i, childFloorSpace[i][x]);
                    }
                    if(i+1==rowAmount-1){
                        onFinalRow =true;
                    }
                }else{
                    if(remainderRow!=0) {
                        childFloorSpace[i] = new Integer[remainderRow];
                        for (int x = 0; x < remainderRow; x++) {
                            //random number between 0 (reps a hole) and the amount of flavors
                            if(x<=crossOverPoint){
                                childFloorSpace[i][x]=selectedMember.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }else{
                                childFloorSpace[i][x]= finalSelectedMemberMate.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }
                            //will only make the hole if the random boolean is true and we can afford to add more holes
                        }
                    }else{
                        childFloorSpace[i] = new Integer[10];
                        for (int x = 0; x < 10; x++) {
                            //random number between 0 (reps a hole) and the amount of flavors
                            if(x<=crossOverPoint){
                                childFloorSpace[i][x]=selectedMember.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }else{
                                childFloorSpace[i][x]= finalSelectedMemberMate.getRoom()[i][x];
                                thisMember.addLocation(x, i, childFloorSpace[i][x]);
                            }
                        }
                    }
                }
            }
            thisMember.addRoom(childFloorSpace);

            ArrayList<Double> averageLengths=new ArrayList<>();
            for (Integer key: thisMember.getHashmap().keySet()) {
                ArrayList<Integer[]> coords=thisMember.getHashmap().get(key);
                //determine the fitness based on the highest proximity of a machine compared to the other machines
                double curFitness=0;
                double currentBestAverage;
                if(coords.size()>1&&key!=0){
                    double thisAverage=0;
                    for(int i=0;i<coords.size();i++){
                        for (Integer[] coord : coords) {
                            //maybe change this so we get the best average distance of key and average those best averages together with the affinity metric...we'll see how it goes first
                            double distance = Math.sqrt((Math.pow((coords.get(i)[0] - coord[0]), 2)) + (Math.pow((coords.get(i)[1] - coord[1]), 2)));
                            thisAverage += distance;
                        }
                    }
                    thisAverage=(thisAverage/(coords.size()*(coords.size()-1)))/key;
                    averageLengths.add(thisAverage);
                }
            }
            double averageBetweenTwoSameValues=0.0;
            for (Double averageLength : averageLengths) {
                averageBetweenTwoSameValues += averageLength;
            }
            averageBetweenTwoSameValues=averageBetweenTwoSameValues/averageLengths.size();
            thisMember.changeFitness(1/averageBetweenTwoSameValues);
            childrenListSyn.add(thisMember);
            return thisMember;
        };

        return null;
    }

    public static List<member> getParents(){
        List<member> parents=new ArrayList<>();
    }

}
