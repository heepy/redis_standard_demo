import dao.UserInfoDao;
import entity.UserInfo;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis_case.DistributedLock;
import redis_case.DoubleWrite;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application {
public static void main(String[] args){
    GenericObjectPoolConfig config =new GenericObjectPoolConfig();

    JedisPool jedisPool=new JedisPool(config,"139.129.90.200",6379,2000,"zhr_redis");



    UserInfoDao userInfoDao=new UserInfoDao();
    DoubleWrite doubleWrite=new DoubleWrite(jedisPool,userInfoDao);
    for (int i=0;i<5000;i++){
        String key=String.valueOf(i);
        System.out.println(doubleWrite.getData(key));
    }
    DistributedLock lock=new DistributedLock(jedisPool);
    lock.lockWithTimeout("lock",1,1);
}
}

