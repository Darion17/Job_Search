import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


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

        boolean doXula = false;
        boolean doJobs = false;
        String otherUrl = null;
        Path outPath = Path.of("fake_jobs.csv");

        for (String a : args) {
            if (a.equalsIgnoreCase("--xula")) doXula = true;
            else if (a.equalsIgnoreCase("--jobs")) doJobs = true;
            else if (a.startsWith("--other-url=")) otherUrl = a.substring("--other-url=".length());
            else if (a.startsWith("--out=")) outPath = Path.of(a.substring("--out=".length()));
        }

        try {
            if (doXula) {
                String xula = scrapeXulaMission();
                System.out.println(BOLD + "[XULA Mission]" + RESET);
                System.out.println(snippet(xula, 700));
                System.out.println();
            }

            if (otherUrl != null && !otherUrl.isBlank()) {
                String other = scrapeUniversityMission(otherUrl, null); // pass a CSS selector if you want
                System.out.println(BOLD + "[Other University Mission]" + RESET);
                System.out.println(snippet(other, 700));
                System.out.println();
            }

            if (doJobs) {
                List<String[]> rows = scrapeFakeJobs();
                writeCsv(outPath, rows);
                System.out.println(BOLD + ("Wrote " + rows.size() + " rows to " + outPath.toAbsolutePath()) + RESET);
            }

            if (!doXula && otherUrl == null && !doJobs) {
                System.out.println("Usage:");
                System.out.println("  java Scraper --xula");
                System.out.println("  java Scraper --other-url=https://example.edu/mission");
                System.out.println("  java Scraper --jobs [--out=fake_jobs.csv]");
            }
        } catch (IOException e) {
            System.err.println("Network or I/O error: " + e.getMessage());
        } catch (UncheckedIOException e) {
            System.err.println("Write error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e);
        }
    }
    static Document fetch(String url) throws IOException {
        // Reasonable defaults
        return Jsoup
            .connect(url)
            .userAgent("Mozilla/5.0 (Job_Search Student Project)")
            .timeout((int) Duration.ofSeconds(20).toMillis())
            .get();
    }

    static String clean(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", " ").trim();
    }

    static String snippet(String s, int max) {
        return (s.length() > max) ? s.substring(0, max) + "…" : s;
    }
    
static String scrapeXulaMission() throws IOException {
        String url = "https://www.xula.edu/about/mission-values.html";
        Document doc = fetch(url);

        Element container = doc.selectFirst("div.editorarea, .editorarea");
        String text = "";
        if (container != null) {
            text = clean(container.text());
        } else {
            // Fallback: find a heading containing "mission" and gather following siblings
            Element head = doc.selectFirst("h1:matchesOwn((?i)mission), h2:matchesOwn((?i)mission), h3:matchesOwn((?i)mission)");
            if (head != null) {
                StringBuilder sb = new StringBuilder();
                Element sib = head.nextElementSibling();
                int grabbed = 0;
                while (sib != null && grabbed < 6) {
                    if (List.of("p","div","section").contains(sib.tagName().toLowerCase())) {
                        sb.append(" ").append(sib.text());
                        grabbed++;
                    }
                    sib = sib.nextElementSibling();
                }
                text = clean(sb.toString());
            }
        }

        // Ensure the required substring is somewhere (per assignment hint)
        if (!text.contains("founded by Saint")) {
            String pageText = clean(doc.text());
            if (pageText.contains("founded by Saint")) {
                text = pageText;
            }
        }
        return text;
    }

     static String scrapeUniversityMission(String url, String containerSelector) throws IOException {
        Document doc = fetch(url);
        if (containerSelector != null && !containerSelector.isBlank()) {
            Element node = doc.selectFirst(containerSelector);
            if (node != null) return clean(node.text());
        }
        Element head = doc.selectFirst("h1:matchesOwn((?i)mission), h2:matchesOwn((?i)mission), h3:matchesOwn((?i)mission)");
        if (head != null) {
            StringBuilder sb = new StringBuilder();
            Element sib = head.nextElementSibling();
            int grabbed = 0;
            while (sib != null && grabbed < 8) {
                if (List.of("p","div","section").contains(sib.tagName().toLowerCase())) {
                    sb.append(" ").append(sib.text());
                    grabbed++;
                }
                sib = sib.nextElementSibling();
            }
            return clean(sb.toString());
        }
        return clean(doc.text());
    }
    static java.util.List<String[]> scrapeFakeJobs() throws java.io.IOException {
    String url = "https://realpython.github.io/fake-jobs/";
    org.jsoup.nodes.Document doc = fetch(url);

    java.util.List<String[]> rows = new java.util.ArrayList<>();
    // (We will prepend the header when writing CSV)
    for (org.jsoup.nodes.Element card : doc.select("div.card-content")) {
        org.jsoup.nodes.Element titleEl   = card.selectFirst("h2.title");
        org.jsoup.nodes.Element companyEl = card.selectFirst("h3.subtitle");
        org.jsoup.nodes.Element locationEl= card.selectFirst("p.location");
        org.jsoup.nodes.Element dateEl    = card.selectFirst("time");

        String title   = titleEl   != null ? clean(titleEl.text())    : "";
        String company = companyEl != null ? clean(companyEl.text())  : "";
        String location= locationEl!= null ? clean(locationEl.text()) : "";
        String datePosted = "";
        if (dateEl != null) {
            String dt = dateEl.hasAttr("datetime") ? dateEl.attr("datetime") : dateEl.text();
            datePosted = clean(dt);
        }

        if (!title.isEmpty() && !company.isEmpty()) {
            rows.add(new String[]{ title, company, location, datePosted });
        }
    }
    return rows;
}

static void writeCsv(java.nio.file.Path out, java.util.List<String[]> rows) {
    // ensure parent folder exists
    try {
        java.nio.file.Path parent = out.getParent();
        if (parent != null) java.nio.file.Files.createDirectories(parent);
    } catch (java.io.IOException ignored) {}

    try (java.io.BufferedWriter bw = java.nio.file.Files.newBufferedWriter(
            out, java.nio.charset.StandardCharsets.UTF_8)) {
        // required header
        bw.write(csvLine(new String[]{ "Job Title", "Company", "Location", "Date Posted" }));
        bw.newLine();
        // data rows
        for (String[] r : rows) {
            bw.write(csvLine(r));
            bw.newLine();
        }
    } catch (java.io.IOException e) {
        throw new java.io.UncheckedIOException(e);
    }
}

// minimal CSV escaping: wrap if contains comma/quote/newline, double quotes inside
static String csvLine(String[] fields) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < fields.length; i++) {
        if (i > 0) sb.append(',');
        sb.append(escapeCsv(fields[i]));
    }
    return sb.toString();
}

static String escapeCsv(String s) {
    if (s == null) return "";
    boolean needsQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
    String t = s.replace("\"", "\"\"");
    return needsQuote ? "\"" + t + "\"" : t;
}

}

