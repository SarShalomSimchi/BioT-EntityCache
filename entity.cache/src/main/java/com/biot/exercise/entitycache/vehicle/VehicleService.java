package com.biot.exercise.entitycache.vehicle;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.biot.exercise.entitycache.BaseService;

@Service
public class VehicleService implements BaseService<Vehicle> {

    private VehicleRepository repository;

    @Autowired
    public VehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Vehicle add(Vehicle vehicle) {
        return repository.save(vehicle);
    }

    @Override
    public Vehicle get(int id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Vehicle update(Vehicle vehicle) {
        return repository.save(vehicle);
    }

    @Override
    public void remove(Vehicle vehicle) {
        repository.delete(vehicle);
    }

    @Override
    public List<Vehicle> getAll() {
        return repository.findAll();
    }
}
