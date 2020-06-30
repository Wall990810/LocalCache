import java.io.Serializable;

/**
 * 缓存项
 * @Author: Wall
 * @version 1.0 2020/06/30
 */

public class CacheEntry implements Serializable {

    /**存储值*/
    private Object value;

    /**过期时间*/
    private long expirationTime;

    /**创建时间*/
    private long createTime;

    public CacheEntry(){
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public Object getValue() {
        return value;
    }

    public long getCreateTime() {
        return createTime;
    }
}
