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

**Next target:**
- Finish 10E, start implementing ECL.

**Architecture:**
- Backend: Java + Spring Boot.
- Frontend: Angular.
- Networking: websocket (broadcasting whole board state at every update).
- Most of the card data is downloaded from Scryfall at server startup (for legal reasons).
- Card art and set symbols are loaded from Scryfall by the client at startup (for legal reasons).
- Mana symbols and watermarks are drawn with the self-hosted Mana font, so they need no fetching at all.

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

Every card in this app is typeset with three open fonts, all self-hosted from `magical-vibes-frontend/public/fonts/` and all licensed under the SIL Open Font License 1.1 (the licence text ships beside each one):

- [Cinzel](https://github.com/NDISCOVER/Cinzel) — card names and headings.
- [Crimson Text](https://github.com/googlefonts/Crimson) — rules text and flavour text.
- [Mana](https://github.com/andrewgioia/mana) by Andrew Gioia — every mana, tap and watermark symbol on a card. Its stylesheet is MIT licensed and is vendored in `magical-vibes-frontend/src/mana.css`; the symbols the glyphs depict are copyright Wizards of the Coast.

Card data, art and set symbols come from [Scryfall](https://scryfall.com/).