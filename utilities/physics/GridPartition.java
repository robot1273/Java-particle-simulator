package physics;
import java.util.ArrayList;

public class GridPartition {
    public class GridUnit{ 
        //public int capacity = 5; //max objects to store
        //public int count = 0; //num items stored
        public ArrayList<Integer> objects = new ArrayList<Integer>();// = new int[capacity];

        private void addItem(int item_id){
            objects.add(item_id);
            //if (count < capacity-1){count+=1;}
        }

        private void clear(){
            objects.clear();
            //objects = new int[capacity];
            //count = 0;
        }
    }

    public GridUnit[] partition;
    public int width, height;

    public GridPartition(int w, int h){
        partition = new GridUnit[w * h];
        for (int i = 0; i < w * h; i++){
            partition[i] = new GridUnit();
        }
        width = w;
        height = h;
    }

    public void addItem(int x, int y, int item_id){
        try {
            partition[x * height + y].addItem(item_id);
        } catch (Exception e) {
            //System.out.println("an error occured...");
        }
        
    }

    public void clear(){
        for (GridUnit unit : partition){
            unit.clear();
        }
    }

}
