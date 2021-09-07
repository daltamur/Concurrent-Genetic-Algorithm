import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class main {
    public static void main(String[] args){
        int cores = Runtime.getRuntime().availableProcessors();
        //save 1 thread for ui
        int coresForProcessing=cores-1;
        ExecutorService service= Executors.newFixedThreadPool(coresForProcessing);
        int rowAmount;
        //the final row will hold the last of the spaces if need be, this number indicates how many spaces are in the row
        int remainderRow;
        Random random=new Random();
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
        //System.out.println(rowAmount);
        //System.out.println(remainderRow);
        final List<Integer[][]> list= new ArrayList<>();
        final List<Integer[][]> synList=Collections.synchronizedList(list);

        Runnable roomCreation=new Runnable() {
            @Override
            public void run() {
                boolean onFinalRow=false;
                if(spaces<=10){
                    onFinalRow=true;
                }
                int spacesLeft=0;
                int machinesPlaced=0;
                int maxHolesLeft=spaces-machines;
                if(remainderRow==0){
                    spacesLeft =10*rowAmount;
                }else{
                    spacesLeft =((10*(rowAmount-1)))+remainderRow;
                }

                Integer floorSpace[][]=new Integer[rowAmount][];
                for (int i=0;i<rowAmount;i++){
                    if (!onFinalRow){
                        floorSpace[i]=new Integer[10];
                        for(int x=0;x<10;x++) {
                            boolean turnToHole=random.nextBoolean();
                            //random number between 0 (reps a hole) and the amount of flavors
                            floorSpace[i][x]=random.nextInt(flavors)+1;
                            if(turnToHole&& maxHolesLeft >0&&machines- machinesPlaced != spacesLeft){
                                floorSpace[i][x]=0;
                                maxHolesLeft--;

                            }else if(machinesPlaced >=machines||machines==0){
                                floorSpace[i][x]=0;
                                maxHolesLeft--;

                            }else {
                                machinesPlaced++;
                            }
                            spacesLeft--;
                        }
                        if(i+1==rowAmount-1){
                            onFinalRow =true;
                        }
                    }else{
                        if(remainderRow!=0) {
                            floorSpace[i] = new Integer[remainderRow];
                            for (int x = 0; x < remainderRow; x++) {
                                boolean turnToHole=random.nextBoolean();
                                //random number between 0 (reps a hole) and the amount of flavors
                                floorSpace[i][x] = random.nextInt(flavors)+1;
                                //will only make the hole if the random boolean is true and we can afford to add more holes
                                if(turnToHole&& maxHolesLeft >0&&machines- machinesPlaced != spacesLeft){
                                    floorSpace[i][x]=0;
                                }else if(machinesPlaced >=machines||machines==0){
                                    floorSpace[i][x]=0;
                                    maxHolesLeft--;
                                }else {
                                    machinesPlaced++;
                                }
                                spacesLeft--;
                            }
                        }else{
                            floorSpace[i] = new Integer[10];
                            for (int x = 0; x < 10; x++) {
                                boolean turnToHole=random.nextBoolean();
                                //random number between 0 (reps a hole) and the amount of flavors
                                floorSpace[i][x] =random.nextInt(flavors)+1;
                                if(turnToHole&& maxHolesLeft >0&&machines- machinesPlaced != spacesLeft){
                                    floorSpace[i][x]=0;
                                    maxHolesLeft--;
                                }else if(machinesPlaced >=machines||machines==0){
                                    floorSpace[i][x]=0;
                                    maxHolesLeft--;
                                }else {
                                    machinesPlaced++;
                                }
                                spacesLeft--;
                            }
                        }
                    }
                }
                synList.add(floorSpace);
            }
        };
        for(int i=0;i<300;i++){
            service.submit(roomCreation);
        }

        //System.out.println(fitnessMeasurment(floorSpace));
        if(synList.size()<300){
            Integer[][] floorSpace=synList.get(2);
            for(int i=0;i<rowAmount;i++){
                for(int x=0;x<floorSpace[i].length ;x++){
                    System.out.print(floorSpace[i][x]+"|");
                }
                System.out.println("");
                System.out.println("________________________");
            }
        }
        Integer[][] example={
                {1,2,3,4,5,6,7,8,9}
        };

        //System.out.println(fitnessMeasurment(example));


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
