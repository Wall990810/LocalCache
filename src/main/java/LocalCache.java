import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 本地缓存类
 * @Author: Wall
 * @version 1.0 2020/06/30
 */
public class LocalCache {

    /**缓存项默认存活时间*/
    private static final long SURVIVAL_TIME = 20 * 1000L;//20秒

    /**缓存清除执行间隔*/
    private static final long CLEAR_INTERVAL = 5 * 1000L;//5秒

    /**可缓存最大数目*/
    private static int MAX_CACHE_SIZE = 10;

    /**缓存项使用记录*/
    private static final Queue<String> entryUseRecord = new LinkedList<String>();

    /**读写锁*/
    private static ReentrantReadWriteLock readAndWriteLock = new ReentrantReadWriteLock();
    private static Lock writeLock = readAndWriteLock.writeLock();
    private static Lock readLock = readAndWriteLock.readLock();

    /**缓存Map*/
    private static final HashMap<String, CacheEntry> cacheMap = new HashMap<String, CacheEntry>();

    private LocalCache(){
        timeoutClear();
    }

    /**
     * 本地缓存类单例
     */
    private static volatile LocalCache localCache = new LocalCache();
    public static LocalCache getInstance() {
        return localCache;
    }

    /**
     * 设置最大缓存数目
     * @param size 最大缓存数
     */
    public void setCacheSize(int size){
        MAX_CACHE_SIZE = size;
    }

    /**
     * 缓存定时清理
     */
    private void timeoutClear(){
       new Timer("MapClear", true).schedule(new TimerTask() {
            @Override
            public void run() {
                writeLock.lock();
                try {
                    Iterator<Map.Entry<String,CacheEntry>> iterator = cacheMap.entrySet().iterator();
                    while (iterator.hasNext()){
                        Map.Entry<String,CacheEntry> e = iterator.next();
                        if (e.getValue().getExpirationTime() < System.currentTimeMillis() && cacheMap.containsKey(e.getKey())){
                            if (e.getValue().getExpirationTime() < e.getValue().getCreateTime()){
                                continue;
                            }
                            iterator.remove();
                        }
                    }
                }finally {
                    writeLock.unlock();
                }
            }
        }, CLEAR_INTERVAL, CLEAR_INTERVAL);
    }

    /**
     * 创建缓存项
     * @param obj 缓存对象
     * @param survivalTime 存活时间 小于0表示永不过期
     * @return 缓存项
     */
    private CacheEntry createEntry(Object obj, long survivalTime){
        if (obj == null){
            return null;
        }

        CacheEntry entry = new CacheEntry();
        entry.setValue(obj);
        entry.setCreateTime(System.currentTimeMillis());
        entry.setExpirationTime(System.currentTimeMillis() + survivalTime);
        return entry;
    }

    /**
     * 对缓存对象进行深复制克隆处理
     * @param entry 缓存对象
     * @return 深复制对象
     */
    private CacheEntry clone(CacheEntry entry){
        CacheEntry c_entry = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();//缓存对象流
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(entry);
            oos.flush();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            c_entry = (CacheEntry) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return c_entry;
    }

    /**
     * 将对象存入缓存
     * @param key 键
     * @param obj 缓存对象
     */
    public void set(String key, Object obj){
        set(key,obj,SURVIVAL_TIME);
    }

    /**
     * @param key 键
     * @param obj 缓存对象
     * @param survivalTime 自设定对象缓存时间
     */
    public void set(String key, Object obj, long survivalTime){
        writeLock.lock();
        try {
            if (entryUseRecord.size() >= MAX_CACHE_SIZE){
                deleteTailRecord();
            }
                CacheEntry entry = createEntry(obj,survivalTime);
                cacheMap.put(key,clone(entry));
                entryUseRecord.add(key);
        }finally {
            writeLock.unlock();
        }
    }

    /**
     * 从缓存中获取缓存对象
     * @param key 键
     * @return 缓存对象
     */
    public Object get(String key){
        Object obj = null;
        if (key != null){
            readLock.lock();
            try {
                obj = clone(cacheMap.get(key)).getValue();
            }finally {
                readLock.unlock();
            }
        }

        updateUseRecord(key);

        return obj;
    }

    /**
     * 从缓存中移除缓存对象
     * @param key 键
     * @return 被移除对象
     */
    public Object remove(String key){
        if (key == null){
            return null;
        }
        writeLock.lock();
        try {
            return cacheMap.remove(key).getValue();
        }finally {
            writeLock.unlock();
        }
    }

    /***
     *
     */
    public boolean contains(String key){
        boolean flag = false;
        readLock.lock();
        try {
            flag = cacheMap.containsKey(key);
        }finally {
            readLock.unlock();
        }
        return flag;
    }

    /**
     * 删除记录队列中的头部元素
     */
    private void deleteTailRecord(){
        writeLock.lock();
        try {
            cacheMap.remove(entryUseRecord.remove());
        }finally {
            writeLock.unlock();
        }
    }

    /**
     * 更新缓存项记录
     * @param key 键
     */
    private void updateUseRecord(String key){
        writeLock.lock();
        try {
            entryUseRecord.remove(key);
            entryUseRecord.add(key);
        }finally {
            writeLock.unlock();
        }
    }

    /**
     * 在本地文件中存储缓存数据
     * @param fileName 文件名
     */
    public void save(String fileName){

        try {
            readLock.lock();
            ObjectPersistenceUtils.store(fileName,cacheMap);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            readLock.unlock();
        }
    }

    /**
     * 通过键获取本地文件中存储的对象
     * @param fileName 本地文件名
     * @return 本地文件中的对象
     */
    public synchronized HashMap load(String fileName){
        HashMap<String,Object> map = null;
        try {
            map = (HashMap<String,Object>) ObjectPersistenceUtils.load(fileName);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }
}
