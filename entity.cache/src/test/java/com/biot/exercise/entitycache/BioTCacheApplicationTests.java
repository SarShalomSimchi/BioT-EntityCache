package com.biot.exercise.entitycache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.stream.Stream;

import javax.persistence.FetchType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.biot.exercise.entitycache.cache.EntityCache;
import com.biot.exercise.entitycache.cache.EntityCache.Action;
import com.biot.exercise.entitycache.cache.EntityCache.Event;
import com.biot.exercise.entitycache.cache.EntityDoseNotExistException;
import com.biot.exercise.entitycache.person.Person;
import com.biot.exercise.entitycache.person.PersonService;

import rx.Subscription;
import rx.observers.AssertableSubscriber;
import rx.subjects.PublishSubject;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class BioTCacheApplicationTests {
	
	@Autowired
	private PersonService personService;
	
	
	private Person p1, p2, p3;
	
	private void createDataInDB() {
		p1 = new Person("Person_1");
		p2 = new Person("Person_2");
		p3 = new Person("Person_3");
		
		List<Person> persons = List.of(p1, p2, p3);
		persons.forEach(personService::add);
	}
	
	@Test
    public void eagerLoadingIsCashAsDefault() {
		createDataInDB();
		
		EntityCache<Person> cache = createCache(personService);
		
		assertEquals(p1, cache.get(p1.getId()));
		assertEquals(p2, cache.get(p2.getId()));
		assertEquals(p3, cache.get(p3.getId()));
	}
	
	@Test
    public void whenGettingNonExistEntityOnEagerLoading_throwsException() {
		createDataInDB();
		
		EntityCache<Person> cache = createCache(personService);
		
		EntityDoseNotExistException thrown = assertThrows(EntityDoseNotExistException.class, () -> {
			cache.get(4);
		});
		
		assertEquals("Entity with id: 4 doesn't exist in cache", thrown.getMessage());
	}
	
	@Test
    public void whenGettingNonExistEntityOnLazyLoading_noException() {
		createDataInDB();
		EntityCache<Person> cache = createCache(personService, FetchType.LAZY);
		
		Person p1 = personService.add(new Person("Person_4"));
		
		asserEqualsOnCacheAndDB(cache, p1);
	}

	private void asserEqualsOnCacheAndDB(EntityCache<Person> cache, Person person) {
		Person p = cache.get(person.getId());
		assertEquals(person, p);
		assertEqualsOnDB(person);
	}
	
	private void assertEqualsOnDB(Person person) {
		Person foundPerson = personService.get(person.getId());
		assertEquals(person, foundPerson);
	}
	
	@Test
	public void addEntityToCache() {
		EntityCache<Person> cache = createCache(personService);
		Person p1 = new Person("Person_1");
		cache.add(p1);
		
		asserEqualsOnCacheAndDB(cache, p1);
	}

	@Test
	public void updateEntityOnCacheAndDB() {
		EntityCache<Person> cache = createCache(personService);
		Person p1 = new Person("Person_1");
		cache.add(p1);
		
		p1.setName("Person_2");
		cache.update(p1);
		
		asserEqualsOnCacheAndDB(cache, p1);
	}
	
	@Test
	public void removeEntityFromCacheAndDB() {
		EntityCache<Person> cache = createCache(personService, FetchType.LAZY);
		Person p1 = new Person("Person_1");
		cache.add(p1);
		
		cache.remove(p1);
		Person p2 = cache.get(p1.getId());
		assertNull(p2);
		
		p2 = personService.get(p1.getId());
		assertNull(p2);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private EntityCache<Person> createCache(BaseService service) {
		 return new EntityCache<Person>(service);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private EntityCache<Person> createCache(BaseService service, FetchType fetchType) {
		 return new EntityCache<Person>(service, fetchType);
	}
	

    @Test
    public void saveAPersonInDB() {
        Person person = personService.add(new Person("Person_1"));
        Person foundPerson = personService.get(person.getId());
 
        assertEquals(person, foundPerson);
    }
    
    
    @Test
    public void updateAPersonInDB() {
        Person person = personService.add(new Person("Person_1"));
        
        person.setName("Person_2");
        personService.update(person);
        Person foundPerson = personService.get(person.getId());
       
        assertEquals(person, foundPerson);
    }
    
    
    @Test
    public void removeAPersonFromDB() {
        Person person = personService.add(new Person("Person_1"));
        
        personService.remove(person);
        Person foundPerson = personService.get(person.getId());
 
        assertNull(foundPerson);
    }
	
	
	@Test
	public void testMultiSuscribesForCachEvents() {
		EntityCache<Person> cache = createCache(personService);
		
		PublishSubject<Event<Person>> observer_1 = PublishSubject.create();
        Subscription subscription_1 = cache.suscribe(observer_1);
        AssertableSubscriber<Event<Person>> assertableSubscriber_1 = observer_1.test();
        
        
        PublishSubject<Event<Person>> observer_2 = PublishSubject.create();
        Subscription subscription_2 = cache.suscribe(observer_2);
        AssertableSubscriber<Event<Person>> assertableSubscriber_2 = observer_2.test();
        
        
        Person p1 = new Person("Person_1");
        Person p2 = new Person("Person_2");
        
        cache.add(p1);
        cache.add(p2);
        
        assertableSubscriber_1.assertValueCount(2);
        assertableSubscriber_2.assertValueCount(2);
        
        List<Event<Person>> events_1 = assertableSubscriber_1.getOnNextEvents();
        List<Event<Person>> events_2 = assertableSubscriber_2.getOnNextEvents();
        List<Person> persons = List.of(p1, p2);
        
        Stream.
		iterate(0, i -> i < events_1.size(), i -> i + 1)
		.forEach(i -> {
			 Person person = persons.get(i);
			 asserEvent(events_1.get(i), person, Action.ADD);
			 asserEvent(events_2.get(i), person, Action.ADD);
		  });
        
        
        cache.remove(p2);
        assertableSubscriber_1.assertValueCount(3);
        assertableSubscriber_2.assertValueCount(3);
        asserEvent(events_1.get(2), p2, Action.REMOVE);
        asserEvent(events_2.get(2), p2, Action.REMOVE);
        
       
        p1.setName("Person_2");
        cache.update(p1);
        assertableSubscriber_1.assertValueCount(4);
        assertableSubscriber_2.assertValueCount(4);
        asserEvent(events_1.get(3), p1, Action.UPDATE);
        asserEvent(events_2.get(3), p1, Action.UPDATE);
        
        
        cache.unsuscribe(subscription_1);
        cache.unsuscribe(subscription_2);
	}
	
	private void asserEvent(Event<Person> event, Entity entity, Action action) {
		assertEquals(entity, event.entity);
		assertEquals(action, event.action);
		assertEquals("Event [entity=" + entity + ", action=" + action + "]", event.toString());
	}
}
