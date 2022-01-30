package com.biot.exercise.entitycache.vehicle;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.biot.exercise.entitycache.Entity;

import lombok.Data;

@Data
@javax.persistence.Entity
@Table(name = "vehicle")
public class Vehicle implements Entity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    public Vehicle() {
	}
    

    public Vehicle(String name) {
		super();
		this.name = name;
	}

    @Override
    public Integer getId() {
        return id;
    }
}
