An experimental online Magic game engine. The goal is to show that modern agents could write okay quality code en-masse with good human supervision. **99.99% of the code in this repo was written by either Claude or Codex.**

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
- All the art assets are loaded from Scryfall by the client at startup (for legal reasons).

**How to start the application:**
- Run `./gradlew clean build`
- `cd` to `magical-vibes-backend/build/libs/`
- Run `java -jar magical-vibes-backend-1.0.0-SNAPSHOT.jar`
- Open a new terminal, go to `magical-vibes-frontend`
- Run `npm install` (you need npm for this).
- Run `ng serve` (you need to have the angular cli tool installed.
- Open `http://localhost:4200/` and log in (there are 3 users for now, credentials are under the login screen).

If you get stuck then ask Gemini. :) Or feel free to create an issue on GitHub.