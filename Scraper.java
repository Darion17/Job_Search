import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class Scraper {

    // ---------- ANSI + Banner ----------
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
                "Tip: Keep your repo private and add your instructor as a collaborator.");
    }

    // ---------- Main CLI ----------
    public static void main(String[] args) {
        System.out.println(FG_GREEN + ASCII_WELCOME + RESET);
        System.out.println(BOLD + FG_CYAN + "Welcome to Job_Search!" + RESET);
        System.out.println();
        System.out.println(FG_YELLOW + "What does this app do?" + RESET);
        System.out.println(explainPurpose());
        System.out.println();

        boolean doXula = false;
        boolean doHoward = false;
        boolean doJobs = false;
        String otherUrl = null;

        Integer sinceDays = null;
        boolean unique = false;
        boolean sortDesc = false;
        String keyword = null;
        Path outPath = Path.of("fake_jobs.csv");

        for (String a : args) {
            if (a.equalsIgnoreCase("--xula"))
                doXula = true;
            else if (a.equalsIgnoreCase("--howard"))
                doHoward = true;
            else if (a.equalsIgnoreCase("--jobs"))
                doJobs = true;
            else if (a.startsWith("--other-url="))
                otherUrl = a.substring("--other-url=".length());
            else if (a.startsWith("--out="))
                outPath = Path.of(a.substring("--out=".length()));
            else if (a.startsWith("--since-days="))
                sinceDays = Integer.parseInt(a.substring("--since-days=".length()));
            else if (a.equalsIgnoreCase("--unique"))
                unique = true;
            else if (a.equalsIgnoreCase("--sort-desc"))
                sortDesc = true;
            else if (a.startsWith("--keyword="))
                keyword = a.substring("--keyword=".length());
        }

        try {
            if (doXula) {
                String xula = scrapeXulaMission();
                System.out.println(BOLD + "[XULA Mission]" + RESET);
                System.out.println(snippet(xula, 700));
                System.out.println();
            }

            if (doHoward) {
                String howard = scrapeHowardMission();
                System.out.println(BOLD + "[Howard Mission]" + RESET);
                System.out.println(howard);
                System.out.println();
            }

            if (otherUrl != null && !otherUrl.isBlank()) {
                String other = scrapeUniversityMission(otherUrl, null);
                System.out.println(BOLD + "[Other University Mission]" + RESET);
                System.out.println(snippet(other, 700));
                System.out.println();
            }

            if (doJobs) {
                List<String[]> rows = scrapeFakeJobs(); // data rows only (no header)
                rows = processJobs(rows, unique, sinceDays, keyword, sortDesc); // NEW
                writeCsv(outPath, rows); // writes header + rows
                System.out.println(BOLD + ("Wrote " + rows.size() + " rows to " + outPath.toAbsolutePath()) + RESET);
            }

            if (!doXula && !doHoward && otherUrl == null && !doJobs) {
                System.out.println("Usage:");
                System.out.println("  java Scraper --xula");
                System.out.println("  java Scraper --howard");
                System.out.println("  java Scraper --other-url=https://example.edu/mission");
                System.out.println(
                        "  java Scraper --jobs [--out=fake_jobs.csv] [--unique] [--since-days=30] [--keyword=engineer] [--sort-desc]");
                System.out.println();
                System.out.println("Examples:");
                System.out.println("  java Scraper --xula --howard");
                System.out.println(
                        "  java Scraper --jobs --out=fake_jobs.csv --unique --since-days=30 --keyword=engineer --sort-desc");
            }
        } catch (IOException e) {
            System.err.println("Network or I/O error: " + e.getMessage());
        } catch (UncheckedIOException e) {
            System.err.println("Write error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e);
        }
    }

    // ---------- Utils ----------
    static Document fetch(String url) throws IOException {
        return Jsoup
                .connect(url)
                .userAgent("Mozilla/5.0 (Job_Search Student Project)")
                .timeout((int) Duration.ofSeconds(20).toMillis())
                .get();
    }

    static String clean(String s) {
        if (s == null)
            return "";
        return s.replaceAll("\\s+", " ").trim();
    }

    static String snippet(String s, int max) {
        return (s.length() > max) ? s.substring(0, max) + "…" : s;
    }

    // ---------- Scrapers (clean output) ----------
    static String scrapeXulaMission() throws IOException {
        String url = "https://www.xula.edu/about/mission-values.html";
        Document doc = fetch(url);

        Element container = doc.selectFirst("div.editorarea, .editorarea, main .field--name-body, main article");
        String text = (container != null) ? clean(container.text())
                : extractMissionSection(doc);

        if (text == null || text.isBlank())
            text = clean(doc.text());

        // trim spillover + polish
        text = cutAtStopMarkers(text, "Values", "Vision", "Goals", "Learn More", "Section Menu", "Back to Home");
        text = removeNavNoise(text);
        text = dedupeSentences(text);

        return text;
    }

    static String scrapeHowardMission() throws IOException {
        String url = "https://howard.edu/about/mission";
        Document doc = fetch(url);

        String page = clean(doc.text());
        String startMarker = "Howard University, a culturally diverse";
        String endMarker = "global community.";

        int start = page.indexOf(startMarker);
        if (start >= 0) {
            int end = page.indexOf(endMarker, start);
            if (end > 0) {
                String exact = page.substring(start, end + endMarker.length()).trim();
                return exact;
            }
        }

        Element container = doc.selectFirst(
                "main .field--name-body, main article, main .rich-text, main .wysiwyg, .compartment .rich-text, .compartment");
        String text = (container != null) ? clean(container.text()) : extractMissionSection(doc);
        if (text == null || text.isBlank())
            text = page;

        text = safeCut(text, "Core Values", "Learn More", "Section Menu", "Back to Home", "About", "History", "Vision");
        text = removeNavNoise(text);
        text = dedupeSentences(text);

        start = text.indexOf(startMarker);
        int end = text.indexOf(endMarker, Math.max(0, start));
        if (start >= 0 && end > 0) {
            return text.substring(start, end + endMarker.length()).trim();
        }

        return text;
    }

    // ---------- Fake Jobs + CSV ----------
    static List<String[]> scrapeFakeJobs() throws IOException {
        String url = "https://realpython.github.io/fake-jobs/";
        Document doc = fetch(url);

        List<String[]> rows = new ArrayList<>();
        for (Element card : doc.select("div.card-content")) {
            Element titleEl = card.selectFirst("h2.title");
            Element companyEl = card.selectFirst("h3.subtitle");
            Element locationEl = card.selectFirst("p.location");
            Element dateEl = card.selectFirst("time");

            String title = titleEl != null ? clean(titleEl.text()) : "";
            String company = companyEl != null ? clean(companyEl.text()) : "";
            String location = locationEl != null ? clean(locationEl.text()) : "";
            String date = "";
            if (dateEl != null) {
                String dt = dateEl.hasAttr("datetime") ? dateEl.attr("datetime") : dateEl.text();
                date = clean(dt);
            }

            if (!title.isEmpty() && !company.isEmpty()) {
                rows.add(new String[] { title, company, location, date });
            }
        }
        return rows;
    }

    static void writeCsv(Path out, List<String[]> rows) {
        try {
            Path parent = out.getParent();
            if (parent != null)
                Files.createDirectories(parent);
        } catch (IOException ignored) {
        }

        try (var bw = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {

            bw.write(csvLine(new String[] { "Job Title", "Company", "Location", "Date Posted" }));
            bw.newLine();
            for (String[] r : rows) {
                bw.write(csvLine(r));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String csvLine(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(escapeCsv(fields[i]));
        }
        return sb.toString();
    }

    static String escapeCsv(String s) {
        if (s == null)
            return "";
        boolean needsQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String t = s.replace("\"", "\"\"");
        return needsQuote ? "\"" + t + "\"" : t;
    }

    // ---------- Helpers (define once; used by all scrapers) ----------

    static String extractMissionSection(Document doc) {
        Element head = doc.selectFirst(
                "h1:matchesOwn((?i)mission), h2:matchesOwn((?i)mission), h3:matchesOwn((?i)mission)");
        if (head == null)
            return null;

        StringBuilder sb = new StringBuilder();
        for (Element sib = head.nextElementSibling(); sib != null; sib = sib.nextElementSibling()) {
            String tag = sib.tagName().toLowerCase();
            if (tag.matches("h[1-6]"))
                break;
            if (tag.equals("p") || tag.equals("div") || tag.equals("section") || tag.equals("article")) {
                sb.append(' ').append(sib.text());
            }
        }
        return clean(sb.toString());
    }

    static String cutAtStopMarkers(String s, String... markers) {
        int cut = s.length();
        for (String m : markers) {
            int i = s.indexOf(m);
            if (i >= 0 && i < cut)
                cut = i;
        }
        return s.substring(0, cut).trim();
    }

    static String removeNavNoise(String s) {
        return s
                .replaceAll("(?i)Back to Home\\s*", "")
                .replaceAll("(?i)Section Menu.*?(?=Mission|$)", "")
                .replaceAll("(?i)Close Section Menu\\s*", "")
                .replaceAll("(?i)Learn More\\s*", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    static String dedupeSentences(String s) {
        String[] parts = s.split("(?<=[.!?])\\s+");
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        StringBuilder out = new StringBuilder();
        for (String p : parts) {
            String t = p.trim();
            if (t.isEmpty())
                continue;
            if (seen.add(t)) {
                if (out.length() > 0)
                    out.append(' ');
                out.append(t);
            }
        }
        return out.toString();
    }

    static String safeCut(String s, String... markers) {
        if (s == null)
            return "";
        int cut = s.length();
        for (String m : markers) {
            int i = s.indexOf(m);
            if (i >= 0 && i < cut)
                cut = i;
        }
        String trimmed = s.substring(0, cut).trim();
        return trimmed.length() >= 140 ? trimmed : s;
    }

    static String scrapeUniversityMission(String url, String containerSelector) throws IOException {
        Document doc = fetch(url);

        if (containerSelector != null && !containerSelector.isBlank()) {
            Element node = doc.selectFirst(containerSelector);
            if (node != null)
                return clean(node.text());
        }

        String text = extractMissionSection(doc);

        if (text == null || text.isBlank())
            text = clean(doc.text());

        text = safeCut(text, "Core Values", "Values", "Vision", "Learn More", "Section Menu", "Back to Home");
        text = removeNavNoise(text);
        text = dedupeSentences(text);
        return text;
    }

    static List<String[]> processJobs(
            List<String[]> rows, boolean unique, Integer sinceDays, String keyword, boolean sortDesc) {

        // rows: [title, company, location, datePosted]
        List<String[]> out = new ArrayList<>(rows);

        // (a) keyword filter (in title/company/location, case-insensitive)
        if (keyword != null && !keyword.isBlank()) {
            String needle = keyword.toLowerCase();
            out.removeIf(r -> {
                String t = (r[0] == null ? "" : r[0]).toLowerCase();
                String c = (r[1] == null ? "" : r[1]).toLowerCase();
                String l = (r[2] == null ? "" : r[2]).toLowerCase();
                return !(t.contains(needle) || c.contains(needle) || l.contains(needle));
            });
        }

        // (b) date filter (keep last N days)
        if (sinceDays != null && sinceDays > 0) {
            java.time.LocalDate cutoff = java.time.LocalDate.now().minusDays(sinceDays);
            out.removeIf(r -> {
                java.time.LocalDate d = parseDateSafe(r[3]);
                return d == null || d.isBefore(cutoff);
            });
        }

        // (c) dedupe on Title+Company+Location
        if (unique) {
            java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>();
            List<String[]> uniq = new ArrayList<>();
            for (String[] r : out) {
                String key = (r[0] + "||" + r[1] + "||" + r[2]).toLowerCase();
                if (seen.add(key))
                    uniq.add(r);
            }
            out = uniq;
        }

        // (d) sort by date (desc = newest first)
        if (sortDesc) {
            out.sort((a, b) -> {
                java.time.LocalDate da = parseDateSafe(a[3]);
                java.time.LocalDate db = parseDateSafe(b[3]);
                if (da == null && db == null)
                    return 0;
                if (da == null)
                    return 1;
                if (db == null)
                    return -1;
                return db.compareTo(da);
            });
        }

        return out;
    }

    // Parse ISO-like dates safely (e.g., "2020-07-08"); returns null if unknown
    static java.time.LocalDate parseDateSafe(String s) {
        if (s == null || s.isBlank())
            return null;
        try {
            // Most fake jobs use ISO-8601 (yyyy-MM-dd). If not, add more parsers.
            return java.time.LocalDate.parse(s);
        } catch (Exception ignored) {
            // Try a looser parse if needed (e.g., "July 8, 2020")
            try {
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MMMM d, uuuu");
                return java.time.LocalDate.parse(s, fmt);
            } catch (Exception ignored2) {
                return null;
            }
        }
    }

}
