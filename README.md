An experimental online Magic game engine. The goal is to show that modern agents could write okay quality code en-masse with proper human supervision. **99.99% of the code in this repo was written by either Claude or Codex.**

**Why Magic?**
- The rules are extremely well-defined.
- It is easy to verify objectively if the app is working as intended (does the cards do what is written on them?).
- It is super complex so if agents can work with it, then they can work with almost anything else as well.

**What the engine supports:**
- 10E (Tenth Edition) 80% coded.
- 1v1 matches against human players.
- Two-player Commander with 40 starting life, one commander, commander tax, command-zone return choices, and 21 combat damage from one commander.
- Saved decks with main deck, sideboard, and commander selection. The deck builder validates Casual, Standard, Pioneer, Modern, Legacy, Vintage, Pauper, and Commander. Invalid drafts can be saved; game admission enforces the selected format.
- 1v1 matches against AI (an easy, heuristic based one).
- 8 player drafts against other humans or AI.

Commander uses the regular Commander rules for a two-player game, not Duel Commander. Partner/background pairs, companions, multiplayer, random Commander decks, and Commander with Planechase are outside the supported scope. Format legality is stored with each set's card data and is not refreshed automatically (see below); validation displays the date the set was downloaded and rejects unknown legality for tournament formats.

**Architecture:**
- Backend: Java + Spring Boot.
- Frontend: Angular.
- Networking: websocket (broadcasting whole board state at every update).
- Card data is downloaded from Scryfall or MTGJSON at server startup (see below).
- Card art is loaded from Scryfall by the client at startup.
- Mana symbols, watermarks and set symbols are drawn with icon fonts (Mana and Keyrune), so none of them are fetched per card.

**How to start the application:**
- Run `./gradlew clean build`
- `cd` to `magical-vibes-application/build/libs/`
- Run `java -jar magical-vibes-application-1.0.0-SNAPSHOT.jar`
- Open a new terminal, go to `magical-vibes-frontend`
- Run `npm install` (you need npm for this).
- Run `ng serve` (you need to have the angular cli tool installed.
- Open `http://localhost:4200/` and log in (there are 3 users for now, credentials are under the login screen).

**Card data source and cache:**
- The server keeps its own copy of every set: one `set-{code}.json` file per set (e.g. `set-isd.json`) with the cards, rarities, print variants and format legality, in a format of its own rather than Scryfall's or MTGJSON's.
- If a set's file exists, it is used as is. If not, the set is downloaded into a temporary folder from the preferred provider, converted, saved as `set-{code}.json`, and the downloaded provider data is deleted. If the preferred provider can't be reached after a few retries, the other one is used instead; each file records which provider it came from.
- Scryfall is the preferred provider by default. To prefer MTGJSON instead, set `oracle.data-provider=MTGJSON` in `magical-vibes-application/src/main/resources/application.properties`, or pass it at startup: `java -jar magical-vibes-application-1.0.0-SNAPSHOT.jar --oracle.data-provider=MTGJSON`.
- Nothing is refreshed automatically, including format legality. To pick up new card data or a new ban list for a set, delete its `set-{code}.json` and restart the server.
- The files go to `./card-data-cache`, resolved against the directory you start the server from (with the steps above, `magical-vibes-application/build/libs/card-data-cache`). To change the location, set `card-data.cache-dir` in `application.properties` or pass it at startup: `--card-data.cache-dir=/path/to/cache`.
- Gradle test runs prefer MTGJSON and always use the repository-root `card-data-cache` directory. Add `-Doracle.data-provider=SCRYFALL` to prefer Scryfall.

If you get stuck then ask Gemini. :) Or feel free to create an issue on GitHub.

**How to run fuzz tests from the command line:**
- Card fuzzing: `.\gradlew :magical-vibes-ai:test --tests "com.github.laxika.magicalvibes.ai.RandomAiFuzzTest" -DrunCardFuzz=true -DfuzzGames=5 -Dorg.gradle.jvmargs="-Xmx6g" --info --rerun  > fuzz.log 2>&1`
- Scenario fuzzing (loops every printing until failure): `.\gradlew :magical-vibes-ai:test --tests "com.github.laxika.magicalvibes.ai.SingleCardScenarioFuzzTest" -DrunScenarioFuzz=true -Dorg.gradle.jvmargs="-Xmx6g" --info --rerun  > scenario-fuzz.log 2>&1`
- Automated card fuzzing and repair: `.\scripts\run-fuzz-and-fix.ps1` runs 500 games, checks `fuzz.log`, and starts a fresh Codex repair instance if the run fails. A successful repair is committed with a descriptive, Codex-attributed message and pushed to `origin/main`, then fuzzing repeats until a run passes.

**Thanks:**

Every card in this app is typeset with four open fonts.

The two text faces are self-hosted from `magical-vibes-frontend/public/fonts/`, both under the SIL Open Font License 1.1 (the licence text ships beside each one):

- [Cinzel](https://github.com/NDISCOVER/Cinzel) — card names and headings.
- [Crimson Text](https://github.com/googlefonts/Crimson) — rules text and flavour text.

The two symbol fonts, both by [Andrew Gioia](https://www.jsdelivr.com/?query=author%3A%20andrewgioia), are **not** checked into this repo. They are loaded at pinned versions from jsDelivr, declared in `magical-vibes-frontend/src/symbols.css`:

- [Mana](https://github.com/andrewgioia/mana) — every mana, tap and watermark symbol on a card. Font under OFL 1.1, stylesheet MIT.
- [Keyrune](https://github.com/andrewgioia/keyrune) — the expansion symbol of every set. Font under OFL 1.1, glyphs and stylesheet under GPL 3.0.

They are linked rather than vendored so this project does not redistribute them — which for Keyrune in particular keeps a copyleft licence off a repo that has no other reason to carry one. The symbols the glyphs depict are trademarks of Wizards of the Coast, redrawn by their author and used here to identify sets. A set newer than the pinned Keyrune has no glyph and falls back to printing its set code.

Card data and art come from [Scryfall](https://scryfall.com/).
