package com.biot.exercise.entitycache.cache;

public class EntityDoseNotExistException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public EntityDoseNotExistException(int id){
        super("Entity with id: " + id + " doesn't exist in cache");
    }
}
