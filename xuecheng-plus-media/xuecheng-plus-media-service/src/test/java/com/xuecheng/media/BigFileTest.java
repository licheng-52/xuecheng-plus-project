package com.xuecheng.media;


import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;



import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 大文件测试
 *
 * @author licheng
 * @date 2023/03/13
 */
public class BigFileTest{

    //测试文件分块方法
    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("D:\\develop\\upload\\52.mp4");

        String chunkPath = "D:\\develop\\upload\\chunk\\";

        File chunkFile = new File(chunkPath);

        if(!chunkFile.exists()){
            chunkFile.mkdirs();
        }
        //分块大小
        long chunkSize = 1024 * 1024 * 1;
        //分块数量
        long chunkNum = (long) Math.ceil(sourceFile.length()*1.0/chunkSize);

        byte[] bytes = new byte[1024];

        RandomAccessFile read = new RandomAccessFile(sourceFile,"r");
        for (long i = 0; i < chunkNum; i++) {
            File file = new File(chunkPath+i);
            if(file.exists()){
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if(newFile){
                RandomAccessFile write = new RandomAccessFile(file,"rw");
                int len = -1;
                while((len = read.read(bytes)) != -1){
                    write.write(bytes,0,len);
                    if(file.length() >= chunkSize){
                        break;
                    }
                }
                write.close();
            }
        }
        read.close();
    }


    @Test
    public void testMerge() throws IOException {
        //块文件目录
        File chunkFolder = new File("D:\\develop\\upload\\chunk\\");
        //原始文件
        File originalFile = new File("D:\\develop\\upload\\52.mp4");
        //合并文件
        File mergeFile = new File("D:\\develop\\upload\\52_1.mp4");

        if(mergeFile.exists()){
            mergeFile.delete();
        }

        mergeFile.createNewFile();

        RandomAccessFile write = new RandomAccessFile(mergeFile,"rw");

        write.seek(0);

        byte[] bytes = new byte[1024];

        File[] listFiles = chunkFolder.listFiles();

        List<File> fileList = Arrays.asList(listFiles);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.valueOf(o1.getName()) - Integer.valueOf(o2.getName());
            }
        });

        for (File file : fileList) {
            RandomAccessFile read = new RandomAccessFile(file,"rw");
            int len = -1;
            while ((len = read.read(bytes)) != -1){
                write.write(bytes,0,len);
            }
            read.close();
        }
        write.close();

        FileInputStream fileInputStream1 = new FileInputStream(originalFile);
        FileInputStream fileInputStream = new FileInputStream(mergeFile);
        String originalMd5 = DigestUtils.md5Hex(fileInputStream1);
        //取出合并文件的md5进行比较
        String mergeFileMd5 = DigestUtils.md5Hex(fileInputStream);
        if (originalMd5.equals(mergeFileMd5)) {
            System.out.println("合并文件成功");
        } else {
            System.out.println("合并文件失败");
        }

    }


}
