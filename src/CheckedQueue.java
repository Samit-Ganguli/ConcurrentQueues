import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class CheckedQueue {
    
    public void testAdd() {
        int arrayLength = 10;
        Queue<String> abq = Collections.checkedQueue(new ArrayBlockingQueue<>(arrayLength), String.class);
        for (int i = 0; i < arrayLength; i++) {
            abq.add(Integer.toString(i));
        }
        try {
            abq.add("full");
        } catch (IllegalStateException full) {
        }
    }
    
    public void testAddFail1() {
        int arrayLength = 10;
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue(arrayLength + 1);

        for (int i = 0; i < arrayLength; i++) {
            abq.add(Integer.toString(i));
        }

        Queue q = Collections.checkedQueue(abq, String.class);
        q.add(0);
    }

    public void testAddFail2() {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue(1);
        Queue q = Collections.checkedQueue(abq, String.class);
        q.add(0);
    }

    public void testArgs() {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue(1);
        Queue q;

        try {
            q = Collections.checkedQueue(null, String.class);
        } catch(NullPointerException npe) {
        }

        try {
            q = Collections.checkedQueue(abq, null);
        } catch(Exception e) {
        }

        try {
            q = Collections.checkedQueue(null, null);
        } catch(Exception e) {
        }
    }

    public void testOffer() {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue(1);
        Queue q = Collections.checkedQueue(abq, String.class);

        try {
            q.offer(null);
        } catch (NullPointerException npe) {
        }

        try {
            q.offer(0);
        } catch (ClassCastException cce) {
        }
    }
}