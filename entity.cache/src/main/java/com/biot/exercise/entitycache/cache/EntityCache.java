package com.biot.exercise.entitycache.cache;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.persistence.FetchType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.biot.exercise.entitycache.BaseService;
import com.biot.exercise.entitycache.Entity;

import rx.Subscription;
import rx.subjects.PublishSubject;


public class EntityCache<E extends Entity> implements Cache<E> {
    private static final Log log = LogFactory.getLog(EntityCache.class);

    private final BaseService<E> repository;
    private final ConcurrentHashMap<Integer, E> cache;
    private final FetchType loadingMode;
    private final Object lock = new Object();

    public enum Action {ADD, UPDATE, REMOVE}
    public final PublishSubject<Event<E>> subject = PublishSubject.create();

    public static class Event<E extends Entity> {
        public E entity;
        public Action action;

        public Event(E entity, Action action) {
            this.entity = entity;
            this.action = action;
        }

		@Override
		public String toString() {
			return "Event [entity=" + entity + ", action=" + action + "]";
		}
    }

    public EntityCache(BaseService<E> repository){
        this(repository, FetchType.EAGER);
    }

    public EntityCache(BaseService<E> repository, FetchType mode) {
        this.repository = repository;
        this.cache = new ConcurrentHashMap<Integer, E>();
        this.loadingMode = mode;

        if(loadingMode == FetchType.EAGER)
            loadData();
    }

    private void loadData() {
        List<E> data = repository.getAll();

        if(data != null)
            data.forEach(this::add);
    }

    @Override
    public void add(E e) {
        boolean success = executeOnRepo(repository::add, e);

        if (success) {
        	 synchronized (lock) {
        		 cache.put(e.getId(), e);
        		 publish(e, Action.ADD);
        	}
        }
    }

	private boolean executeOnRepo(Consumer<E> c, E e) {
        try {
            c.accept(e);
            return true;
        } catch (Exception ex){
            log.error(ex.getMessage());
            return true;
        }
    }
	
	private void publish(E e, Action action) {
	  	 subject.onNext(new Event<E>(e,action));
	}

	@Override
    public E get(int id) {
        
        E e = cache.get(id);

        if (e == null) {
            if (loadingMode == FetchType.EAGER)
                throw new EntityDoseNotExistException(id);

            e = getFromRepo(id);

            if (e != null)
            	synchronized (lock) {
            		add(e);
            	}
        }

        return e;
    }

    private E getFromRepo(int id) {
        try {
            return repository.get(id);
        } catch (Exception ex){
            log.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public void update(E e) {
        boolean success = executeOnRepo(repository::update, e);

        if (success) {
        	synchronized (lock) {
        		cache.put(e.getId(), e);
        		publish(e, Action.UPDATE);
        	}
        }
    }

    @Override
    public void remove(E e) {
        boolean success = executeOnRepo(repository::remove, e);

        if (success) {
        	synchronized (lock) {
        		cache.remove(e.getId());
        		publish(e, Action.REMOVE);
        	}
        }
    }


    public Subscription suscribe(PublishSubject<Event<E>>observer) {
        return subject.subscribe(observer);
    }

    public void unsuscribe(Subscription subscription) {
        subscription.unsubscribe();
    }

}

