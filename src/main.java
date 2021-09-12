import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class main {
    public static void main(String[] args){
        Lock lock=new ReentrantLock();
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
        final List<Integer[][]> list= new ArrayList<>();
        final List<Integer[][]> synList=Collections.synchronizedList(list);

        Runnable roomCreation= () -> {
            boolean onFinalRow= spaces <= 10;
            int maxHolesLeft=spaces-machines;

            Integer[][] floorSpace =new Integer[rowAmount][];
            for (int i=0;i<rowAmount;i++){
                if (!onFinalRow){
                    floorSpace[i]=new Integer[10];
                    for(int x=0;x<10;x++) {
                        //random number between 0 (reps a hole) and the amount of flavors
                        floorSpace[i][x]=ThreadLocalRandom.current().nextInt(1, flavors+1);
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
                            //will only make the hole if the random boolean is true and we can afford to add more holes
                        }
                    }else{
                        floorSpace[i] = new Integer[10];
                        for (int x = 0; x < 10; x++) {
                            //random number between 0 (reps a hole) and the amount of flavors
                            floorSpace[i][x] =ThreadLocalRandom.current().nextInt(1, flavors+1);
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
                floorSpace[yAxis][xAxis]=0;
            }

            synList.add(floorSpace);
            System.out.println(synList.size());
            if(synList.size()==300){
                lock.lock();
                System.out.println("all done");
                    floorSpace=synList.get(299);
                    for(int i=0;i<rowAmount;i++){
                        for(int x=0;x<floorSpace[i].length ;x++){
                            System.out.print(floorSpace[i][x]+"|");
                        }
                        System.out.println("");
                        System.out.println("________________________");
                    }
                lock.unlock();
                System.out.println(synList.size());
            }
        };

        for(int i=0;i<300;i++){
            service.execute(roomCreation);
        }
        //System.out.println(fitnessMeasurment(floorSpace));
        System.out.println("yo");
        Integer[][] example={
                {1,2,3,4,5,6,7,8,9}
        };



    }

    private static int fitnessMeasurment(Integer[][] floorSpace) {
       int fitness=0;
       for(int i=0;i<floorSpace.length;i++){
           for(int x=0;x<floorSpace[i].length;x++){
               //if we are in an evenly numbered space that isn't on an edge
               if(x%2==0&&i!=floorSpace.length-1&&x!=floorSpace[i].length-1){
                    //if the current number is odd, the number to the right and bottom is even, add one point to fitness
                   if(floorSpace[i][x]%2!=0&&floorSpace[i+1][x]%2==0&&floorSpace[i][x+1]%2==0){
                       fitness+=1;
                   }
                   //on a vertical edge
               }else if(x%2==0&&i==floorSpace.length-1&&x!=floorSpace[i].length-1){
                   if(floorSpace[i][x]%2!=0&&floorSpace[i][x+1]%2==0){
                       fitness+=1;
                   }
                   //on a horizontal edge
               }else  if(x%2==0&&i!=floorSpace.length-1&&x==floorSpace[i].length-1){
                   if(floorSpace[i][x]%2!=0&&floorSpace[i][x+1]%2==0){
                       fitness+=1;
                   }
               }else if(x%2==0&&i==floorSpace.length-1&&x==floorSpace[i].length-1){
                   if(floorSpace[i][x]%2!=0){
                       fitness+=1;
                   }
               }else if(x%2!=0&&i!=floorSpace.length-1&&x!=floorSpace[i].length-1){
                   //if the current number is even, the number to the right and bottom is odd, add one point to fitness
                   if(floorSpace[i][x]%2==0&&floorSpace[i+1][x]%2!=0&&floorSpace[i][x+1]%2!=0&&floorSpace[i][x]!=0){
                       fitness+=1;
                   }
                   //on a vertical edge
               }else if(x%2!=0&&i==floorSpace.length-1&&x!=floorSpace[i].length-1){
                   if(floorSpace[i][x]%2==0&&floorSpace[i][x+1]%2!=0&floorSpace[i][x]!=0){
                       fitness+=1;
                   }
                   //on a horizontal edge
               }else  if(x%2!=0&&i!=floorSpace.length-1&&x==floorSpace[i].length-1){
                   if(floorSpace[i][x]%2==0&&floorSpace[i+1][x]%2!=0&floorSpace[i][x]!=0){
                       fitness+=1;
                   }
                   //on the final entry
               }else if(x%2!=0&&i==floorSpace.length-1&&x==floorSpace[i].length-1){
                   if(floorSpace[i][x]%2==0){
                       fitness+=1;
                   }
               }
           }
       }

       return fitness;
    }
}
