package com.example.bag_serve.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.bag_serve.entity.Scatter;
import com.example.bag_serve.util.ScatterUtil;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;

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
//        定义存放价值项集和重量项集的数组(此时数据已被分割)
        ArrayList<Integer> profitsList = new ArrayList<>();
        ArrayList<Integer> weightsList = new ArrayList<>();
//        获取前端的请求数据
        String fileName=scatter.getFileName();
        Integer group = scatter.getGroup();
        readFile(fileName,group,profitsList,weightsList);
//        将创建好的数据传递给前端
        ArrayList<ScatterUtil> scatterUtils = new ArrayList<>();
        for (int i = 0; i < profitsList.size(); i++) {
            ScatterUtil scatterUtil = new ScatterUtil();
            scatterUtil.setWeight(weightsList.get(i));
            scatterUtil.setProfit(profitsList.get(i));

            scatterUtils.add(scatterUtil);
        }
        jsonObject.put("status",200);
        jsonObject.put("data",scatterUtils);
        return jsonObject;

    }


//    抛异常的注解

    /**
     * 创建文件对象，清洗数据的前期准备
     */
    @SneakyThrows
    public void readFile(String fileName,Integer group,ArrayList<Integer> profitsList,ArrayList<Integer> weightsList) {
//        所有数据文件的公共路径
        String publicFilePath=System.getProperty("user.dir")+System.getProperty("file.separator")
                +"data";
        //具体文件路径
        String filePath=publicFilePath+System.getProperty("file.separator")+fileName;
//        创建一个文件对象
        File file = new File(filePath);
//        创建一个StringBuilder对象，用于储存读取的内容
        StringBuilder stringBuilder = new StringBuilder();

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
}
