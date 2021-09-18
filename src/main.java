import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class main {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors();
        //save 1 thread for UI
        int coresForProcessing=cores-1;
        ExecutorService service= Executors.newFixedThreadPool(coresForProcessing);
        int rowAmount;
        //the final row will hold the last of the spaces if need be, this number indicates how many spaces are in the row
        int remainderRow;
        Scanner scanner=new Scanner(System.in);
        System.out.println("How many spaces?");
        int spaces=scanner.nextInt();
        System.out.println("How many Machines?");
        int machines=scanner.nextInt();
        System.out.println("How many flavors?");
        int flavors=scanner.nextInt();
        if (spaces%10==0){
            rowAmount=spaces/10;
            remainderRow=0;
        }else{
            rowAmount=(spaces/10)+1;
            remainderRow=spaces%10;
        }
        //maximum amount of holes allowed, counts down to zero as holes are placed

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
            for (Integer key: thisMember.getHashmap().keySet()) {
                ArrayList<Integer[]> coords=thisMember.getHashmap().get(key);
                //determine the fitness based on the highest proximity of a machine compared to the other machines
                double curHighestFitness=0;
                if(coords.size()>0){
                    for(int i=0; i<coords.size();i++){
                        double thisFitness=0;
                        Integer[] comparedCoord=coords.get(i);
                        for(int z=1; z<coords.size(); z++){
                            Integer[] nextCoords=coords.get(z);
                            double distance=Math.sqrt((Math.pow((comparedCoord[0]+nextCoords[0]),2))+(Math.pow((comparedCoord[1]+nextCoords[1]),2)));
                            thisFitness+=1/(Math.pow(distance,2));
                        }
                        if(thisFitness>curHighestFitness){
                            curHighestFitness=thisFitness;
                        }
                    }
                    thisMember.changeFitness(curHighestFitness);
                }
            }
            return thisMember;
        };
        List<Callable<member>>tasks=new ArrayList<>();
        List<Future<member>> allFloors;
        for(int i=0;i<300;i++){
            tasks.add(roomCreation);
        }

        allFloors=service.invokeAll(tasks);

        //System.out.println(fitnessMeasurment(floorSpace));

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
        service=ForkJoinPool.commonPool();
        Lock lock=new ReentrantLock();
        final AtomicInteger selectionListIndex=new AtomicInteger(0);
        Callable<member>selection=()->{
            System.out.println(selectionListIndex.get());
            selectionListIndex.getAndAdd(1);
            return null;
        };
        for(int i=0;i<300;i++){
            service.submit(selection);
        }
    }

}
