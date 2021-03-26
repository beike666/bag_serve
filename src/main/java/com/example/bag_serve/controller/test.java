package com.example.bag_serve.controller;

/**
 * @program: bag_serve
 * @description
 * @author: BeiKe
 * @create: 2021-03-26 21:03
 **/
import java.util.*;

class test {
    private static final int S = 110;


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入物品组数：");
        int N = sc.nextInt();            // 物品组数
        System.out.println("请输入背包容积：");
        int C = sc.nextInt();            // 背包容积

        int[][] v = new int[N+1][S];        // 体积
        int[][] w = new int[N+1][S];        // 价值
        int[] s = new int[N+1];

        for (int i = 1; i <= N; i++) {
            System.out.println("请输入第"+i+"组的物品个数");
            s[i] = sc.nextInt();
            for (int j = 0; j < s[i]; j++) {
                System.out.println("请输入价值：");
                v[i][j] = sc.nextInt();
                System.out.println("请输入重量：");
                w[i][j] = sc.nextInt();
            }
        }

        int[] dp = new int[C+1];
        for (int i = 1; i <= N; i++) {
            for (int j = C; j >= 0; j--) {
                for (int k = 0; k < s[i]; k++) {
                    if(j>=v[i][k]) {
                        dp[j] = Math.max(dp[j], dp[j - v[i][k]] + w[i][k]);
                    }
                }
            }
        }
        System.out.println(dp[C]);
    }
}
