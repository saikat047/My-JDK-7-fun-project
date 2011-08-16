package test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ForkJoinTest {

    private final int NUMBER_OF_CPU = Runtime.getRuntime().availableProcessors();
    private final int NUMBERS_PER_FORK = 5;
    private final int TIME_PER_OPERATION_IN_MILLIS = 10;

    private final int [] dummyNumbers = new int [] {2,3,4,5,6,7,8,9,1,2,3,5,6,4,3,2,4,5,6,7,5,4,6,7,8,5,4,3,4,5,6,7,8};

    public static void main(String [] argv) {
        new ForkJoinTest().testIt();
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
        final long serialResultTimeInMillis = (end - start) / 1000000;
        System.out.println(String.format("Expected total: %s, took: %d ms", serialValue.value(), serialResultTimeInMillis));

        if (serialResultTimeInMillis == 0) {
            System.out.println("Calculation went too fast, consider raising the delay per add operation");
            System.exit(-1);
        }

        for (int i = 0; i < NUMBER_OF_CPU; i++) {
            start = System.nanoTime();
            SlowAdd slowAdd = new SlowAdd(slowNumbers, 0, slowNumbers.length);
            ForkJoinPool addTaskPool = new ForkJoinPool(i + 1);
            final SlowNumber parallelValue = addTaskPool.invoke(slowAdd);
            end = System.nanoTime();
            final long parallelResultTimeInMillis = (end - start) / 1000000;
            if (!serialValue.value().equals(parallelValue.value())) {
                throw new RuntimeException(String.format("Stupid programmer couldn't even get the addition right, " +
                                                         "expected: %s, found: %s", serialValue, parallelValue));
            }
            System.out.println(String.format("Found slowAdd total: %s, number of CPU: %s, took: %d ms",
                                             parallelValue.value(), i + 1, parallelResultTimeInMillis));
            System.out.println(String.format("Speed up by: %s%%", 100 * serialResultTimeInMillis / parallelResultTimeInMillis));
        }
    }

    class SlowAdd extends RecursiveTask<SlowNumber> {

        private final SlowNumber [] numbers;
        private final int fromIndex;
        private final int toIndex;

        SlowAdd(final SlowNumber [] numbers, final int fromIndex, final int toIndex) {
            this.numbers = numbers;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        @Override
        protected SlowNumber compute() {
            List<ForkJoinTask<SlowNumber>> childAddTasks = new ArrayList<>();
            int myStart = fromIndex;
            if (toIndex - fromIndex > NUMBERS_PER_FORK) {
                for (int i = fromIndex; i < toIndex; i += NUMBERS_PER_FORK) {
                    if (i + NUMBERS_PER_FORK >= toIndex) {
                        myStart = i;
                        break;
                    }
                    SlowAdd slowAdd = new SlowAdd(numbers, i, i + NUMBERS_PER_FORK);
                    childAddTasks.add(slowAdd);
                    slowAdd.fork();
                }
            }

            SlowNumber total = new SlowNumber();
            for (int i = myStart; i < toIndex; i++) {
                total.add(numbers[i]);
            }

            if (!childAddTasks.isEmpty()) {
                for (ForkJoinTask<SlowNumber> childAddTask : childAddTasks) {
                    total.add(childAddTask.join());
                }
            }
            return total;
        }
    }

    private class SlowNumber {

        private Long value = 0L;

        SlowNumber() {
            this(0);
        }

        SlowNumber(final long val) {
            value = val;
        }

        void add(final SlowNumber number) {
            sleep(TIME_PER_OPERATION_IN_MILLIS);
            value += number.value;
        }

        Long value() {
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
