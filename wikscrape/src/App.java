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
        String[] URLs = {"https://en.wikipedia.org/wiki/List_of_Capcom_games:_0-D", "https://en.wikipedia.org/wiki/List_of_Capcom_games:_E-L", "https://en.wikipedia.org/wiki/List_of_Capcom_games:_M", 
        "https://en.wikipedia.org/wiki/List_of_Capcom_games:_N-R", "https://en.wikipedia.org/wiki/List_of_Capcom_games:_S", "https://en.wikipedia.org/wiki/List_of_Capcom_games:_T-Z"};

            /* Pretty much everything else you will need to modify with
            some rare exceptions, use this link as a guide: https://jsoup.org/apidocs/org/jsoup/select/Selector.html
        /*--------------------------------------------------------------------*/
        /* */       String titleLoc = "td:eq(0)";                             // 
        /* */       String dateLoc = "td span[data-sort-value]";              // 
        /* */       String systemLoc = "td > a";                              // 
        /*--------------------------------------------------------------------*/ 

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
                int date_type = 1; //0 for FULL, 1 for YEAR
                String[] prev_date = {"9999"}; //Format used by default when date is YEAR formatted as ex. "1984"
                if(!(doc.select("table tr:eq(1) " + dateLoc).attr("data-sort-value").equals(""))) //Checks if date format instead is the FULL date (ex. "November 18th, 1984")
                    {
                        prev_date[0] = "000000009999-99-99-0000";
                        date_type = 0;
                    }

                String[] disp_date = new String[1];
                String[] system = new String[1];

                //Iterate over each row in the table
                for (Element row: doc.select("table tr"))
                    {
                        //System.out.println(row.select(dateLoc).text());

                        //Get relevant info for each title
                        String titleTextLoc = titleLoc + " i a"; //Gets the exact location of where the text is (can vary due to some entries missing hyperlinks)
                        String title = row.select(titleTextLoc).text();
                        if(title.equals("")) //Handles titles with no hyperlinks
                        {
                            titleTextLoc = titleLoc + " i";
                            title = row.select(titleTextLoc).text();
                        }
                        

                        String date = "";
                        switch(date_type)
                        {
                            case 0:
                            date = row.select(dateLoc).attr("data-sort-value");
                            disp_date[0] = row.select(dateLoc).text();
                            break;
                            
                            case 1:
                            date = row.select(dateLoc).text();
                            disp_date[0] = date;
                            break;
                        }
                        
                        if(!(date.equals(""))) {prev_date[0] = date;}
                        system[0] = row.select(systemLoc).text();

                        //For any rows with blank titles, uses the last non-blank title
                        if(!(title.equals("")))
                        {
                            prevName = title;

                                //For each title, scan over every date to determine which version came out first (we want to play versions for the original respective platforms)
                                Element next = row.nextElementSibling();
                                while(next != null && (next.select(titleTextLoc).text().equals("") || next.select(titleTextLoc).text().equals(prev_name[0])))
                                {
                                    //System.out.println(next.select(dateLoc).attr("data-sort-value"));
                                    if(!(next.select(dateLoc).attr("data-sort-value").equals("")) && next.select(dateLoc).attr("data-sort-value").compareTo(prev_date[0]) < 0)
                                    {
                                        prev_date[0] = next.select(dateLoc).attr("data-sort-value");
                                        disp_date[0] = next.select(dateLoc).text();
                                        system[0] = next.select(systemLoc).text();
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
