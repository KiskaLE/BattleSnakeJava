package com.battlesnake.starter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        Test t = new Test();
        List<List<String>> x = new ArrayList<>();
        List<String> y = new ArrayList<>();
        Collections.addAll(y, new String[]{"up", "down"});
        x.add(y);

        y = new ArrayList<>();
        Collections.addAll(y, new String[]{"left", "right"});
        x.add(y);

        y = new ArrayList<>();
        Collections.addAll(y, new String[]{"left", "right"});
        x.add(y);


        System.out.println(Arrays.toString(Arrays.stream(split(x, 3)).toArray()));
    }

    public Test() {
    }

    public static List<List<String>>[] split(List<List<String>> moves, int number){
        int min = 0;
        int max = moves.size();
        int result = max/number;
        List<List<String>>[] out = new List[number];
        boolean isOdd;
        if (moves.size()%number == 0){
            isOdd = false;
        }else{
            isOdd = true;
        }
        for (int i = 0; i < number; i++) {
            if (isOdd && i == 0){
                min = (max - (result))-1;
            }else{
                min = max - (result);
            }

            out[i] = new ArrayList<>();
            List<List<String>> get = out[i];
            for (int j = min; j < max; j++) {
                get.add(moves.get(j));
            }
            max = min;
        }
        return out;
    }
}
