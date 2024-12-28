//When modifying use: https://jsoup.org/apidocs/org/jsoup/select/Selector.html
/* 
Note for future self: Each group of articles for whatever it may be that you are trying to
                      sort is formatted slightly differently, modifications to the program
                      are likely going to need to be made to some degree. Perhaps you might
                      even make some major generalization fixes or bug fixes while you are
                      at it, such as I did when writing this.
*/

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
        String[] URLs = {"https://en.wikipedia.org/wiki/List_of_arcade_video_games:_0%E2%80%939", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_A", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_B", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_C",
                        "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_D", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_E", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_F", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_G", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_H", 
                        "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_I", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_J", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_K", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_L", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_M", 
                        "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_N", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_O", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_P", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_Q", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_R", 
                        "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_S", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_T", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_U", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_V", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_W", 
                        "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_X", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_Y", "https://en.wikipedia.org/wiki/List_of_arcade_video_games:_Z"};

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
                int date_type = 0; //0 for FULL, 1 for YEAR
                String[] prev_date = {"000000009999-99-99-0000"}; //Format used by default when date is FULL formatted as ex. "November 18th, 1984"
                if(!(doc.select("table tr:eq(1) td:eq(2)").text().equals(""))) //Checks if date format instead is simply just YEAR (ex. 1984)
                    {
                        prev_date[0] = "9999";
                        date_type = 1;
                    }

                String[] disp_date = new String[1];
                String[] developer = new String[1];

                //Iterate over each row in the table
                for (Element row: doc.select("table tr"))
                    {
                        //System.out.println(row.select("td:eq(2)").text());

                        //Get relevant info for each title
                        String title = row.select("td:eq(0) i a").text();
                        if(title.equals(""))
                        title = row.select("td:eq(0) i").text(); //Handles titles with no hyperlinks

                        String date = "";
                        switch(date_type)
                        {
                            case 0:
                            date = row.select("td span[data-sort-value]").attr("data-sort-value");
                            disp_date[0] = row.select("td span[data-sort-value]").text();
                            break;
                            
                            case 1:
                            date = row.select("td:eq(2)").text();
                            disp_date[0] = date;
                            break;
                        }
                        
                        if(!(date.equals(""))) {prev_date[0] = date;}
                        developer[0] = row.select("td:eq(3)").text();

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
                                        developer[0] = next.select("td:eq(3)").text();
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
                                title = prevName + " - "+disp_date[0]+": "+developer[0];
                                //System.out.println(title);
                                games.put(date, title);

                                //Reset default date (for determining oldest platform)
                                switch(date_type)
                                {
                                    case 0:
                                    prev_date[0] = "000000009999-99-99-0000";
                                    break;

                                    case 1:
                                    prev_date[0] = "9999";
                                    break;
                                }
                            }
                        }   
                    }
                
                //Write data to file then close
                try 
                    {
                        FileWriter file = new FileWriter("test.txt");
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
