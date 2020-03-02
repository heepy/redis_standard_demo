import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;
import redis_case.DistributedLock;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DistributedLockApplication {
 public static int count=1000;
    public static String execute(DistributedLock redisLock) throws InterruptedException {
        int clientcount = 1000;
        CountDownLatch countDownLatch = new CountDownLatch(clientcount);

        ExecutorService executorService = Executors.newFixedThreadPool(clientcount);
        long start = System.currentTimeMillis();
        for (int i = 0; i < clientcount; i++) {
            executorService.execute(() -> {

                //通过Snowflake算法获取唯一的ID字符串,可以采用 mac地址+进程ID+线程ID的方式
                String id = String.valueOf(Thread.currentThread().getId());
                boolean lock=redisLock.lockWithTimeout(id, 60000, 99999);
                    if(lock){
                        System.out.println("线程"+Thread.currentThread().getId()+"-------获得锁，开始执行,count"+count--);

                    }else{
                        System.out.println("线程"+Thread.currentThread().getId()+"-------获取锁失败");
                    }

                boolean releaseLock=redisLock.releaseLock(id);
                if(releaseLock){
                    System.out.println("线程"+Thread.currentThread().getId()+"-------释放锁完成"+releaseLock);
                }else {
                    System.out.println("线程"+Thread.currentThread().getId()+"-------释放锁失败"+releaseLock);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();

        return "Hello";
    }


    public static void main(String[] args) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(10000);
        config.setMaxIdle(800);
        config.setMinIdle(200);
        JedisPool jedisPool = new JedisPool(config, "139.129.90.200", 6379, 2000, "zhr_redis");
        DistributedLock distributedLock = new DistributedLock(jedisPool);

        try {
            execute(distributedLock);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


}
