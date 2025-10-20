# Job_Search

Small educational project to practice web scraping, text extraction, and data handling based on assignment requirements.  
This project demonstrates scraping university mission statements, extracting and processing job listings, and applying optional data enhancement features for bonus credit.

---

## What the Application Does (Assignment Tasks)

1. Prints an ASCII welcome banner and describes the application's purpose. (TODO 3–4)  
2. Scrapes university mission statements:
   - **Xavier University of Louisiana (XULA)** from `https://www.xula.edu/about/mission-values.html` (TODO 6)
   - **Another university** (e.g., Howard University), extracted using substring-based text matching for clean mission accuracy (TODO 7)
3. Scrapes a demo job board from the RealPython fake jobs page and writes results to `fake_jobs.csv` using the required columns:  
   `Job Title, Company, Location, Date Posted` (TODO 8–9).
4. Implements multiple optional bonus features for enhanced scraping and data usability.

## Bonus Feature Documentation 
The following bonus features were implemented to extend job scraping functionality and simulate real-world job filtering behavior:
1. Duplicate Removal (--unique)
This removes repeated job listings with identical title, company, and location values.
It was implemented using a LinkedHashSet to preserve first occurrences while ensuring uniqueness.
2. Date-Based Filtering (--since-days=N)
Allows the user to specify how many days back job listings should be included.
Dates are converted to LocalDate objects and compared against the current date.
Note: The demo site uses older dates (e.g., from 2020), so using a small window like --since-days=30 will result in no remaining listings.
3. Keyword Filtering (--keyword=WORD)
Filters listings that contain a user-specified keyword within the job title, company, or location.
The match is case-insensitive.
4. Sorting by Date Descending (--sort-desc)
Sorts job entries so the most recent postings appear first.
Implemented using LocalDate parsing and a custom comparator.

---

## Requirements

- Java 17 or higher
- [jsoup 1.18.1](https://jsoup.org/)
- Internet connection required
- `jsoup-1.18.1.jar` must be located next to `Scraper.java` or inside a `lib/` folder and included in the classpath during compilation and execution

---

## How to Compile & Run

### macOS/Linux
```bash
javac -cp .:jsoup-1.18.1.jar Scraper.java
java  -cp .:jsoup-1.18.1.jar Scraper --xula --howard
java  -cp .:jsoup-1.18.1.jar Scraper --jobs --out=fake_jobs.csv

Bonus : java -cp .:jsoup-1.18.1.jar Scraper --jobs --out=fake_jobs.csv --unique --keyword=engineer --sort-desc