package com.eventzapp;

import com.eventzapp.EMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JPACursorHelper;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Api(name = "eventmemberdataendpoint", namespace = @ApiNamespace(ownerDomain = "eventzapp.com", ownerName = "eventzapp.com", packagePath = ""))
public class EventMemberDataEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listEventMemberData")
	public CollectionResponse<EventMemberData> listEventMemberData(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		EntityManager mgr = null;
		Cursor cursor = null;
		List<EventMemberData> execute = null;

		try {
			mgr = getEntityManager();
			Query query = mgr
					.createQuery("select from EventMemberData as EventMemberData");
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<EventMemberData>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (EventMemberData obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<EventMemberData> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getEventMemberData")
	public EventMemberData getEventMemberData(@Named("id") String id) {
		EntityManager mgr = getEntityManager();
		EventMemberData eventmemberdata = null;
		try {
			eventmemberdata = mgr.find(EventMemberData.class, id);
		} finally {
			mgr.close();
		}
		return eventmemberdata;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param eventmemberdata the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertEventMemberData")
	public EventMemberData insertEventMemberData(EventMemberData eventmemberdata) {
		EntityManager mgr = getEntityManager();
		try {
			if (containsEventMemberData(eventmemberdata)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.persist(eventmemberdata);
		} finally {
			mgr.close();
		}
		return eventmemberdata;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param eventmemberdata the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateEventMemberData")
	public EventMemberData updateEventMemberData(EventMemberData eventmemberdata) {
		EntityManager mgr = getEntityManager();
		try {
			if (!containsEventMemberData(eventmemberdata)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(eventmemberdata);
		} finally {
			mgr.close();
		}
		return eventmemberdata;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 * @return The deleted entity.
	 */
	@ApiMethod(name = "removeEventMemberData")
	public EventMemberData removeEventMemberData(@Named("id") String id) {
		EntityManager mgr = getEntityManager();
		EventMemberData eventmemberdata = null;
		try {
			eventmemberdata = mgr.find(EventMemberData.class, id);
			mgr.remove(eventmemberdata);
		} finally {
			mgr.close();
		}
		return eventmemberdata;
	}

	private boolean containsEventMemberData(EventMemberData eventmemberdata) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			EventMemberData item = mgr.find(EventMemberData.class,
					eventmemberdata.getId());
			if (item == null) {
				contains = false;
			}
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static EntityManager getEntityManager() {
		return EMF.get().createEntityManager();
	}

}
