package com.hologramsciences;

import java.util.List;
import java.util.ArrayList;

public class Algorithms {
    /**
     *
     *  Compute the cartesian product of a list of lists of any type T
     *  the result is a list of lists of type T, where each element comes
     *  each successive element of the each list.
     *
     *  https://en.wikipedia.org/wiki/Cartesian_product
     *
     *  For this problem order matters.
     *
     *  Example:
     *
     *   listOfLists = Arrays.asList(
     *                         Arrays.asList("A", "B"),
     *                         Arrays.asList("K", "L")
     *                 )
     *
     *   returns:
     *
     *   Arrays.asList(
     *         Arrays.asList("A", "K"),
     *         Arrays.asList("A", "L"),
     *         Arrays.asList("B", "K"),
     *         Arrays.asList("B", "L")
     *   )
     *
     *
     *
     */
    public static final <T> List<List<T>> cartesianProductForLists(final List<List<T>> listOfLists) {
       /*

        ****iterative approach but it will not maintain sequence as per our expected output********

        int totalCartesianProduct = 1;
        List<List<T>> result = new ArrayList<>();
        for (List<T> list : listOfLists)
            totalCartesianProduct *= list.size();

        for (int i = 0; i < totalCartesianProduct; i++) {
            int index = 1;
            List<T> l = new ArrayList<>();
            for (List<T> list : listOfLists) {
                int x = i / index % list.size();
                l.add(list.get(x));
                index *= list.size();
            }
            result.add(l);
        }
        return result;

        */

        List<List<T>> result = new ArrayList<List<T>>();
        if (listOfLists.size() == 0){
            result.add(new ArrayList<>());
            return result;
        }

        List<T> firstList = listOfLists.get(0);
        List<List<T>> remainingList = cartesianProductForLists(listOfLists.subList(1, listOfLists.size()));
        for (T value : firstList) {
            for (List<T> list : remainingList) {
                ArrayList<T> l = new ArrayList<T>();
                l.add(value);
                l.addAll(list);
                result.add(l);
            }
        }
        return result;
    }

    /**
     *
     *  In the United States there are six coins:
     *  1¢ 5¢ 10¢ 25¢ 50¢ 100¢
     *  Assuming you have an unlimited supply of each coin,
     *  implement a method which returns the number of distinct ways to make totalCents
     */
    public static final long countNumWaysMakeChange(final int totalCents) {
        int[] coins = new int[] { 1, 5, 10, 25, 50, 100 };
        long[] ways = new long[totalCents + 1];
        ways[0] = 1;
        for (int coin : coins) {
            for (int i = 1; i <= totalCents; i++) {
                if (coin <= i)
                    ways[i] += ways[i - coin];
            }
        }
        return ways[totalCents];
    }
}
