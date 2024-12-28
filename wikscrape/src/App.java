//When modifying use: https://jsoup.org/apidocs/org/jsoup/select/Selector.html

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class App 
{
    public static void main(String[] args)
    {

        //List of URLs to parse tables of (change to what is needed)
        String[] URLs = {"https://en.wikipedia.org/wiki/List_of_Capcom_games:_0-D", "https://en.wikipedia.org/wiki/List_of_Capcom_games:_E-L", "https://en.wikipedia.org/wiki/List_of_Capcom_games:_M", 
        "https://en.wikipedia.org/wiki/List_of_Capcom_games:_N-R", "https://en.wikipedia.org/wiki/List_of_Capcom_games:_S", "https://en.wikipedia.org/wiki/List_of_Capcom_games:_T-Z"};

        //List of stored games, to be sorted by release date.
        Map<String, String> games = new TreeMap<>();

        //Iterate for each url
        for (String string:URLs)
        {
            try
            {
                //Connect to URL
                Document doc = Jsoup.connect(string).get();

                //Vars needed for storing info for future comparisons
                String prevName = "";
                String[] prev_name = {""};
                String[] prev_date = {"000000009999-99-99-0000"};
                String[] disp_date = new String[1];
                String[] system = new String[1];

                //Iterate over each row in the table
                for (Element row: doc.select("table tr"))
                    {
                        //Get relevant info for each title
                        String title = row.select("td:eq(0) i a").text();
                        if(title.equals(""))
                        title = row.select("td:eq(0) i").text(); //Handles titles with no hyperlinks
                        String date = row.select("td span[data-sort-value]").attr("data-sort-value");
                        if(!(date.equals(""))) {prev_date[0] = date;}
                        disp_date[0] = row.select("td span[data-sort-value]").text();
                        system[0] = row.select("td > a").text();

                        //For any rows with blank titles, uses the last non-blank title
                        if(!(title.equals("")))
                        {
                            prevName = title;

                                //For each title, scan over every date to determine which version came out first (we want to play versions for the original respective platforms)
                                Element next = row.nextElementSibling();
                                while(next != null && (next.select("td i a").text().equals("") || next.select("td i a").text().equals(prev_name[0])))
                                {
                                    //System.out.println(next.select("td span[data-sort-value]").attr("data-sort-value"));
                                    if(!(next.select("td span[data-sort-value]").attr("data-sort-value").equals("")) && next.select("td span[data-sort-value]").attr("data-sort-value").compareTo(prev_date[0]) < 0)
                                    {
                                        prev_date[0] = next.select("td span[data-sort-value]").attr("data-sort-value");
                                        disp_date[0] = next.select("td span[data-sort-value]").text();
                                        system[0] = next.select("td > a").text();
                                    }

                                    next = next.nextElementSibling();
                                }
                                date = prev_date[0];
                        }

                        //If there is no release date, we don't even need it hence skip
                        if(!(date.equals("")))
                        {


                            
                            //Essentially filters out any duplicate titles
                            if(!(prevName.equals(prev_name[0])))
                            {
                                prev_name[0] = prevName;

                                //Check if there are any games released on the same day, if there is, differentiate the two so both are added
                                while(games.containsKey(date))
                                {date=date+"1";}

                                //Add data together then add it to the map to be sorted
                                title = prevName + " - "+disp_date[0]+": "+system[0];
                                //System.out.println(title);
                                games.put(date, title);

                                //Reset default date (for determining oldest platform)
                                prev_date[0] = "000000009999-99-99-0000";
                            }
                        }   
                    }
                
                //Write data to file then close
                try 
                    {
                        FileWriter file = new FileWriter("C:\\Users\\demae\\Desktop\\test.txt");
                        for(Map.Entry<String, String> entry : games.entrySet())
                        {
                            file.write(entry.getValue()+"\n");
                        }

                        file.write("\nTotal entries: "+games.size());
                        file.close();
                    } 
                catch (IOException e) {e.printStackTrace();}
                
                
            }
            catch(Exception ex){}
        }
    }
}