import java.util.Random;
import java.util.Scanner;

public class main {
    public static void main(String[] args){
        int cores = Runtime.getRuntime().availableProcessors();
        //save 1 thread for ui
        int coresForProcessing=cores-1;
        //how many rows there will be
        int rowAmount;
        //the final row will hold the last of the spaces if need be, this number indicates how many spaces are in the row
        int remainderRow;
        int spacesLeft;
        boolean onFinalRow=false;
        Random random=new Random();
        Scanner scanner=new Scanner(System.in);
        System.out.println("How many spaces?");
        int spaces=scanner.nextInt();
        if(spaces<=10){
            onFinalRow=true;
        }
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
        int maxHolesLeft=spaces-machines;
        int machinesPlaced=0;
        System.out.println(rowAmount);
        System.out.println(remainderRow);
        if(remainderRow==0){
            spacesLeft=10*rowAmount;
        }else{
            spacesLeft=((10*(rowAmount-1)))+remainderRow;
        }

        Integer floorSpace[][]=new Integer[rowAmount][];
        for (int i=0;i<rowAmount;i++){
            if (!onFinalRow){
                floorSpace[i]=new Integer[10];
                for(int x=0;x<10;x++) {
                    //random number between 0 (reps a hole) and the amount of flavors
                    floorSpace[i][x]=random.nextInt(flavors)+1;
                    if(random.nextBoolean()&&maxHolesLeft>0&&machines-machinesPlaced!=spacesLeft){
                        floorSpace[i][x]=0;
                        maxHolesLeft--;

                    }else if(machinesPlaced>=machines||machines==0){
                        floorSpace[i][x]=0;
                        maxHolesLeft--;

                    }else {
                        machinesPlaced++;
                    }
                    spacesLeft--;
                }
                if(i+1==rowAmount-1){
                    onFinalRow=true;
                }
            }else{
                if(remainderRow!=0) {
                    floorSpace[i] = new Integer[remainderRow];
                    for (int x = 0; x < remainderRow; x++) {
                        //random number between 0 (reps a hole) and the amount of flavors
                        floorSpace[i][x] = random.nextInt(flavors)+1;
                        if(random.nextBoolean()&&maxHolesLeft>0&&machines-machinesPlaced!=spacesLeft){
                            floorSpace[i][x]=0;
                        }else if(machinesPlaced>=machines||machines==0){
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
                        //random number between 0 (reps a hole) and the amount of flavors
                        floorSpace[i][x] =random.nextInt(flavors)+1;
                        if(random.nextBoolean()&&maxHolesLeft>0&&machines-machinesPlaced!=spacesLeft){
                            floorSpace[i][x]=0;
                            maxHolesLeft--;
                        }else if(machinesPlaced>=machines||machines==0){
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
        for (int i=0; i<rowAmount;i++){
            System.out.print("|");
            for(int x=0; x<floorSpace[i].length;x++){
                System.out.print(floorSpace[i][x]+"|");
            }
            System.out.println("");
            System.out.println("_______________________");
        }


    }
}
