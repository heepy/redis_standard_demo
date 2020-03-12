import dao.UserInfoDao;
import entity.UserInfo;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis_case.DoubleWrite;
import redis_case.RedisSnowCrash;

public class DoubleWriteApplication {
    private static Logger logger = LoggerFactory.getLogger(DoubleWriteApplication.class);

    public static void main(String[] args) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(1200);
        config.setMaxIdle(800);
        config.setMinIdle(200);
        config.setTestOnBorrow(true);
        config.setMaxWaitMillis(1000);
        JedisPool jedisPool = new JedisPool(config, "139.129.90.200", 6379, 10000, "zhr_redis");

        UserInfoDao userInfoDao = new UserInfoDao();
        testCacheThrough(jedisPool, userInfoDao);
    }

    public static void testCacheThrough(JedisPool jedisPool, UserInfoDao userInfoDao) {

        DoubleWrite doubleWrite = new DoubleWrite(jedisPool, userInfoDao);
        String result = doubleWrite.getDataIsEmpty("312");
        logger.info("结果是：" + result);
    }
}
