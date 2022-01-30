package com.biot.exercise.entitycache.cache;

import com.biot.exercise.entitycache.Entity;

public interface Cache<E extends Entity> {
    public void add(E e);
    public E get(int id);
    public void update(E e);
    public void remove(E e);
}
