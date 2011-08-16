package test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ForkJoinTest {
    private final int NUMBER_OF_CPU = Runtime.getRuntime().availableProcessors();
    // maximum number of SlowNumber per fork to perform add operation upon
    private final int NUMBERS_PER_FORK = 5;
    // number of milliSeconds simulating the amount of time per add operation
    private final int TIME_PER_OPERATION_IN_MILLIS = 10;

    // just a set of numbers on which addition will be performed
    // this is just to ease the tester to write as many numbers as he feels like
    // a SlowNumber class is created for each number
    private final int [] dummyNumbers = new int [] {2,3,4,5,6,7,8,9,1,2,3,5,6,4,3,2,4,5,6,7,5,4,6,7,8,5,4,3,4,5,6,7,8};

    public static void main(String [] argv) {
        new ForkJoinTest().testIt();
    }

    private long toMillis(final long nanos) {
        return nanos / 1000000;
    }

    private double percentage(final long original, final long found) {
        return 100 * original / found;
    }

    private void testIt() {
        SlowNumber [] slowNumbers = new SlowNumber[dummyNumbers.length];
        for (int i = 0; i < slowNumbers.length; i++) {
            slowNumbers[i] = new SlowNumber(dummyNumbers[i]);
        }

        long start = System.nanoTime();
        final SlowNumber serialValue = new SlowNumber();
        for (SlowNumber slowNumber : slowNumbers) {
            serialValue.add(slowNumber);
        }
        long end = System.nanoTime();
        final long serialExeTime = toMillis(end - start);
        System.out.println(String.format("Expected total: %s, took: %d ms", serialValue.value(), serialExeTime));

        if (serialExeTime == 0) {
            System.out.println("Calculation went too fast, consider raising the value of TIME_PER_OPERATION_IN_MILLIS");
            System.exit(-1);
        }

        for (int i = 0; i < NUMBER_OF_CPU; i++) {
            start = System.nanoTime();
            SlowAdd slowAdd = new SlowAdd(slowNumbers, 0, slowNumbers.length);
            ForkJoinPool addTaskPool = new ForkJoinPool(i + 1);
            final SlowNumber parallelValue = addTaskPool.invoke(slowAdd);
            end = System.nanoTime();
            final long parallelExeTime = toMillis(end - start);
            if (!serialValue.value().equals(parallelValue.value())) {
                throw new RuntimeException(String.format("Stupid programmer couldn't even get the addition right, " +
                                                         "expected: %s, found: %s", serialValue, parallelValue));
            }
            System.out.println(String.format("Found slowAdd total: %s, number of CPU: %s, took: %d ms",
                                             parallelValue.value(), i + 1, parallelExeTime));
            System.out.println(String.format("Speed up by: %s%%", percentage(serialExeTime, parallelExeTime)));
        }
    }

    class SlowAdd extends RecursiveTask<SlowNumber> {

        private final SlowNumber [] numbers;
        private final int fromIndex;
        private final int toIndex;

        // toIndex is exclusive
        SlowAdd(final SlowNumber [] numbers, final int fromIndex, final int toIndex) {
            this.numbers = numbers;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        @Override
        protected SlowNumber compute() {
            List<ForkJoinTask<SlowNumber>> childAddTasks = new ArrayList<>();
            // start from fromIndex
            int computeBeginning = fromIndex;
            // if more numbers than NUMBERS_PER_FORK
            if (toIndex - fromIndex > NUMBERS_PER_FORK) {
                // create forks with own fromIndex and toIndex to perform add operation
                for (int i = fromIndex; i < toIndex; i += NUMBERS_PER_FORK) {
                    if (i + NUMBERS_PER_FORK >= toIndex) {
                        // ok, have less or equal to NUMBERS_PER_FORK number of items, so don't fork
                        computeBeginning = i;
                        break;
                    }
                    SlowAdd slowAdd = new SlowAdd(numbers, i, i + NUMBERS_PER_FORK);
                    childAddTasks.add(slowAdd);
                    slowAdd.fork();
                }
            }

            // now perform addition on own share of numbers
            SlowNumber total = new SlowNumber();
            for (int i = computeBeginning; i < toIndex; i++) {
                total.add(numbers[i]);
            }

            if (!childAddTasks.isEmpty()) {
                for (ForkJoinTask<SlowNumber> childAddTask : childAddTasks) {
                    // add results created by children
                    total.add(childAddTask.join());
                }
            }
            return total;
        }
    }

    // A stupid number class that allows the user to add something to it
    // Caution: This class should NOT be used in production. As if you didn't know ;-)
    private class SlowNumber {

        private Long value = 0L;

        public SlowNumber() {
            this(0);
        }

        public SlowNumber(final long val) {
            value = val;
        }

        public void add(final SlowNumber number) {
            sleep(TIME_PER_OPERATION_IN_MILLIS);
            value += number.value;
        }

        public Long value() {
            return value;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }

        private void sleep(final int milliSeconds) {
            try {
                Thread.sleep(milliSeconds);
            } catch (InterruptedException e) {
                throw new RuntimeException("Got interrupted, calculation will not be correct. Please restart", e);
            }
        }
    }
}
