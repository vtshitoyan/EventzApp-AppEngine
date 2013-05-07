package com.eventzapp;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.googlecode.batchfb.FacebookBatcher;
import com.googlecode.batchfb.Later;

public class EventUtils {
	private static final String EVENT_FIELDS_TO_GET = "eid,attending_count,unsure_count,not_replied_count,description,location,name,venue,start_time,timezone";
	private static final char LIST_SEPARATOR = ',';
	
	public static void getAllEventsFromFb(User user) {
		// TODO these lists should be properly converted to strings
		String friendIds_toUse;
		String likeIds_toUse;
		String friendListIds_toUse;
		Date since;
		Date until;
		if (user.getEventFatchParamsId() != 0 && user.getEventFatchParamsId() != null) {
			EventFetchParams eventFetchParams = new EventFetchParamsEndpoint().getEventFetchParams(user.getEventFatchParamsId());
			friendIds_toUse = StringUtils.join(eventFetchParams.getFriendids_touse(), LIST_SEPARATOR);
			likeIds_toUse = StringUtils.join(eventFetchParams.getLikeids_touse(), LIST_SEPARATOR);
			friendListIds_toUse = StringUtils.join(eventFetchParams.getFriendlistids_touse(), LIST_SEPARATOR);
			// TODO take care of the eventfetchparams with relative since/until somehow..
			since = eventFetchParams.getSince();
			until = eventFetchParams.getUntil();
		} else {
			friendIds_toUse =  StringUtils.join(user.getFriendIds(), LIST_SEPARATOR);
			likeIds_toUse =  StringUtils.join(user.getLikeIds(), LIST_SEPARATOR);
			// TODO changes this to friend_list_ids after adding this field to the user class
			friendListIds_toUse =  StringUtils.join(user.getFriendIds(), LIST_SEPARATOR);
			since = new Date();
			// TODO Do this nicer..
			until = new Date(System.currentTimeMillis() + (24 * 60 * 60 * 7 * 1000));		
		}
		
		FacebookBatcher batcher = new FacebookBatcher(user.getAccesToken());
		batcher.setMaxBatchSize(5);
		batcher.setTimeout(59000);
		// TODO add the part with the friend lists to the query
		// TODO handle the part with the limit.. should somehow take care of request times longer then 60 sec
		// maybe divide them into 100 by 100 parts..
		// TODO start_time filter doesn't work properly, figure this out..
		Later<List<Event>> userEvents = batcher.query(
			    "SELECT " + EVENT_FIELDS_TO_GET +
			    " FROM event WHERE eid IN(SELECT eid FROM event_member WHERE uid IN(SELECT uid2 FROM friend WHERE uid1=" + 
			    		user.getUid()+" AND uid2 IN(" + friendIds_toUse + ")) or uid=" + user.getUid()+")" + 
			    		"OR creator IN(" + likeIds_toUse + ")" + " and start_time>" + since.getTime() + 
			    		" AND start_time<" + until.getTime()/1000 + " limit 100", Event.class);
//		Later<List<Event>> userEvents = batcher.query("SELECT eid,attending_count,unsure_count,not_replied_count,description,location,name,venue,start_time,timezone" + 
//													  " FROM event where eid IN(SELECT eid from event_member WHERE uid=" + user.getUid()+")", Event.class);
		
		for (Event event : userEvents.get()) {
			// adding this users id to the event so that he/she has access to it later
			event.setModified(new Date());
			//	TODO handle insertion which is supposed to add the uid to the already existing list
			event.insertOrUpdateEvent(user.getUid());
			FindEventMatches(user, event);
		}
		return;
	}
	
	public static void FindEventMatches(User user, Event event) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		EntityManager mgr = EMF.get().createEntityManager();
		
		Key userKey = KeyFactory.createKey(User.class.getSimpleName(), user.getUid());
		
		Query matchTypeQuery = new Query(MatchType.class.getSimpleName()).setAncestor(userKey);
		List<Entity> matchTypes = datastore.prepare(matchTypeQuery)
                .asList(FetchOptions.Builder.withDefaults());
		for (Entity matchType : matchTypes) {
			calculateMatch(event, mgr.find(MatchType.class, matchType.getKey()));
		}
		
		// TODO
		// get all the matchtypes that belong to the given user in the db
		// for each matchtype do tha matching based on matchmethod and eventmatch
		// insert the matches into the db
	}
	
	private static void calculateMatch (Event event, MatchType matchType) {
		Long matchTypeId = matchType.getId();
		Match match = new Match();
		match.setMatchTypeId(matchTypeId);
		match.setEid(event.getEid());
		switch (matchType.getMatchMethod()) {
			case "friends":
				// TODO match according to the number of friends attending the event
				match.setMatch(new Float(0));
			case "newpeople":
				// TODO match according to the number of
				match.setMatch(new Float(0));
			case "flirty":
				// TODO match according to the percentage of male/female depending on the sex of the user
				match.setMatch(new Float(0));
			case "location":
				// TODO match using the locations of the event and the user
				match.setMatch(new Float(0));
			case "time":
				// TODO match using the time of the event by comparing with the local android calendar of the user
				match.setMatch(new Float(0));
			case "interests":
				// TODO match using the interests of the user, comparing with the description of the events
				// TODO MORE ADVANCED: comparing with the interests of the other attending users
			case "history":
				// TODO match using the history of previous events;
				match.setMatch(new Float(0));
			case "keywordmap":
			case "default":
				// TODO match using a keywordmap, this is also the matching mechanism that can be created by a user
				match.setMatch(new Float(0));			
		}
		// TODO set the user as a parent for the match
	}
}
