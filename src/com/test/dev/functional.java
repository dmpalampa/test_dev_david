package com.test.dev;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.google.gson.Gson;

@Path("/dev_test")
public class functional {

	/*
	 * 1. Count the number of unique recipe names.
	 */
	@GET
	@Path("/unique_recipe_count")
	@Produces(MediaType.APPLICATION_JSON)
	public Response countNumberOfUniqueRecipes() {

		JSONParser parser = new JSONParser();
		JSONArray recipes = recipeStat();
		ArrayList<String> listOfRecipes = new ArrayList<>();
		ArrayList<Integer> recipesPostCodeList = new ArrayList<>();
		try {
			for (Object recipeObject : recipes) {
				JSONObject myJSONObject = new JSONObject();
				// If you want to get JSONObject
				myJSONObject = (JSONObject) recipeObject;
				if (recipeObject instanceof JSONObject) {
					myJSONObject = (JSONObject) parser.parse((recipeObject.toString()));
					String recipeName = (String) myJSONObject.get("recipe");
					Integer postCode = Integer.parseInt((String) myJSONObject.get("postcode"));
					listOfRecipes.add(recipeName);
					recipesPostCodeList.add(postCode);
				}
			}
			HashSet<String> hashSet = new HashSet<>(listOfRecipes);
			JSONObject object = new JSONObject();
			object.put("unique_recipe_count", hashSet.size());
			return Response.status(200).entity(object).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/*
	 * 2. Count the number of occurences for each unique recipe name (alphabetically ordered by recipe name).
	 */
	@GET
	@Path("/count_per_recipe")
	@Produces(MediaType.APPLICATION_JSON)
	public Response countPerRecipe() {

		JSONObject obj = null;
		JSONArray objArray = new JSONArray();
		JSONParser parser = new JSONParser();
		JSONArray recipes = recipeStat();
		ArrayList<String> listOfRecipes = new ArrayList<>();
		try {
			for (Object recipeObject : recipes) {
				JSONObject myJSONObject = new JSONObject();
				// If you want to get JSONObject
				myJSONObject = (JSONObject) recipeObject;
				if (recipeObject instanceof JSONObject) {
					myJSONObject = (JSONObject) parser.parse((recipeObject.toString()));
					String recipeName = (String) myJSONObject.get("recipe");
					listOfRecipes.add(recipeName);
				}
			}

			for (int i = 0; i < listOfRecipes.size(); i++) {
				obj = new JSONObject();
				int count = Collections.frequency(listOfRecipes, listOfRecipes.get(i));
				obj.put("recipe", listOfRecipes.get(i));
				obj.put("count", count);
				objArray.add(obj);
			}
			JSONObject o = new JSONObject();
			o.put("count_per_recipe", objArray);
			return Response.status(200).entity(o).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	
	/*
	 * 3. Find the postcode with most delivered recipes.
	 */
	@GET
	@Path("/busiest_postcode")
	@Produces(MediaType.APPLICATION_JSON)
	public Response mostFrequent() {

		JSONParser parser = new JSONParser();//creating instance os json parser
		JSONArray recipes = recipeStat();
		ArrayList<String> listOfRecipes = new ArrayList<>();
		ArrayList<Integer> recipesPostCodeList = new ArrayList<>();
		try {

			for (Object recipeObject : recipes) {
				JSONObject myJSONObject = new JSONObject();
				// If you want to get JSONObject
				myJSONObject = (JSONObject) recipeObject;
				if (recipeObject instanceof JSONObject) {
					myJSONObject = (JSONObject) parser.parse((recipeObject.toString()));
					Integer postCode = Integer.parseInt((String) myJSONObject.get("postcode"));
					recipesPostCodeList.add(postCode);
				}
			}
			System.out.println(listOfRecipes.size());
			if (recipesPostCodeList.size() > 0) {

				Map<Integer, Integer> counterMap = new HashMap<Integer, Integer>();
				Integer maxValue = 0;
				Integer mostFrequentValue = null;

				for (Integer valueAsKey : recipesPostCodeList) {
					Integer counter = counterMap.get(valueAsKey);
					counterMap.put(valueAsKey, counter == null ? 1 : counter + 1);
					counter = counterMap.get(valueAsKey);
					if (counter > maxValue) {
						maxValue = counter;
						mostFrequentValue = valueAsKey;
					}
				}

				JSONObject object = new JSONObject();
				object.put("postcode", mostFrequentValue);
				object.put("delivery_count", maxValue);
				return Response.status(200).entity(object).build();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 *4.  List the recipe names (alphabetically ordered) that contain in their name one of the following words:
		Potato
		Veggie
		Mushroom
	 */
	@POST
	@Path("/match_by_name")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sortRecipeNamesAlphabetically(InputStream incomingData) {

		String postData = readPostData(incomingData);
		JSONParser parser = new JSONParser();
		JSONArray recipes = recipeStat();

		ArrayList<String> listOfRecipes = new ArrayList<>();
		ArrayList<String> searchResultsList = new ArrayList<>();
		try {
			
			JSONObject dataObject = (JSONObject) parser.parse(postData);
			
			for (Object recipeObject : recipes) {
				JSONObject myJSONObject = new JSONObject();
				// If you want to get JSONObject
				myJSONObject = (JSONObject) recipeObject;
				if (recipeObject instanceof JSONObject) {
					 myJSONObject = (JSONObject) parser.parse((recipeObject.toString()));
					String recipeName = (String) myJSONObject.get("recipe");
					listOfRecipes.add(recipeName);
				}
			}
			
			String searchValue = (String) dataObject.get("searchValue");
			for (String recipe : listOfRecipes) {
				if (recipe.contains(searchValue)) {
					System.out.println(recipe);
					searchResultsList.add(recipe);
				}
			}

			//getting list of such results without duplicates
			List<String> UniqueRecipes = searchResultsList.stream().distinct().collect(Collectors.toList());

			String[] searchResultsListArray = UniqueRecipes.toArray(new String[UniqueRecipes.size()]);
			String temp;
			for (int i = 0; i < searchResultsListArray.length; i++) {
				for (int j = i + 1; j < searchResultsListArray.length; j++) {
					// to compare one string with other strings
					if (searchResultsListArray[i].compareTo(searchResultsListArray[j]) > 0) {
						// swapping
						temp = searchResultsListArray[i];
						searchResultsListArray[i] = searchResultsListArray[j];
						searchResultsListArray[j] = temp;
					}
				}
			}
			JSONObject object = new JSONObject();
			object.put("match_by_name", searchResultsListArray);
			return Response.status(200).entity(object).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*Method to access and read data from data.json file*/
	protected JSONArray recipeStat() {

		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(new FileReader("./test_dev_david_/src/com/test/dev/data.json"));

			JSONArray recipes = (JSONArray) obj;
			return recipes;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	protected String readPostData(InputStream incomingData) {
		StringBuilder crunchifyBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			String line = null;
			while ((line = in.readLine()) != null) {
				crunchifyBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		return crunchifyBuilder.toString();
	}

}
