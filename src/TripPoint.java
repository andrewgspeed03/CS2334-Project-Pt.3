/**
 * @author good0161
 * @verison 3.0.1
 * 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class TripPoint {

	private double lat;	// latitude
	private double lon;	// longitude
	private int time;	// time in minutes
	
	private static ArrayList<TripPoint> trip;	// ArrayList of every point in a trip
	private static ArrayList<TripPoint> movingTrip;

	// default constructor
	public TripPoint() {
		time = 0;
		lat = 0.0;
		lon = 0.0;
	}
	
	// constructor given time, latitude, and longitude
	public TripPoint(int time, double lat, double lon) {
		this.time = time;
		this.lat = lat;
		this.lon = lon;
	}
	
	// returns time
	public int getTime() {
		return time;
	}
	
	// returns latitude
	public double getLat() {
		return lat;
	}
	
	// returns longitude
	public double getLon() {
		return lon;
	}
	
	// returns a copy of trip ArrayList
	public static ArrayList<TripPoint> getTrip() {
		return new ArrayList<>(trip);
	}

	/**
	 * returns a copy of the movingTrip ArrayList
	 * @return	movingTrip ArrayList
	 */
	public static ArrayList<TripPoint> getMovingTrip(){
		return new ArrayList<>(movingTrip);
	}
	
	// uses the haversine formula for great sphere distance between two points
	public static double haversineDistance(TripPoint first, TripPoint second) {
		// distance between latitudes and longitudes
		double lat1 = first.getLat();
		double lat2 = second.getLat();
		double lon1 = first.getLon();
		double lon2 = second.getLon();
		
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) *
                   Math.cos(lat1) *
                   Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
	}
	
	// finds the average speed between two TripPoints in km/hr
	public static double avgSpeed(TripPoint a, TripPoint b) {
		
		int timeInMin = Math.abs(a.getTime() - b.getTime());
		
		double dis = haversineDistance(a, b);
		
		double kmpmin = dis / timeInMin;
		
		return kmpmin*60;
	}

	/**
	 * Finds the average speed over the course of the trip moving points in km/hr
	 * @return average speed while moving in km/hr
	 */
	public static double avgMovingSpeed(){
		double totDis = 0;
		double timeInHrs = movingTime();
		double kmphr;

		TripPoint a;
		TripPoint b;

		for(int i = 1; i < trip.size(); i++){
			a = movingTrip.get(i-1);
			b = movingTrip.get(i);
			totDis += haversineDistance(a, b);
		}

		kmphr = totDis / timeInHrs;

		return kmphr;
	}
	
	// returns the total time of trip in hours
	public static double totalTime() {
		int minutes = trip.get(trip.size()-1).getTime();
		double hours = minutes / 60.0;
		return hours;
	}

	/**
	 * Finds the amount of time moving in hours
	 * @return amount of time moving in hours
	 */
	public static double movingTime(){
		int minutes = movingTrip.get(movingTrip.size()-1).getTime();
		double hours = minutes / 60;
		return hours;
	}

	/**
	 * Finds amount of time spent stopped in hours
	 * @return time spent stopped in hours
	 */
	public static double stoppedTime(){
		double timeInMin = h1StopDetection() * 5;
		double timeInHrs = timeInMin / 60;

		return timeInHrs;
	}
	
	// finds the total distance traveled over the trip
	public static double totalDistance() throws FileNotFoundException, IOException {
		
		double distance = 0.0;
		
		if (trip.isEmpty()) {
			readFile("triplog.csv");
		}
		
		for (int i = 1; i < trip.size(); ++i) {
			distance += haversineDistance(trip.get(i-1), trip.get(i));
		}
		
		return distance;
	}
	
	public String toString() {
		
		return null;
	}

	public static void readFile(String filename) throws FileNotFoundException, IOException {

		// construct a file object for the file with the given name.
		File file = new File(filename);

		// construct a scanner to read the file.
		Scanner fileScanner = new Scanner(file);
		
		// initiliaze trip
		trip = new ArrayList<TripPoint>();

		// create the Array that will store each lines data so we can grab the time, lat, and lon
		String[] fileData = null;

		// grab the next line
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();

			// split each line along the commas
			fileData = line.split(",");

			// only write relevant lines
			if (!line.contains("Time")) {
				// fileData[0] corresponds to time, fileData[1] to lat, fileData[2] to lon
				trip.add(new TripPoint(Integer.parseInt(fileData[0]), Double.parseDouble(fileData[1]), Double.parseDouble(fileData[2])));
			}
		}

		// close scanner
		fileScanner.close();
	}

	/**
	 * Stops use the second heuristic with a stop radius of 0.6km to detect stops.
	 * Intializes and fills movingTrip with all non-stop points
	 * @return number of stops in trip ArrayList
	 */
	public static int h1StopDetection(){
		double dis;
		int numStops = 0;

   		movingTrip = new ArrayList<TripPoint>();
    	TripPoint a;
		TripPoint b;

   		for (int i = 1; i < trip.size(); i++) {
        	a = trip.get(i-1);
			b = trip.get(i);
            dis = haversineDistance(a, b);
			if(i == 1)
				movingTrip.add(a);
            if (dis > 0.6)  // Use 0.6 km as the distance threshold
                movingTrip.add(b);
			else 
                numStops++;
    	}


		return numStops;
	}

	/**
	 * Stops use the second heuristic with a stop radius of 0.5km to detect stops.
	 * Intializes and fills movingTrip with all non-stop points
	 * @return number of stops in trip ArrayList
	 */
	public static int h2StopDetection(){
		double dis;
		int numStops = 0;
		movingTrip = new ArrayList<TripPoint>();

		TripPoint a;
		TripPoint b;

		for(int i = 1; i < trip.size(); i++){
			a = trip.get(i-1);
			b = trip.get(i);
			dis = haversineDistance(a, b);
			if(dis > 0.5)
				movingTrip.add(b);
			else
				numStops++;
		}

		return numStops;
	}

}
