import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis_case.DistributedLock;
import redis_case.RedisSnowCrash;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DistributedLockApplication {
 public static int count=500;
    private static Logger logger = LoggerFactory.getLogger(DistributedLockApplication.class);
    public static String execute(DistributedLock redisLock) throws InterruptedException {
        int clientcount = 500;
        CountDownLatch countDownLatch = new CountDownLatch(clientcount);
        ExecutorService executorService = Executors.newFixedThreadPool(clientcount);
        long start = System.currentTimeMillis();
        for (int i = 0; i < clientcount; i++) {
            executorService.execute(() -> {
                //通过Snowflake算法获取唯一的ID字符串,可以采用 mac地址+进程ID+线程ID的方式
                String id = String.valueOf(Thread.currentThread().getId());
                boolean lock=redisLock.lockWithTimeout(id, 30, 60000);
                    if(lock){
                        logger.info("线程"+Thread.currentThread().getId()+"-------获得锁，开始执行,count"+count--);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        boolean releaseLock=redisLock.releaseLock(id);
                        if(releaseLock){
                            logger.info("线程"+Thread.currentThread().getId()+"-------释放锁完成"+releaseLock);
                        }else {
                            logger.info("线程"+Thread.currentThread().getId()+"-------释放锁失败"+releaseLock);
                        }
                    }else{
                        logger.info("线程"+Thread.currentThread().getId()+"-------自动放弃获取锁");
                    }

                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();
        logger.info("执行完毕，程序运行时间:"+(end-start)+"ms");
        return "Hello";
    }


    public static void main(String[] args) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(1200);
        config.setMaxIdle(800);
        config.setMinIdle(200);
        config.setTestOnBorrow(true);
        config.setMaxWaitMillis(1000);
        JedisPool jedisPool = new JedisPool(config, "139.129.90.200", 6379, 100000, "zhr_redis");
        DistributedLock distributedLock = new DistributedLock(jedisPool);

        try {
            execute(distributedLock);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


}
