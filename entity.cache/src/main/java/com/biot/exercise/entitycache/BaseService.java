package com.biot.exercise.entitycache;

import java.util.List;

public interface BaseService<E extends Entity> {
    public E add(E e);
    public E get(int id);
    public E update(E e);
    public void remove(E e);
    public List<E> getAll();
}
