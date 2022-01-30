package com.biot.exercise.entitycache.person;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.biot.exercise.entitycache.BaseService;

@Service
public class PersonService implements BaseService<Person> {

   private final PersonRepository repository;

    @Autowired
    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public Person add(Person person) {
        return repository.save(person);
    }

    @Override
    public Person get(int id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Person update(Person person) {
        return repository.save(person);
    }

    @Override
    public void remove(Person person) {
        repository.delete(person);
    }
    
    @Override
    public List<Person> getAll() {
        return repository.findAll();
    }
}