import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SingleProducerMultipleConsumerLoops {
    static final long LONG_DELAY_MS = 1000;
    static ExecutorService pool;

    public static void main(String[] args) throws Exception {
        final int maxDequeuers = (args.length > 0)
            ? Integer.parseInt(args[0])
            : 7;
        long startTime = System.currentTimeMillis();
            
        pool = Executors.newCachedThreadPool();
            run(new ArrayBlockingQueue<Integer>(100), maxDequeuers, 500000);
        pool.shutdown();
        if (! pool.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
            throw new Error();
        pool = null;
        Long endTime = System.currentTimeMillis();
        System.out.println("total time of execution ::  "+(endTime-startTime));
   }

    static void run(BlockingQueue<Integer> queue, int dequeuers, int iters) throws Exception {
        new SingleProducerMultipleConsumerLoops(queue, dequeuers, iters).run();
    }

    final BlockingQueue<Integer> queue;
    final int consumers;
    final int iters;
    final CyclicBarrier barrier;
    Throwable fail;

    SingleProducerMultipleConsumerLoops(BlockingQueue<Integer> queue, int dequeuers, int iters) {
        this.queue = queue;
        this.consumers = dequeuers;
        this.iters = iters;
        this.barrier = new CyclicBarrier(dequeuers + 2);
    }

    void run() throws Exception {
        pool.execute(new Enqueuers());
        for (int i = 0; i < consumers; i++) {
            pool.execute(new Dequeuers());
        }
        barrier.await();
        barrier.await();
        if (fail != null) throw new AssertionError(fail);
    }

    abstract class CheckedRunnable implements Runnable {
        abstract void realRun() throws Throwable;
        public final void run() {
            try {
                realRun();
            } catch (Throwable t) {
                fail = t;
                t.printStackTrace();
                throw new AssertionError(t);
            }
        }
    }

    class Enqueuers extends CheckedRunnable {
        volatile int result;
        void realRun() throws Throwable {
            barrier.await();
            for (int i = 0; i < iters * consumers; i++) {
                queue.put(new Integer(i));
            }
            barrier.await();
            result = 432;
        }
    }

    class Dequeuers extends CheckedRunnable {
        volatile int result;
        void realRun() throws Throwable {
            barrier.await();
            int l = 0;
            int s = 0;
            int last = -1;
            for (int i = 0; i < iters; i++) {
                Integer item = queue.take();
                int v = item.intValue();
                if (v < last)
                    throw new Error("Out-of-Order transfer");
                last = v;
                s += l;
            }
            barrier.await();
            result = s;
        }
    }
}