package com.example.elasticsearch.controller;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.example.elasticsearch.model.User;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


/**
 * This class is to demo how ElasticsearchTemplate can be used to Save/Retrieve
 */

@RestController
@RequestMapping("/rest/users")
public class UserController {

    @Autowired
    Client client;
    @PostMapping("/create")
    public String create(@RequestBody User user) throws IOException {
        
        IndexResponse response = client.prepareIndex("users", "employee", user.getUserId())
                .setSource(jsonBuilder()
                        .startObject()
                        .field("name", user.getName())
                        .field("location",user.getLocation())
                        .field("text",user.getText())
                        .field("userSettings", user.getUserSettings())
                        //.field("hastag", "#verizon")
                        .endObject()
                )
                .get();
              // System.out.println("response id:"+response.getId());
        return response.getResult().toString();
    }



    
    
    @GetMapping("/save")
    public  String getTwitterinstance() throws TwitterException , IOException {
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey("7MTPzMreeBh1eqOLB8bUnE1gc")
		.setOAuthConsumerSecret("Vy88uZQWJloGVBXn3jjDbVl6SbM1izfiw9aFJKTt3IAuYuDv9n")
		.setOAuthAccessToken("988006988268257280-8eLdCBVdAAGiANdCiN3Pv0rflOieXSd") 
		.setOAuthAccessTokenSecret("46orXbbIcitplXfPwBYdkR8ipRNAjr1phIbxGySEFpzvp");
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		
		Query query = new Query("#verizon");
		query.setCount(10);
		QueryResult result = twitter.search(query);
		for(Status status :result.getTweets())
		{
			
			//System.out.println("Name_of_user:"+status.getUser().getScreenName()+":"+"Location--:"+
		            //status.getUser().getLocation()+"Text_of_user:"+status.getText());
			
			 IndexResponse response = client.prepareIndex("users", "employee", Integer.toString((int) status.getId()))
		                .setSource(jsonBuilder()
		                        .startObject()
		                        .field("hashtag", "#verizon")
		                        .field("date",status.getCreatedAt() )
		                        .field("name",status.getUser().getScreenName() )
		                       
		                        .field("location",status.getUser().getLocation() )
		                        .field("text",status.getText() )
		                       
		                     
		                 
		                        
		                         .endObject()
		                )
		                .get();
		             //  System.out.println("response id:"+response.getId());
		       
					
					
		}
		return "";
	}
    
    
    
    @GetMapping("/view/{id}")
    public Map<String, Object> view(@PathVariable final String id) {
        GetResponse getResponse = client.prepareGet("users", "employee", id).get();
        System.out.println(getResponse.getSource());


        return getResponse.getSource();
    }
    @GetMapping("/view/name/{field}")
    public Map<String, Object> searchByName(@PathVariable final String field) {
        Map<String,Object> map = null;
        SearchResponse response = client.prepareSearch("users")
                                .setTypes("employee")
                                .setSearchType(SearchType.QUERY_AND_FETCH)
                                .setQuery(QueryBuilders.matchQuery("name", field))
                                .get()
                                ;
        List<SearchHit> searchHits = Arrays.asList(response.getHits().getHits());
        map =   searchHits.get(0).getSource();
        return map;

    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable final String id) throws IOException {

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("users")
                .type("employee")
                .id(id)
                .doc(jsonBuilder()
                        .startObject()
                        .field("name", "Rajesh")
                        .endObject());
        try {
            UpdateResponse updateResponse = client.update(updateRequest).get();
            System.out.println(updateResponse.status());
            return updateResponse.status().toString();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e);
        }
        return "Exception";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable final String id) {

        DeleteResponse deleteResponse = client.prepareDelete("users", "employee", id).get();

        System.out.println(deleteResponse.getResult().toString());
        return deleteResponse.getResult().toString();
    }
}