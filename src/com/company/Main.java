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
        for (int i = 0; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
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

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        int [] array = generateArray(15000000);

        ///////////////////
        long startTime = System.currentTimeMillis();

        int[] result = divArray(array);
        long endTime = System.currentTimeMillis();

        System.out.println("Total execution time: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();

        ///////////////////

        var splitArr = splitArray(array,array.length/12);
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
            //System.out.println("Поток завершился" + max);
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
}
