An experimental online Magic game engine. The goal is to show that modern agents could write okay quality code en-masse with proper human supervision. **99.99% of the code in this repo was written by either Claude or Codex.**

**Why Magic?**
- The rules are extremely well-defined.
- It is easy to verify objectively if the app is working as intended (does the cards do what is written on them?).
- It is super complex so if agents can work with it, then they can work with almost anything else as well.

**What the engine supports:**
- 10E (Tenth Edition) 80% coded.
- 1v1 matches against human players.
- 1v1 matches against AI (an easy, heuristic based one).
- 8 player drafts against other humans or AI.

**Architecture:**
- Backend: Java + Spring Boot.
- Frontend: Angular.
- Networking: websocket (broadcasting whole board state at every update).
- Card data is downloaded from Scryfall/MTGJson at server startup.
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

If you get stuck then ask Gemini. :) Or feel free to create an issue on GitHub.

**How to run fizz tests from the command line:**
- Card fuzzing: `.\gradlew :magical-vibes-ai:test --tests "com.github.laxika.magicalvibes.ai.RandomAiFuzzTest" -DrunCardFuzz=true -DfuzzGames=5 -Dorg.gradle.jvmargs="-Xmx6g" --info --rerun  > fuzz.log 2>&1`
- Scenario fuzzing (loops every printing until failure): `.\gradlew :magical-vibes-ai:test --tests "com.github.laxika.magicalvibes.ai.SingleCardScenarioFuzzTest" -DrunScenarioFuzz=true -Dorg.gradle.jvmargs="-Xmx6g" --info --rerun  > scenario-fuzz.log 2>&1`

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