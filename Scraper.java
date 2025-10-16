package Job_Search;


public class Scraper {

    static final String RESET = "\u001B[0m";
    static final String BOLD = "\u001B[1m";
    static final String FG_GREEN = "\u001B[32m";
    static final String FG_CYAN = "\u001B[36m";
    static final String FG_YELLOW = "\u001B[33m";

    static final String ASCII_WELCOME = """
          _       _           _                          _    
         | |     | |         | |                        | |   
         | | ___ | |__   ___ | | ___  _ __   ___   _ __ | | __
     _   | |/ _ \\| '_ \\ / _ \\| |/ _ \\| '_ \\ / _ \\ | '_ \\| |/ /
    | |__| | (_) | |_) | (_) | | (_) | | | |  __/_| |_) |   < 
     \\____/ \\___/|_.__/ \\___/|_|\\___/|_| |_|\\___(_)_ .__/|_|\\_\\
                                                  | |          
                                                  |_|          
        """;

    static String explainPurpose() {
        return String.join("\n",
            "This app helps you explore job opportunities.",
            "- Scrapes or loads postings from selected sources",
            "- Lets you filter by role, location, and keywords",
            "- Preps clean summaries you can compare quickly",
            "",
            "Tip: Keep your repo private and add your instructor as a collaborator."
        );
    }

    public static void main(String[] args) {
        System.out.println(FG_GREEN + ASCII_WELCOME + RESET);
        System.out.println(BOLD + FG_CYAN + "Welcome to Job_Search!" + RESET);
        System.out.println();
        System.out.println(FG_YELLOW + "What does this app do?" + RESET);
        System.out.println(explainPurpose());
    }
}