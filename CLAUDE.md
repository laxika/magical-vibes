# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Do no verification. All Java & frontend verification will be done by me after you finish with the coding/implementation.

## Build & Test Commands

```bash
# Build entire project (all modules including Angular frontend)
./gradlew build

# Build backend only (skips frontend)
./gradlew :magical-vibes-backend:build

# Run all backend tests
./gradlew :magical-vibes-backend:test

# Run a single test class
./gradlew :magical-vibes-backend:test --tests "com.github.laxika.magicalvibes.cards.h.HonorGuardTest"

# Run a single test method
./gradlew :magical-vibes-backend:test --tests "com.github.laxika.magicalvibes.cards.h.HonorGuardTest.hasCorrectProperties"

# Run the Spring Boot application
./gradlew :magical-vibes-backend:bootRun

# Frontend (from magical-vibes-frontend/)
npm start        # dev server
npm run build    # production build
npm test         # run tests
```

## Architecture

Gradle multi-module project: a collectible card game with WebSocket-based real-time multiplayer.

### Module Dependency Graph

```
backend → card → domain
       → websocket → networking → domain
```

- **`magical-vibes-domain`** — Core domain model: `Card`, `Permanent`, `GameData`, `StackEntry`, `ManaPool`, `ManaCost`, enums (`CardType`, `CardColor`, `CardSubtype`, `Keyword`, `TurnStep`), and all `CardEffect` records (in `model/effect/`).
- **`magical-vibes-networking`** — Wire protocol: WebSocket message records (in `message/`), view DTOs (`CardView`, `PermanentView`, `StackEntryView`), and their factory services (`CardViewFactory`, `PermanentViewFactory`, `StackEntryViewFactory`). The `SessionManager` interface and `Connection` interface live here too.
- **`magical-vibes-card`** — Card definitions: each card is a `Card` subclass (organized in alphabetical subpackages like `cards/a/`, `cards/b/`). `CardSet` enum registers all cards with their set code, collector number, and factory. `CardPrinting` stamps `setCode`/`collectorNumber`/`flavorText` onto cards.
- **`magical-vibes-websocket`** — WebSocket infrastructure: `WebSocketSessionManager` (implements `SessionManager`), `WebSocketHandler`, Spring config. Depends on networking for the `SessionManager` interface.
- **`magical-vibes-backend`** — Spring Boot application. `GameMessageHandler` dispatches WebSocket messages to `LoginService`, `LobbyService`, or `GameService`. `GameService` is the game engine (~1700 lines): turn progression, combat, stack resolution, effect dispatch.
- **`magical-vibes-frontend`** — Angular standalone components. `websocket.service.ts` defines all TypeScript interfaces (`Card`, `Permanent`, `Game`, `StackEntry`) and handles WebSocket communication. `game.component.ts` is the main game UI.

### Key Patterns

- **Jackson 3.x**: Import `tools.jackson.databind.ObjectMapper` (not `com.fasterxml`).
- **Domain model is mutable, views are immutable**: `Card` uses `@Setter` with defaults; `Permanent` has mutable state. View records (`CardView`, `PermanentView`) are created by factory services for serialization — never mutate domain objects for serialization purposes.
- **Effect system**: Effects are records implementing the marker interface `CardEffect`. `GameService.resolveStackEntry()` dispatches each effect to a `resolve*` method via `instanceof` pattern matching.
- **Two types of activated abilities on cards**: `tapActivatedAbilityEffects`/`tapActivatedAbilityCost` (requires tapping) vs `manaActivatedAbilityEffects`/`manaActivatedAbilityCost` (mana cost only, no tap). `onTapEffects` is separate — used for land mana production.
- **Static/continuous effects**: Computed on-the-fly via `computeStaticBonus()` and baked into `PermanentView`, never stored on the `Permanent`.
- **Thread safety**: `GameData` uses `ConcurrentHashMap` + `synchronized(gameData)` blocks in `GameService`. Validation checks must go INSIDE synchronized blocks.
- **Frontend signals**: `game = signal<Game | null>(null)`, updated via spread + `game.set()`. RxJS Subjects for WebSocket messages.

### Adding a New Card

1. Create card class in `magical-vibes-card/src/main/java/.../cards/{letter}/CardName.java` extending `Card`.
2. Constructor: call `super(name, CardType, manaCost, CardColor)`, blank line, then setter calls.
3. Register in `CardSet` with a `new CardPrinting("SET", "collectorNumber", CardName::new)` (optionally with flavor text).
4. If the card introduces a new effect, create a record in `magical-vibes-domain/.../model/effect/` implementing `CardEffect`, then add resolution logic in `GameService.resolveStackEntry()`.
5. Update `CardView`/`CardViewFactory` if the new effect requires a new boolean flag on the view.
6. Update the frontend `Card` interface in `websocket.service.ts` to match.

### Testing Cards

Tests live in `magical-vibes-backend/src/test/java/.../cards/{letter}/CardNameTest.java`. Use `GameTestHarness` which sets up a full two-player game with fake connections. Key harness methods: `skipMulligan()`, `setHand()`, `addMana()`, `addToBattlefield()`, `castCreature()`, `castInstant()`, `activateAbility()`, `passBothPriorities()`, `forceStep()`, `forceActivePlayer()`. Tests use JUnit 5 + AssertJ.
