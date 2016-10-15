package com.appdirect.controllers;

import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.appdirect.domain.ErrorCode;
import com.appdirect.persistent.entities.User;
import com.appdirect.rest.presentation.EventResponse;
import com.appdirect.rest.presentation.SubscriptionCancelEvent;
import com.appdirect.rest.presentation.SubscriptionOrderEvent;
import com.appdirect.service.EventService;
import com.appdirect.service.SubscriptionService;
import com.appdirect.service.UserService;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {
	
	@Autowired
	EventService eventService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	SubscriptionService subscriptionService;

	@RequestMapping(value="/create",method=RequestMethod.GET)
	public @ResponseBody EventResponse createSubscription(@RequestParam("eventUrl") String eventUrl){
		EventResponse eventResponse = null;
		try{
			URL url = new URL(eventUrl);
			RestTemplate restTemplate = new RestTemplate();
			SubscriptionOrderEvent event = restTemplate.getForObject(url.toURI(), SubscriptionOrderEvent.class);
			if(userService.findByOpenId(event.getCreator().getOpenId())==null){
				User user = eventService.saveUser(event);
				subscriptionService.createSubscription(user);
				eventResponse = new EventResponse(Boolean.TRUE.toString(),user.getUserId(),null,"Subscription Order is Successful");
			}else{
				eventResponse = new EventResponse(Boolean.FALSE.toString(),null,ErrorCode.USER_ALREADY_EXISTS.toString(),"Subscription Order failed");
			}
		}catch(Exception e){
			e.printStackTrace();
			eventResponse = new EventResponse(Boolean.FALSE.toString(),null,null,e.getMessage());
		}
		return eventResponse;
	}
	
		
	@RequestMapping(value="/cancel",method=RequestMethod.GET)
	public @ResponseBody EventResponse cancelSubscription(@RequestParam("eventUrl") String eventUrl){
		EventResponse eventResponse = null;
		try{
			URL url = new URL(eventUrl);
			RestTemplate restTemplate = new RestTemplate();
			SubscriptionCancelEvent event = restTemplate.getForObject(url.toURI(), SubscriptionCancelEvent.class);
			User user = userService.findById(event.getPayload().getAccount().getAccountIdentifier());
			if(user!=null){
				subscriptionService.cancelSubscription(user);
				eventResponse = new EventResponse(Boolean.TRUE.toString(),user.getUserId(),null,"Subscription Cancellation is Successful");
			}else{
				eventResponse = new EventResponse(Boolean.FALSE.toString(),null,ErrorCode.USER_NOT_FOUND.toString(),"Subscription Cancellation failed");
			}
		}catch(Exception e){
			e.printStackTrace();
			eventResponse = new EventResponse(Boolean.FALSE.toString(),null,null,e.getMessage());
		}
		return eventResponse;
	}
	
	

}
