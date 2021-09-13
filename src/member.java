import java.util.ArrayList;
import java.util.HashMap;

public class member {
    private Integer [][] floorSpace;
    private HashMap<Integer, ArrayList<Integer []>> valueLocations;
    private double fitness;

    public member() {
        this.fitness=0.0;
        this.valueLocations=new HashMap<>();
    }

    public void addLocation(int xCoord, int yCoord, int value){
        ArrayList<Integer[]> locations = valueLocations.computeIfAbsent(value, k -> new ArrayList<>());
        locations.add(new Integer[]{xCoord, yCoord});
        valueLocations.replace(value,locations);
    }

    public HashMap<Integer, ArrayList<Integer []>> getHashmap(){
        return valueLocations;
    }


    public void changeFitness(double value){
        fitness += value;
    }


    public void addRoom(Integer[][] room){
        this.floorSpace=room;
    }

    public Integer[][] getRoom(){
        return floorSpace;
    }

    public double getFitness(){
        return fitness;
    }


    public void removeValueFromHashmap(int xCoord, int yCoord, int value){
        ArrayList<Integer[]>list=valueLocations.get(value);
        list.removeIf(coords -> coords[0] == xCoord && coords[1] == yCoord);
    }


}
