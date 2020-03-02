package util;

import dao.UserInfoDao;
import entity.UserInfo;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

public class MockData {
  static UserInfoDao userInfoDao=new UserInfoDao();
  static JedisPool jedisPool=new JedisPool("139.129.90.200",6379);
    public static void mockDataBase(){


        try {
            List<UserInfo> userInfos=new ArrayList<UserInfo>();
            for(int i=0;i<10000;i++){
                UserInfo userInfo=new UserInfo(i);
                userInfos.add(userInfo);
            }
            userInfoDao.addUserInfoList(userInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void mockRedis(){
        GenericObjectPoolConfig config=new GenericObjectPoolConfig();
        config.setMaxIdle(8);
    }
    public static void main(String[] args){
        mockDataBase();
    }
}
