//Vanya Yorgova, Rachael Youngworth, Zephan Johnson
//CS 425 Final Project

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.*;

public class MovieTheater {
	private static Connection connection = null;
	private static Statement stmt = null;
	private static Scanner input = null;
	private static ArrayList<String> movieList = null;
	private static ArrayList<Integer> showtimeList = null;
	private static boolean loggedIn = false;
	private static int loggedinID = 0;
	
	public static void main(String[] args)
	{
		try 
		{//Testing For Driver
			Class.forName ("oracle.jdbc.driver.OracleDriver");
			System.out.println("Driver registered.");
			
		} 
		catch (ClassNotFoundException e) 
		{
			System.out.println("driver not found.");
			e.printStackTrace();
			return;
		}
		
		while(connection == null){
			try 
			{//Testing Connection
				connection = DriverManager.getConnection("jdbc:oracle:thin:@fourier.cs.iit.edu:1521:orcl", 
														"vyorgova", 
														"zxcs425");
				
				System.out.println("Connection established");
			} 
			catch (SQLException e) 
			{
				System.out.println("Connection failed.");
				e.printStackTrace();
			}
		}
		
		if (connection != null) 
		{//If connection is not null, start to query
					
			boolean menu = true;//variable to control menu loop
			while(menu)
			{
				System.out.println("Welcome to the ReZerVed Theater!\nPlease make a selection\n\n"
						+ "1\t-\tLogin\n"
						+ "2\t-\tCreate Account\n"
						+ "3\t-\tView Movies\n"
						+ "4\t-\tSearch for Movie\n"
						+ "5\t-\tExit\n");
				input = new Scanner(System.in);
				int menuSelection = input.nextInt();
				
				switch (menuSelection) 
				{
				case 1: login();
						break;
						
				case 2: createAccount();
					break;
				case 3: 
					viewMovieList();
					break;
					
				case 4: searchForMovie();
					break;
				case 5: menu = false;
					System.out.println("Thank you for visiting the ReZerVed Theater!");
					break;
					
				default: System.out.println("Invalid Input\n\n");	
				}
			}
		}
	}	
	
	

	public static int getMovieList()
	{//this function queries for all movies and sorts them by id.
		//TODO we could potentially return the array that holds all the movies.
		int count = 1;
		try 
		{
			stmt = connection.createStatement();
			
			String query = "select* from Movies";//This is the main query
			ResultSet result = stmt.executeQuery(query);
			
			movieList = new ArrayList<String>();//small array to hold all movies
			
			while (result.next())	
			{//Loops through the query to add all the results to the array
				String title = result.getString("title");
				movieList.add(title);//stores the title in it's corresponding id slot in the array.
				count++;
			}
				
			for (int i = 0; i < movieList.size(); i++) {
				System.out.println(i+1 + "\t-\t"+movieList.get(i));
			}		
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return count;
	}
	


public static void viewMovieList()
	{
		boolean menu = true;
		while(menu)
		{
			int lastNumber = getMovieList();
			System.out.println(lastNumber + "\t-\tReturn to Original Menu\n");
			int menuselect = input.nextInt();
			if(menuselect == lastNumber)
				menu = false;
			else if(menuselect > lastNumber)
				System.out.println("Invalid input\n");
			else
			{
				boolean menu2 = true;
				while(menu2)
				{
					System.out.println("1\t-\tView Showtimes\n"
						+ "2\t-\tView Ratings\n"
						+ "3\t-\tView Movie Information\n"
						+ "4\t-\tPurchase A Ticket\n" 
						+ "5\t-\tReturn to movie listings");
					int menu2select = input.nextInt();
					switch(menu2select)
					{
					
					case 1: getShowtimes(menuselect - 1);
						break;
					case 2: viewRatings(menuselect - 1);
						break;
					case 3: getMovieInfo(menuselect - 1);
						break;
					case 4: viewPurchaseMenu(menuselect - 1);
						break;
					case 5:menu2 = false;
						break;
					default: System.out.println("Invalid input\n");	
					}
				}
			}
		}
	}
	
	public static int getPurchaseTicketMenu(int MovieID)
	{
		int count = 1;
		try 
		{
			stmt = connection.createStatement();
			
			String query = "select movie_ID from Movies where title = '" + movieList.get(MovieID) + "'";//This is the main query
			ResultSet result = stmt.executeQuery(query);
			result.next();
			int movie_ID = result.getInt("movie_ID");
			
			query = "select distinct time, room_ID, price, showing_ID from Showings, tickets where Showings.showing_ID = Tickets.showing_ID and movie_ID = " + Integer.toString(movie_ID);//This is the main query
			ResultSet result2 = stmt.executeQuery(query);	
			
			showtimeList = new ArrayList<Integer>();
			
			while (result2.next())	
			{//Loops through the query to add all the results to the array
				int showing = result2.getInt("showing_ID");
				showtimeList.add(showing);//stores the title in it's corresponding id slot in the array.
				
				System.out.println("\n"+count + ".\t" + movieList.get(MovieID)
						+ "\n\tTime: " + result2.getString("time")
						+ "\n\tTheater: " + result2.getInt("room_ID")
						+ "\n\tPrice: $" + result2.getInt("price")
						+ "\n");
				count++;
			}		
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return count;
	}
	
	public static void viewPurchaseMenu(int MovieID)
	{
		if(loggedinID > 0)
		{
			boolean menu = true;
			while(menu)
			{
				int lastNumber = getPurchaseTicketMenu(MovieID);
				System.out.println(lastNumber + "\tReturn to Original Menu\n");
				System.out.println("Choose a showtime by number or go back a menu: ");
				int menuselect = input.nextInt();
				if(menuselect == lastNumber)
					menu = false;
				else if(menuselect > lastNumber)
					System.out.println("Invalid input\n");
				else
				{
					boolean menu2 = true;
					while(menu2)
					{
						System.out.print("Please type in your payment method(Credit, Debit, Cash, etc.): ");
						String payment = input.next();
						setPaymentMethod(payment);
						showSelectedShowing(menuselect - 1, MovieID);
						System.out.println("Is this the correct showtime and price? y/n");
						String answer = input.next();
						if(answer.equals("y"))
						{
							System.out.println("Buying a ticket");
							setPurchaseTicket(menuselect - 1);
							menu2 = false;
						}
						else
						{
							System.out.println("Returning to movie list");
							menu2 = false;
						}
					}
				}
			}
		}
		else
		{
			System.out.println("\nPlease login as a non-staff member to continue\n");
		}
	}
	
	public static void setPurchaseTicket(int ShowingID)
	{
		try 
		{
			stmt = connection.createStatement();
			
			String query = "select max(tickets_ID) as max from Tickets";//This is the main query
			ResultSet maxResult = stmt.executeQuery(query);
			int id;
			if(maxResult.next())
			{
				id = maxResult.getInt("max") + 1;
				
				String query2 = "select * from Tickets";//This is the main query
				ResultSet price = stmt.executeQuery(query2);
				int ticketPrice = 15;
				if(price.next())
				{
					ticketPrice = price.getInt("price");
				}
				
				String query3 = "INSERT INTO Tickets VALUES (" + id 
						+ ", " + loggedinID + ", " 
						+ showtimeList.get(ShowingID) + ", " 
						+ ticketPrice + ")";//This is the main query
				System.out.println(query3);
				ResultSet addResult = stmt.executeQuery(query3);
				if(addResult.next())
				{
					System.out.println("\nTicket bought!\n");
				}
				else
				{
					System.out.println("Query Failed");
				}
			}
			else
			{
				
			}
			
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		
	
	}
	
	public static void showSelectedShowing(int ShowingID, int MovieID)
	{
		try 
		{
			stmt = connection.createStatement();
			
			String query = "select distinct time, room_ID, price, showing_ID "
					+ "from Showings, Tickets "
					+ "where Showings.showing_ID = Tickets.showing_ID "
					+ "and Showings.showing_ID = " + showtimeList.get(ShowingID);//This is the main query
			System.out.println(query);
			ResultSet result = stmt.executeQuery(query);
			
				while(result.next())
				{
					System.out.println("\n\t" + movieList.get(MovieID)
							+ "\n\tTime: " + result.getString("time")
							+ "\n\tTheater: " + result.getInt("room_ID")
							+ "\n\tPrice: $" + result.getInt("price")
							+ "\n");
				}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void setPaymentMethod(String payment)
	{
		try 
		{
			stmt = connection.createStatement();
			String query = "select * "
					+ "from Payment "
					+ "where user_ID = " + loggedinID
					+ " and method = '" + payment + "'";
			//System.out.println(query);
			ResultSet check = stmt.executeQuery(query);
			if(!check.next())
			{
				
				String query2 = "INSERT INTO Payment VALUES (" + loggedinID + ", '" + payment +"')";//This is the main query
				ResultSet addResult = stmt.executeQuery(query2);
				if(addResult.next())
				{
					System.out.println(payment + " added to user Payment Methods.\n");
				}
				else
				{
					System.out.println("Query Failed");
				}
			}
			else
			{
				//System.out.println("Existing payment method");
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	public static void getShowtimes(int MovieID)
	{
		try 
		{
			stmt = connection.createStatement();
			
			String query = "select movie_ID from Movies where title = '" + movieList.get(MovieID) + "'";//This is the main query
			ResultSet result = stmt.executeQuery(query);
			result.next();
			int movie_ID = result.getInt("movie_ID");
			
			query = "select * from Showings where movie_ID = " + Integer.toString(movie_ID);//This is the main query
			ResultSet result2 = stmt.executeQuery(query);	

				while(result2.next())
				{
					System.out.println("Movie Title: " + movieList.get(MovieID)
						+ "\nTime: " + result2.getString("time")
						+ "\nTickets available: " + result2.getInt("tickets_left")
						+ "\nTheater: " + result2.getInt("room_ID")
						+ "\n");
				}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		System.out.println("Press Enter to go back");
		try{System.in.read();}  
		catch(Exception e){}  
	}


	public static void getMovieInfo(int MovieID)
	{
		try 
		{
			stmt = connection.createStatement();
			
			String query = "select movie_ID from Movies where title = '" + movieList.get(MovieID) + "'";//This is the main query
			ResultSet result = stmt.executeQuery(query);
			result.next();
			int movie_ID = result.getInt("movie_ID");
			query = "select * from Movies where movie_ID = " + Integer.toString(movie_ID);//This is the main query
			ResultSet result2 = stmt.executeQuery(query);
			result2.next();
			System.out.print("Movie Title: " + result2.getString("title")
					+ "\nGenre: " + result2.getString("genre"));
			if(result2.getString("genre2")!=null){
				System.out.print(", " + result2.getString("genre2")+"\n");
			}		
			else{System.out.println("");}
			System.out.println("Year of Release: " + result2.getInt("year")
					+ "\nMovie Length: " + result2.getInt("length") + " min"
					+ "\nRated: " + result2.getString("PG_rating")+ "\n") ;	
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		System.out.println("Press Enter to go back");
		try{System.in.read();}  
		catch(Exception e){}  
	}



	public static void viewRatings(int MovieID)
	{
		getMovieRating(MovieID);
		getActorRatings(MovieID);
	}
	
	public static void getMovieRating(int MovieID)
	{
		try 
		{
			stmt = connection.createStatement();
			
			String query = "select movie_ID, title from Movies where title = '" + movieList.get(MovieID) + "'";//This is the main query
			ResultSet result = stmt.executeQuery(query);
			result.next();
			String title = result.getString("title");
			int movie_ID = result.getInt("movie_ID");
			query = "select * from rating_movies where movie_ID = " + Integer.toString(movie_ID);//This is the main query
			ResultSet result2 = stmt.executeQuery(query);

			System.out.println();
			while (result2.next())	
			{
				double rating = result2.getDouble("rating");
				System.out.println("Rating for "+ title + "  \t" + rating + "/10.0");	
			}	
			System.out.println();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}


public static void getActorRatings(int MovieID)
	{
		try 
		{
			stmt = connection.createStatement();
			
			String query = "select movie_ID, title from Movies where title = '" + movieList.get(MovieID) + "'";//This is the main query
			ResultSet result = stmt.executeQuery(query);
			result.next();
			String title = result.getString("title");
			int movie_ID = result.getInt("movie_ID");
			query = "select distinct Person.name as name, Rating_Person.rating as rating, involved_in.position "
					+ "from involved_in, Person, Rating_Person "
					+ "where involved_in.movie_ID = " + Integer.toString(movie_ID) 
					+ " and Person.person_ID = Rating_Person.person_ID"
					+ " and Person.person_ID = involved_in.person_ID";//This is the main query
			ResultSet result2 = stmt.executeQuery(query);
			System.out.println("Ratings for the Actors/Writers/Directors of"
					+ "\n" + title + "\n");
			while (result2.next())	
			{
				double rating = result2.getDouble("rating");
				String actorName = result2.getString("name");
				String position = result2.getString("position");
				System.out.println("Rating " +  "for " + position +":\t" + actorName + "  \t" + rating + "/10.0");	
			}	
			System.out.println();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		System.out.println("Press Enter to go back");
		try{System.in.read();}  
		catch(Exception e){} 
	}



public static void login (){

		System.out.println("\nEnter your username:");
		String usr = input.next();
		System.out.println("\nEnter your password:");
		String pass = input.next();
		try {
			stmt = connection.createStatement();
			String query = "select* from User_new where name='" + usr + "' and password='" + pass + "'";
			ResultSet result = stmt.executeQuery(query);
			if(result.next()){
				String user = "U";
				if(user.equals(result.getString("status"))){
					loggedIn = true;
					loggedinID = result.getInt("user_ID");//change the user id to the user's id
				memberMenu();
				}
				else{
loggedIn = true;
					loggedinID =0;//zero when the staff are logged in
					staffMenu();
				}
			}
			else{
				System.out.println("Invalid username or password.");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

public static void searchForMovie() {
	boolean searchMenu = true;
	while(searchMenu){
		System.out.println("Search Movies by:\n"
				+ "1\t-\tTitle\n"
				+ "2\t-\tDirector\n"
				+ "3\t-\tGenre\n"
				+ "4\t-\tActor\n"
				+ "5\t-\tGo Return to Main Menu");
	int searchSelect = input.nextInt();
		switch(searchSelect){
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		case 5: searchMenu = false;
			break;
		default: System.out.println("Invalid input");
			break;
		}
	}
	}


public static void memberMenu(){//all the menu options after logging in
		boolean mm = true;
		while(mm){
		System.out.println(
				"Welcome Member!\n"
				+ "1\t-\tView Account Information\n"
				+ "2\t-\tRate a Movie\n"
				+ "3\t-\tRate a Person\n"
				+ "4\t-\tChoose a Ticket/ Browse Movies\n"
				+ "5\t-\tSearch for Movies\n"
				+ "6\t-\tLog Out\n");
		int mmselect = input.nextInt();
		switch (mmselect){
		case 1: 
			userSettings();
			break;
		case 2: 
			rateMovie();
			break;
		case 3: ratePerson();
			break;
		case 4: 
			viewMovieList();
			break;
		case 5: searchForMovie();
			break;
		case 6: loggedIn = false;
			System.out.println("Logged out.\n");
			mm = false;
			break;
		default: System.out.println("Invalid Input");
			break;
		}
		}
	}

public static void rateMovie() {
		//list the movies
		//allow to select a movie_id and enter a rating
		boolean menu = true;
		while(menu)
		{
			System.out.println("Select a movie to rate:\n");
			int lastNumber = getMovieList();
			System.out.println(lastNumber + "\t-\tReturn to Original Menu\n");
			int menuselect = input.nextInt();
			if(menuselect == lastNumber)
				menu = false;
			else if(menuselect > lastNumber)
				System.out.println("Invalid input\n");
			else
			{
				try {
					String maxquery = "select max(rating_movie_id) as max from Rating_Movies";
					stmt = connection.createStatement();
					ResultSet result1 = stmt.executeQuery(maxquery);
					if(result1.next()){
					int maxID = result1.getInt("max") + 1;
					System.out.println("Choose a rating out of 10:");
					double rating = input.nextDouble(); //have the user choose a rating
					
					String query2 = " INSERT INTO Rating_Movies VALUES (" + maxID + "," + loggedinID +"," 
					+ menuselect +","+ rating+")"; //create a query for adding to the database
					ResultSet addRating = stmt.executeQuery(query2);
					if(addRating.next())
					{
						System.out.println("Rating added. To view ratings, go to Account Information Menu.");
					}
					else{
					System.out.println("Error in adding rating.");
					}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				System.out.println("\nPress Enter to go back");
				try{System.in.read();}  
				catch(Exception e){}
				
			}
		}
		
	}


public static void ratePerson() {
	boolean menu = true;
	while(menu)
	{
		int lastNumber = 1;
		System.out.println("Select a person to rate:\n");
		//display all the people
		try {
			stmt = connection.createStatement();
			String pquery  = "select name, person_ID from Person";
			ResultSet peopleResult = stmt.executeQuery(pquery);
			while(peopleResult.next()){
				System.out.println(peopleResult.getInt("person_ID") + "\t-\t" + peopleResult.getString("name"));
				lastNumber++;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		
		System.out.println("\n"+lastNumber + "\t-\tReturn to Original Menu\n");
		int menuselect = input.nextInt();
		if(menuselect == lastNumber)
			menu = false;
		else if(menuselect > lastNumber)
			System.out.println("Invalid input\n");
		else
		{
			try {
				String maxquery = "select max(rating_person_id) as max from Rating_Person";
				stmt = connection.createStatement();
				ResultSet result1 = stmt.executeQuery(maxquery);
				if(result1.next()){
				int maxID = result1.getInt("max") + 1;
				System.out.println("Choose a rating out of 10:");
				double rating = input.nextDouble(); //have the user choose a rating
				
				String query2 = " INSERT INTO Rating_Person VALUES (" + maxID + "," + loggedinID +"," 
				+ menuselect +","+ rating+")"; //create a query for adding to the database
				ResultSet addRating = stmt.executeQuery(query2);
				if(addRating.next())
				{
					System.out.println("Rating added. To view ratings, go to Account Information Menu.");
				}
				else{
				System.out.println("Error in adding rating.");
				}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			System.out.println("\nPress Enter to go back");
			try{System.in.read();}  
			catch(Exception e){}
			
		}
	}
		
	}




	public static void userSettings() {
		boolean usermenu = true;
		while(usermenu){
		System.out.println("1\t-\tView your Ratings\n"
				+ "2\t-\tView Tickets Purchased\n"
				+ "3\t-\tView Payment Methods Used\n"
				+ "4\t-\tGo back\n");
		int userchoose = input.nextInt();
		switch(userchoose){
		case 1: //view ratings
			try {
				stmt = connection.createStatement();
				String personQuery = "select Rating_Person.rating as rating, Person.name as name "
						+ "from Rating_Person, Person"
						+ " where Rating_Person.user_ID=" + loggedinID
						+ " and Rating_Person.person_ID = Person.person_ID";
				String movieQuery = "select Rating_Movies.rating as rating, Movies.title as title"
						+ " from Rating_Movies, Movies"
						+ " where Rating_Movies.user_ID =" +loggedinID +
					" and Movies.movie_ID = Rating_Movies.movie_ID";
				ResultSet resultP = stmt.executeQuery(personQuery); //all the person ratings
				System.out.println("People Ratings:");
				while(resultP.next()){
					System.out.println(resultP.getString("name") + "\tRating: " + resultP.getDouble("rating")+"/10");
				}
				
				ResultSet resultM = stmt.executeQuery(movieQuery); //all the movie ratings
				System.out.println("\nMovie Ratings:");
				while(resultM.next()){
					System.out.println(resultM.getString("title") + "\tRating: " + resultM.getDouble("rating")+"/10");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("\nPress Enter to go back");
			try{System.in.read();}  
			catch(Exception e){} 
			
			break;
		case 2: //tickets purchased
			try{
				String query = "select Movies.title as title, Tickets.tickets_ID as ticketID,"
					+ " Room.room_number as room, Showings.time as time, Tickets.price as price"
					+ " from Tickets, Showings, Movies, Room"
					+ " where Tickets.user_ID=" + loggedinID
					+ " and Showings.room_ID = Room.room_ID"
					+ " and Tickets.showing_ID = Showings.showing_ID"
					+ " and Showings.movie_ID = Movies.movie_ID"; //query to get user ticket information
			
			stmt = connection.createStatement();
			ResultSet result = stmt.executeQuery(query);
			int ticketcounter = 1;
			while(result.next()){
				System.out.println(ticketcounter + " - Ticket number: " + result.getString("ticketID")
						+ "\nMovie: " + result.getString("title") + "\nTime: " + result.getString("time")
						+ "\nRoom Number: " + result.getString("room") + "\nPrice: $" + result.getDouble("price"));
				System.out.println("\n");
				ticketcounter ++;
			}
		}
			catch (SQLException e) {
				e.printStackTrace();
			}
			
			System.out.println("\nPress Enter to go back");
			try{System.in.read();}  
			catch(Exception e){} 
			
			break;
		case 3: //payment method
			System.out.println("All previous payment methods:");
			try {
				String query = "select* from Payment where user_ID = " + loggedinID;
				stmt = connection.createStatement();
				ResultSet result = stmt.executeQuery(query);
				while(result.next()){
					System.out.println(result.getString("method"));
				}
				System.out.println("\nPress Enter to go back");
				try{System.in.read();}  
				catch(Exception e){} 
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		case 4: usermenu = false;
			break;
		default: System.out.println("Invalid input");
			break;
			}
		
		}
	}


public static void staffMenu(){//all the menu options for staff members
		boolean sm = true;
		while(sm){
		System.out.println("Welcome Staff!\n"
				+ "1\t-\tUpdate Movie List\n"
				+ "2\t-\tUpdate Showings\n"
				+ "3\t-\tView Room Information\n"
				+ "4\t-\tBrowse Movies\n"
				+ "5\t-\tSearch for Movies\n"
				+ "6\t-\tLog Out\n");
		int smselect = input.nextInt();
		
		switch (smselect){
		case 1: updateMovies();
			break;
		case 2: updateShowings();
			break;
		case 3: viewRoomInfo();
			break;
		case 4: viewMovieList();//browse movies
			break;
		case 5: searchForMovie();
			break;
		case 6:
			System.out.println("Logged out.\n");
			sm = false;
			break;
		default: System.out.println("Invalid Input");
			break;
		}
		
		}
		}



	public static void viewRoomInfo() {
		System.out.println("Room ID\t\tRoom Number\tCapacity");
		
			try {
				stmt = connection.createStatement();
				String query = "select* from Room";
				ResultSet result = stmt.executeQuery(query);
				while(result.next()){
					System.out.println(result.getInt("room_ID")+"\t\t"+result.getInt("room_number")+"\t\t"+result.getInt("capacity"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("Press Enter to go back");
			try{System.in.read();}  
			catch(Exception e){}		
	}




	public static void updateShowings() {
		// TODO Auto-generated method stub
				boolean menu1 = true;
				while(menu1){
					System.out.println("1\t-\tChange Showtime Detail\n"
							+ "2\t-\tAdd new Showtime\n"
							+ "3\t-\tDelete Showtime\n"
							+ "4\t-\tGo back\n");
					int menu1choice = input.nextInt();
					
					switch(menu1choice){
					case 1://list all the showtimes
						//select which showtime to change
						

						int lastNumber = 1;
						String showings = "select Showings.showing_ID as showing_ID, Movies.title as title,"
								+ " Showings.time as time, Showings.room_ID as room_ID"
								+ " from Showings, Movies where Movies.movie_ID = Showings.movie_ID";
						try {
							stmt = connection.createStatement();
							ResultSet showResults = stmt.executeQuery(showings);
							while(showResults.next()){
								System.out.println(showResults.getInt("showing_ID") + "\t-\t" + showResults.getString("title")+" Time: " + 
							showResults.getString("time") + " Room: " + showResults.getInt("room_ID"));
									lastNumber++;
							}
							
							System.out.println("\n"+lastNumber + "\t-\tGo back");
						
						} catch (SQLException e1) {
							e1.printStackTrace();}
						
						
						
						
						int menuselect = input.nextInt(); //choose showtime to update
						if(menuselect == lastNumber){
							break;
						}
						else if(menuselect > lastNumber)
							System.out.println("Invalid input\n");
						else
						{
							//list current option and offer their input
							String movieQ = "select* from Showings where showing_ID =" + menuselect;
							try {
								stmt = connection.createStatement();
								ResultSet showingInfo = stmt.executeQuery(movieQ);
								showingInfo.next();
								
								System.out.println("Current movie ID: " + showingInfo.getString("movie_ID")
										+ "\nChange movie ID to: ");
								int newMovie = input.nextInt();
								input.nextLine();
								System.out.println("\nCurrent room: " + showingInfo.getString("room_ID")
										+ "\nChange room to: ");
								int newRoom = input.nextInt();
								input.nextLine();
								System.out.println("\nCurrent time: " + showingInfo.getString("time")
										+ "\nChange time to: ");
								String newTime = input.nextLine();
								System.out.println("\nCurrent # tickets left: " + showingInfo.getInt("tickets_left")
										+ "\nChange # tickets left to: ");
								int newTickets = input.nextInt();
								
								
								
								System.out.println("\n\nThe updated showing will be:\nMovie ID: " + newMovie
										+"\nRoom: " + newRoom + "\nTime: " + newTime + "\n# of ticket left: " + newTickets +
										 "\n\nWould you like to save these changes? (y/n)");
								String change = input.next();
								if(change.equals("y"))
								{	
								//append the database
								String changeShowing = "update Showings "
										+ "set movie_ID ="+ newMovie +", room_ID ="+newRoom+", "
										+"time ='"+newTime + "', tickets_left = " + newTickets 
										+ " where showing_ID = " + menuselect;
								ResultSet changeS = stmt.executeQuery(changeShowing);
								if(changeS.next()){
									System.out.println("Changes were saved.\n");
								}
								else{change = "n";}
								}
								else if (change.equals("n"))
									System.out.println("Changes were not saved.\n");
								else 
									System.out.println("Invalid input, please try again.\n\n");
								
							} catch (SQLException e) {
								e.printStackTrace();
							}
							
							//display new version if char = yes, execute query to add to database
						}
						break;
					case 2: //ask for new attributes and make a new movie in the database
						input.nextLine();

						System.out.println("Movie ID: " );
						int newMovie = input.nextInt();
						input.nextLine();
						System.out.println("\nRoom: ");
						int newRoom = input.nextInt();
						input.nextLine();
						System.out.println("\nTime: ");
						String newTime = input.nextLine();
						System.out.println("\n# tickets left: ");
						int newTickets = input.nextInt();
						
						//figure out the last ID+1 for the next ID of the movie
						try {
							int maxID = 0;
							String maxquery = "select max(showing_ID) as max from Showings";
							stmt = connection.createStatement();
							ResultSet result1 = stmt.executeQuery(maxquery);
							if(result1.next()){
							maxID = result1.getInt("max") + 1;
							}
							
						System.out.println("\n\nThe showing will be:\n Movie ID: " + newMovie
								+"\nRoom: " + newRoom + "\nTime: " + newTime + "\n# of tickets left: " + newTickets +
								 "\n\nWould you like to add this showing to the database? (y/n)");
						String change = input.next();
						if(change.equals("y"))
						{	
						//add to the database
						String newShowing = " INSERT INTO Showings VALUES (" + maxID + " , " + newMovie + "," 
								+ newRoom +", '"+ newTime+ "' , " + newTickets+ ")";
						ResultSet addMovie = stmt.executeQuery(newShowing);
						if(addMovie.next())System.out.println("Changes were saved.");
						else change = "n";
						}
						else if (change.equals("n"))
							System.out.println("Changes were not saved.\n");
						else 
							System.out.println("Invalid input, please try again.");
						
						}
						catch (SQLException e) {
								e.printStackTrace();
						}
						break;
					
					case 3://delete
						
						System.out.println("Select a showing to delete:\n");

						int lastNumberDelete = 1;
						String showingsDel = "select Showings.showing_ID as showing_ID, Movies.title as title,"
								+ " Showings.time as time, Showings.room_ID as room_ID"
								+ " from Showings, Movies where Movies.movie_ID = Showings.movie_ID";
						try {
							stmt = connection.createStatement();
							ResultSet showResults = stmt.executeQuery(showingsDel);
							while(showResults.next()){
								System.out.println(showResults.getInt("showing_ID") + "\t-\t" + showResults.getString("title")+" Time: " + 
							showResults.getString("time") + " Room: " + showResults.getInt("room_ID"));
									lastNumberDelete++;
							}
							
							System.out.println("\n"+lastNumberDelete + "\t-\tGo back");
						
						} catch (SQLException e1) {
							e1.printStackTrace();}
						int menuselectDelete = input.nextInt(); 
						////list all the showings
						
						if(menuselectDelete == lastNumberDelete){
							break;
						}
						else if(menuselectDelete > lastNumberDelete)
							System.out.println("Invalid input\n");
						else
						{
							System.out.println("Are you sure you want to delete the showing " + menuselectDelete + "? (y/n)");
							String delete = input.next();
							if(delete.equals("y"))
							{
								String deleteQ = "delete from Showings where showing_ID = " + menuselectDelete;
								try {
									stmt = connection.createStatement();
									ResultSet deleteResult = stmt.executeQuery(deleteQ);
									if(deleteResult.next()){
										System.out.println("Showing deleted\n\n");
									}
									else{delete = "n";}
								} catch (SQLException e) {
									e.printStackTrace();}
								
								//delete
								
							}
							else 
							{
								System.out.println("Showing not deleted\n\n");
							}
						}
						break;
					case 4: menu1=false; //exit out of this menu
						break;
					default: System.out.println("Invalid input");
						break;
					}
				}
		
	}


	private static void createAccount() {
		// TODO Auto-generated method stub
		//check for apostrophes
		
	}
	
	public static void updateMovies() {
		// TODO Auto-generated method stub
		boolean menu1 = true;
		while(menu1){
			System.out.println("1\t-\tChange Movie Detail\n"
					+ "2\t-\tAdd new Movie\n"
					+ "3\t-\tDelete Movie\n"
					+ "4\t-\tGo back\n");
			int menu1choice = input.nextInt();
			switch(menu1choice){
			case 1://list all the movies
				//select which movie to change
				int lastNumber = getMovieList();
				System.out.println("\n"+lastNumber + "\t-\tGo back");
				int menuselect = input.nextInt(); //choose movie to update
				if(menuselect == lastNumber){
					break;
				}
				else if(menuselect > lastNumber)
					System.out.println("Invalid input\n");
				else
				{
					//list current option and offer their input
					String movieQ = "select* from Movies where title = '" + movieList.get(menuselect-1) + "'";
					try {
						stmt = connection.createStatement();
						ResultSet movieInfo = stmt.executeQuery(movieQ);
						movieInfo.next();
						input.nextLine();
						System.out.println("Current title: " + movieInfo.getString("title")
								+ "\nChange title to: ");
						String newTitle = input.nextLine();
						System.out.println("\nCurrent genre: " + movieInfo.getString("genre")
								+ "\nChange genre to: ");
						String newGenre = input.nextLine();
						System.out.println("\nCurrent year: " + movieInfo.getInt("year")
								+ "\nChange year to: ");
						int newYear = input.nextInt();
						System.out.println("\nCurrent length: " + movieInfo.getInt("length")
								+ "\nChange length to: ");
						int newLength = input.nextInt();
						input.nextLine();
						System.out.println("\nCurrent PG Rating: " + movieInfo.getString("PG_rating")
								+ "\nChange PG Rating to: ");
						String newPGRating = input.nextLine();
						
						
						System.out.println("\n\nThe updated movie will be:\nTitle: " + newTitle
								+"\nGenre: " + newGenre + "\nYear: " + newYear + "\nLength: " + newLength +
								"\nPG Rating: " + newPGRating + "\n\nWould you like to save these changes? (y/n)");
						String change = input.next();
						if(change.equals("y"))
						{	
						//append the database
						String changeMovie = "update Movies "
								+ "set title ='"+ newTitle +"', genre ='"+newGenre+"', "
								+"year ="+newYear + ", length = " + newLength + ", PG_rating = '"
										+ newPGRating + "'"
								+ " where title = '" + movieList.get(menuselect-1) +"'";
						ResultSet changeM = stmt.executeQuery(changeMovie);
						if(changeM.next()){
							System.out.println("Changes were saved.\n");
						}
						else{change = "n";}
						}
						else if (change.equals("n"))
							System.out.println("Changes were not saved.\n");
						else 
							System.out.println("Invalid input, please try again.\n\n");
						
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
					//display new version if char = yes, execute query to add to database
				}
				break;
			case 2: //ask for new attributes and make a new movie in the database
				input.nextLine();

				System.out.println("\nTitle: ");
				String newTitle = input.nextLine();
				System.out.println("\nGenre: ");
				String newGenre = input.nextLine();
				System.out.println("\nSecond Genre: ");
				String newGenre2 = input.nextLine();
				System.out.println("\nYear: ");
				int newYear = input.nextInt();
				System.out.println("\nLength: ");
				int newLength = input.nextInt();
				input.nextLine();
				System.out.println("\nPG Rating:");
				//String random1 = input.next();
				String newPGRating = input.nextLine();
				
				//figure out the last ID+1 for the next ID of the movie
				try {
					int maxID = 0;
					String maxquery = "select max(rating_movie_id) as max from Rating_Movies";
					stmt = connection.createStatement();
					ResultSet result1 = stmt.executeQuery(maxquery);
					if(result1.next()){
					maxID = result1.getInt("max") + 1;
					}
					
				System.out.println("\n\nThe movie will be:\nTitle: " + newTitle
						+"\nGenre: " + newGenre + "\nYear: " + newYear + "\nLength: " + newLength +
						"\nPG Rating: " + newPGRating + "\n\nWould you like to add this movie to the database? (y/n)");
				String change = input.next();
				if(change.equals("y"))
				{	
				//add to the database
				String newMovie = " INSERT INTO Movies VALUES (" + maxID + " , '" + newTitle +"' , '" 
						+ newGenre +"',"+ newYear+ " , " + newLength+ " , '" + newPGRating +"' , '"+ newGenre2 +"')";
				ResultSet addMovie = stmt.executeQuery(newMovie);
				if(addMovie.next())System.out.println("Changes were saved.");
				else change = "n";
				}
				else if (change.equals("n"))
					System.out.println("Changes were not saved.\n");
				else 
					System.out.println("Invalid input, please try again.");
				
				}
				catch (SQLException e) {
						e.printStackTrace();
				}
				break;
			
			case 3://delete
				
				int lastNumberDelete = getMovieList();
				System.out.println("\n"+lastNumberDelete + "\t-\tGo back");
				int menuselectDelete = input.nextInt(); //choose movie to update
				if(menuselectDelete == lastNumberDelete){
					break;
				}
				else if(menuselectDelete > lastNumberDelete)
					System.out.println("Invalid input\n");
				else
				{
					System.out.println("Are you sure you want to delete the movie " + movieList.get(menuselectDelete-1) + "? (y/n)");
					String delete = input.next();
					if(delete.equals("y"))
					{
						String deleteQ = "delete from Movies where title = '" + movieList.get(menuselectDelete-1) + "'";
						try {
							stmt = connection.createStatement();
							ResultSet deleteResult = stmt.executeQuery(deleteQ);
							if(deleteResult.next()){
								System.out.println("Movie deleted\n\n");
							}
							else{delete = "n";}
						} catch (SQLException e) {
							e.printStackTrace();}
						
						//delete
						
					}
					else if (delete.equals("n"))
					{
						System.out.println("Movie not deleted\n\n");
					}
				}
				break;
			case 4: menu1=false; //exit out of this menu
				break;
			default: System.out.println("Invalid input");
				break;
			}
		}	
	}
}