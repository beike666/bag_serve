package com.example.bag_serve.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.bag_serve.entity.Scatter;
import com.example.bag_serve.util.OrderUtil;
import com.example.bag_serve.util.ScatterUtil;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @program: bag_serve
 * @description
 * @author: BeiKe
 * @create: 2021-03-25 17:14
 **/
@RestController
@CrossOrigin
public class FileController {


    /**
     * 前端请求散点图数据
     * @param scatter
     * @return
     */
    @PostMapping("/get/scatter/data")
    public Object getScatterData(@RequestBody Scatter scatter){
        JSONObject jsonObject = new JSONObject();

//        传的数据不满足要求，返回失败
        if(!scatter.getFileName().equals("idkp1-10.txt")){
            if(scatter.getGroup()==11){
                jsonObject.put("status",202);
                return jsonObject;
            }
        }
//        将创建好的数据传递给前端
        ArrayList<ScatterUtil> scatterUtils = new ArrayList<>();
//        将数据分割成【重量，价值】的数组
        splitDataTwoGroup(scatter, scatterUtils);

//        返回结果
        if(scatterUtils.size()>0){
            jsonObject.put("status",200);
            jsonObject.put("data",scatterUtils);
            return jsonObject;
        }
        jsonObject.put("status",201);
        jsonObject.put("data",null);
        return jsonObject;

    }

    /**
     * 非递增排序
     * @param scatter
     * @return
     */
    @PostMapping("/order")
    public Object order(@RequestBody Scatter scatter){
        JSONObject jsonObject = new JSONObject();
//      传的数据不满足要求，返回失败
        if(!scatter.getFileName().equals("idkp1-10.txt")){
            if(scatter.getGroup()==11){
                jsonObject.put("status",202);
                return jsonObject;
            }
        }
        ArrayList<ScatterUtil> scatterUtils = new ArrayList<>();
//      将数据分割成【重量，价值】的数组
        splitDataTwoGroup(scatter, scatterUtils);
//        返回前端的数据列表
        ArrayList<OrderUtil> orderUtils = new ArrayList<>();
        for (int i = 0; i < scatterUtils.size(); i=i+3) {
//            封装数据
            OrderUtil orderUtil = new OrderUtil();
            List<ScatterUtil> item = scatterUtils.subList(i, i + 3);
            orderUtil.setItem(item);
            float rate = (float) item.get(2).getProfit() / item.get(2).getWeight();
            orderUtil.setRate(rate);
            orderUtils.add(orderUtil);
        }
        Collections.sort(orderUtils);
//        返回结果
        if (orderUtils.size()>0) {
            jsonObject.put("status",200);
            jsonObject.put("data",orderUtils);
            return jsonObject;
        }
        jsonObject.put("status",201);
        jsonObject.put("data",null);
        return jsonObject;

    }

    /**
     * 求某一组数据的最优解
     * @param scatter
     * @return
     */
    @PostMapping("/get/answer")
    public Object answer(@RequestBody Scatter scatter){
        JSONObject jsonObject = new JSONObject();
//        定义存放价值项集和重量项集的数组(此时数据已被分割)
        ArrayList<Integer> profitsList = new ArrayList<>();
        ArrayList<Integer> weightsList = new ArrayList<>();
//        获取前端的请求数据
        String fileName=scatter.getFileName();
        Integer group = scatter.getGroup();
        readFile(fileName,group,profitsList,weightsList);
//        获取当前组的容量
        int volume=volumeCount(scatter.getFileName(),scatter.getGroup());
        if(scatter.getType()==0){
//            采用动态规划算法
            long startTime=System.currentTimeMillis();   //获取开始时间
            int answer=dp(profitsList,weightsList,volume);
            long endTime=System.currentTimeMillis(); //获取结束时间
            jsonObject.put("status",200);
            jsonObject.put("answer",answer);
            jsonObject.put("runtime",endTime-startTime);
            return jsonObject;
        }else{
//            采用回溯算法
        }
        return jsonObject;
    }

    /**
     * 动态规划算法
     * @param profitsList
     * @param weightsList
     * @param volume
     */
    private int dp(ArrayList<Integer> profitsList, ArrayList<Integer> weightsList, int volume) {
//        组数
        int N=profitsList.size()/3;
//        每一个项集的元素个数
        int S=3;
        int[][] v = new int[N+1][S];        // 全部体积（每S个为一组）
        int[][] w = new int[N+1][S];        // 价值（每S个为一组）
//        将数据存为动态规划需要的格式
        int index=0;
        for (int i = 1; i <= N; i++) {
            List<Integer> pi = profitsList.subList(index, index + 3);
            int[] pa = pi.stream().mapToInt(Integer::intValue).toArray();
            w[i]=pa;
            List<Integer> wi = profitsList.subList(index, index + 3);
            int[] wa = wi.stream().mapToInt(Integer::intValue).toArray();
            w[i]=wa;
            index=index+3;
        }
//        算法
        int[] dp = new int[volume+1];
        for (int i = 1; i <= N; i++) {
            for (int j = volume; j >= 0; j--) {
                for (int k = 0; k < 3; k++) {
                    if(j>=v[i][k]) {
                        dp[j] = Math.max(dp[j], dp[j - v[i][k]] + w[i][k]);
                    }
                }
            }
        }
        System.out.println(dp[volume]);
        return dp[volume];

    }


    /**
     * 将数据分割成【重量，价值】的数组
     * @param scatter
     * @param scatterUtils
     */
    private void splitDataTwoGroup(@RequestBody Scatter scatter, ArrayList<ScatterUtil> scatterUtils) {
//        定义存放价值项集和重量项集的数组(此时数据已被分割)
        ArrayList<Integer> profitsList = new ArrayList<>();
        ArrayList<Integer> weightsList = new ArrayList<>();
//        获取前端的请求数据
        String fileName=scatter.getFileName();
        Integer group = scatter.getGroup();
        readFile(fileName,group,profitsList,weightsList);

        for (int i = 0; i < profitsList.size(); i++) {
            ScatterUtil scatterUtil = new ScatterUtil();
            scatterUtil.setWeight(weightsList.get(i));
            scatterUtil.setProfit(profitsList.get(i));
            scatterUtils.add(scatterUtil);
        }
    }



    /**
     * 创建文件对象，清洗数据的前期准备
     */
//    抛异常的注解
    @SneakyThrows
    public void readFile(String fileName,Integer group,ArrayList<Integer> profitsList,ArrayList<Integer> weightsList) {
//        所有数据文件的公共路径
        String publicFilePath=System.getProperty("user.dir")+System.getProperty("file.separator")
                +"data";
        //具体文件路径
        String filePath=publicFilePath+System.getProperty("file.separator")+fileName;
//        创建一个文件对象
        File file = new File(filePath);

//        用BufferedReader类来进行读取内容
        BufferedReader reader = new BufferedReader(new FileReader(file));

//        定义存放价值项集和重量项集的数组(此时数据还都是字符串)
        ArrayList<String> profits = new ArrayList<>();
        ArrayList<String> weights = new ArrayList<>();
//        清洗数据
        clearData(reader, profits, weights);

        splitData(group, profitsList, profits);
        splitData(group, weightsList, weights);
    }

    /**
     * 分割具体某一组数据
     * @param group
     * @param divided
     * @param undivided
     */
    private void splitData(Integer group, ArrayList<Integer> divided, ArrayList<String> undivided) {
        String[] profitSplit = undivided.get(group - 1).split(",");
        String number=profitSplit[profitSplit.length-1];
        String a = number.replace(".", "");
        profitSplit[profitSplit.length-1]=a;
        for (String s : profitSplit) {
            divided.add(Integer.parseInt(s));
        }
    }

    /**
     * 将文件中的特定数据读取到列表中
     * @param reader
     * @param profits
     * @param weights
     * @throws IOException
     */
    private static void clearData(BufferedReader reader, ArrayList<String> profits, ArrayList<String> weights) throws IOException {
        String s=null;
//            读取数据的标志字符串
        String profit="The profit of items are:";
        String weight="The weight of items are:";
//            读取数据的标志位
        int flag1=0;
        int flag2=0;
        ArrayList<Integer> volumes = new ArrayList<>();
        while ((s = reader.readLine()) != null) {
            if (s.contains(profit)) {
                flag1=1;
                continue;
            }else if (s.contains(weight)) {
                flag2=1;
                continue;
            }
            if(flag1==1){
                profits.add(s);
                flag1=0;
            }else if(flag2==1){
                weights.add(s);
                flag2=0;
            }
        }
    }

    @SneakyThrows
    private int volumeCount(String fileName, Integer group){
        //        所有数据文件的公共路径
        String publicFilePath=System.getProperty("user.dir")+System.getProperty("file.separator")
                +"data";
        //具体文件路径
        String filePath=publicFilePath+System.getProperty("file.separator")+fileName;
//        创建一个文件对象
        File file = new File(filePath);

//        用BufferedReader类来进行读取内容
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String s=null;
        String volumeWord="the cubage of knapsack is";
        ArrayList<Integer> volumes = new ArrayList<>();
        while ((s = reader.readLine()) != null) {
            if(s.contains(volumeWord)){
                String[] s1 = s.split(" ");
                String replace = s1[s1.length - 1].replace(".", "");
                volumes.add(Integer.parseInt(replace));
            }
        }
        return volumes.get(group-1);
    }
}
