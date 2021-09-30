package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Main {

    Random random = new Random();

    public static int[] generateArray(int count){
        Random rnd = new Random();
        int[] a = new int[count];
        for (int i = 0; i < count; i++) {
            a[i] = rnd.nextInt(49)+2;
        }
        return a;
    }

    public static int[] divArray(int [] array){
        int min = array[0];
        for (int j : array) {
            if (j < min) {
                min = j;
            }
        }
        for(int i=0;i<array.length;i++){
            array[i] = array[i]/min;
        }
        return array;
    }

    public static int[][] splitArray(int[] inputArray, int chunkSize) {
        return IntStream.iterate(0, i -> i + chunkSize)
                .limit((int) Math.ceil((double) inputArray.length / chunkSize))
                .mapToObj(j -> Arrays.copyOfRange(inputArray, j, Math.min(inputArray.length, j + chunkSize)))
                .toArray(int[][]::new);
    }


    public static int[] ConvertFuture(ArrayList<Future<Integer>> list) throws ExecutionException, InterruptedException {
        int[] a = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            a[i] = list.get(i).get();
        }
        return a;
    }

    public static int FindMinSimple(int[] array) {
        int min = array[0];
        for (int j : array) {
            if (j < min) {
                min = j;
            }
        }
        return min;
    }

    public static int[][] getArr(ArrayList<Future<int[]>> list) throws ExecutionException, InterruptedException {
        int[][] a = new int[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            a[i] = list.get(i).get();
        }
        return a;
    }

    public static class FindInt implements Callable<Integer> {
        int[] a;
        int min;

        public FindInt(int[] a) {
            this.a = a;
        }

        @Override
        public Integer call() throws Exception {
            min = Integer.MAX_VALUE;
            for (int j : a) {
                if (j < min) {
                    min = j;
                }
            }
            return min;
        }
    }

    public static class DivInt implements Callable<int[]> {
        int[] a;
        int min;

        public DivInt(int[] a, int min) {
            this.a = a;
            this.min = min;
        }
        @Override
        public int[] call() throws Exception {

            for(int i = 0;i<a.length;i++){
                a[i] = a[i]/min;
            }
            return a;
        }
    }

    public static class MinFork extends RecursiveTask<Integer>{

        private final int[] array;
        int i;
        int j;

        public MinFork(int[] array, int i, int j){
            this.array = array;
            this.i = i;
            this.j = j;
        }

        @Override
        protected Integer compute() {
            if(j- i <= 10000) {
                return computeDirectly();
            }
            MinFork right = new MinFork(array,i,((j-i)/2)+i);
            MinFork left = new MinFork(array, ((j-i)/2)+i, j);
            right.fork();
            left.fork();
            return computeDirectly();
        }
        private Integer computeDirectly() {
            int min = Integer.MAX_VALUE;
            for (int j : array) {
                if (j < min) {
                    min = j;
                }
            }
            return min;
        }
    }

    public static class DivFork extends RecursiveTask<int[]>{

        int min;
        int[] array;
        int i;
        int j;

        public DivFork(int min, int[] array,int i,int j){
            this.min = min;
            this.array = array;
            this.i = i;
            this.j = j;
        }

        @Override
        protected int[] compute(){
            if(j-i <= 10000) {
                return divArr();
            }
            MinFork right = new MinFork(array,i,((j-i)/2)+i);
            MinFork left = new MinFork(array, ((j-i)/2)+i, j);
            right.fork();
            left.fork();
            return null;
        }
        private int[] divArr(){
            for (int i = 0; i<array.length;i++){
                array[i] = array[i]/min;
            }
            return array;
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        int [] array = generateArray(15000000);
        int[] array_test = array.clone();
        ///////////////////
        long startTime = System.currentTimeMillis();

        int[] result = divArray(array_test);
        long endTime = System.currentTimeMillis();

        System.out.println("Total execution time: " + (endTime - startTime) + "ms");

        /////////////////////
        array_test = array.clone();
        startTime = System.currentTimeMillis();
        var splitArr = splitArray(array_test,array.length/6);
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(6,12,1, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<>());
        ArrayList<Future<Integer>> Ints = new ArrayList<Future<Integer>>();
        ArrayList<Future<int[]>> Divs = new ArrayList<Future<int[]>>();

        for (var i : splitArr) {
            FindInt fi = new FindInt(i);
            Ints.add(tpe.submit(fi));
        }

        int[] newArray = ConvertFuture(Ints);
        int min = FindMinSimple(newArray);

        for(var i : splitArr){
            DivInt di = new DivInt(i,min);
            Divs.add(tpe.submit(di));
        }

        int[][] resultArr = getArr(Divs);

        endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) + "ms");
        tpe.shutdown();

        //////////////////////////
        array_test = array.clone();
        startTime = System.currentTimeMillis();
        MinFork fork = new MinFork(array_test,0,array_test.length);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        int min_fork = forkJoinPool.invoke(fork);
        DivFork div_fork = new DivFork(min_fork,array_test,0,array_test.length);
        forkJoinPool.invoke(div_fork);
        endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) + "ms");
    }
}
