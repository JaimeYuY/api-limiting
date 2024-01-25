package com.ocean.angel.tool.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RateLimiterKeyUtil {

    private RateLimiterKeyUtil() {};

    // 内存存储RateLimiter Key
    public static List<String> list = Collections.synchronizedList(new ArrayList<>());

    /**
     *  是否包含
     */
    public static boolean contains(String data) {

        // 不包含返回false,并加入数据到list中
        if(!list.contains(data)) {
            list.add(data);
            return false;
        }

        // 包含返回true
        return true;
    }
}
