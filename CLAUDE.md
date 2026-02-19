# CLAUDE.md

- Do not start committing changes until I tell you to do so.
- Make sure that the way we are implementing the cards and the rules is accurate. Having non-rules-correct behavior in the engine is not fine nor acceptable!
- If you need any rules clarification feel free to search the web! Being rules accurate is our number one priority.
- When implementing cards always try to create reusable components (effects, abilities, etc) that could be reused for other cards as well.
- If you can recreate an effect by combining existing ones then do not create a new class for it. For example "Orcish Artillery deals 2 damage to any target and 3 damage to you." could be achieved by combining DealDamageToAnyTargetEffect with DealDamageToControllerEffect.

## Architecture

Gradle multi-module project: a collectible card game with WebSocket-based real-time multiplayer.

### Module Dependency Graph

```
backend → card → domain
       → websocket → networking → domain
```

- **`magical-vibes-domain`** — Core domain model: `Card`, `Permanent`, `GameData`, `StackEntry`, `ManaPool`, `ManaCost`, enums (`CardType`, `CardColor`, `CardSubtype`, `Keyword`, `TurnStep`), and all `CardEffect` records (in `model/effect/`).
- **`magical-vibes-networking`** — Wire protocol: WebSocket message records (in `message/`), view DTOs (`CardView`, `PermanentView`, `StackEntryView`), and their factory services (`CardViewFactory`, `PermanentViewFactory`, `StackEntryViewFactory`). The `SessionManager` interface and `Connection` interface live here too.
- **`magical-vibes-card`** — Card definitions: each card is a `Card` subclass (organized in alphabetical subpackages like `cards/a/`, `cards/b/`), annotated with `@CardRegistration(set, collectorNumber)`. `CardSet` enum discovers printings at runtime via `CardScanner` (ClassGraph classpath scan). `CardPrinting` stamps `setCode`/`collectorNumber`/`flavorText` onto cards.
- **`magical-vibes-websocket`** — WebSocket infrastructure: `WebSocketSessionManager` (implements `SessionManager`), `WebSocketHandler`, Spring config. Depends on networking for the `SessionManager` interface.
- **`magical-vibes-backend`** — Spring Boot application. `GameMessageHandler` dispatches WebSocket messages to `LoginService`, `LobbyService`, or `GameService`. `GameService` is the game engine (~1700 lines): turn progression, combat, stack resolution, effect dispatch.
- **`magical-vibes-frontend`** — Angular standalone components. `websocket.service.ts` defines all TypeScript interfaces (`Card`, `Permanent`, `Game`, `StackEntry`) and handles WebSocket communication. `game.component.ts` is the main game UI.

### Key Patterns

- **Jackson 3.x**: Import `tools.jackson.databind.ObjectMapper` (not `com.fasterxml`).
- **Domain model is mutable, views are immutable**: `Card` uses `@Setter` with defaults; `Permanent` has mutable state. View records (`CardView`, `PermanentView`) are created by factory services for serialization — never mutate domain objects for serialization purposes.
- **Effect system**: Effects are records implementing the marker interface `CardEffect`. `GameService.resolveStackEntry()` dispatches each effect to a `resolve*` method via `instanceof` pattern matching.
- **Static/continuous effects**: Computed on-the-fly via `computeStaticBonus()` and baked into `PermanentView`, never stored on the `Permanent`.
- **Thread safety**: `GameData` uses `ConcurrentHashMap` + `synchronized(gameData)` blocks in `GameService`. Validation checks must go INSIDE synchronized blocks.
- **Frontend signals**: `game = signal<Game | null>(null)`, updated via spread + `game.set()`. RxJS Subjects for WebSocket messages.

### Scryfall Oracle Data

All card metadata (name, type, mana cost, color, supertypes, subtypes, card text, power/toughness, keywords) comes from Scryfall via `ScryfallOracleLoader`. On startup, `ScryfallDataService` (`@PostConstruct`) fetches oracle data for each set in `CardSet`, caches it to `./scryfall-cache/`, and populates `Card.oracleRegistry`. Card subclass constructors contain only game-engine logic (effects, abilities, targeting) — metadata is auto-populated from the registry via `Card`'s no-arg constructor. Tests load the registry via `GameTestHarness` (first run requires network; subsequent runs use disk cache).

### Adding a New Card

1. Create card class in `magical-vibes-card/src/main/java/.../cards/{letter}/CardName.java` extending `Card`.
2. Constructor: only add game-engine logic — `addEffect()`, `addActivatedAbility()`, `setNeedsTarget()`, `setNeedsSpellTarget()`, `setTargetFilter()`. All metadata (name, type, mana cost, color, subtypes, keywords, power/toughness, card text) is loaded automatically from Scryfall. Cards with no special abilities have an empty class body.
3. Add `@CardRegistration(set = "SET", collectorNumber = "collectorNumber")` annotation(s) to the card class. For multiple printings (e.g. basic lands), use multiple annotations.
4. If the card introduces a new effect, create a record in `magical-vibes-domain/.../model/effect/` implementing `CardEffect`, then add resolution logic in `GameService.resolveStackEntry()`.
5. If Scryfall returns subtypes or keywords not yet in our enums (`CardSubtype`, `Keyword`), add the new enum values.
6. Update `CardView`/`CardViewFactory` if the new effect requires a new boolean flag on the view.
7. Update the frontend `Card` interface in `websocket.service.ts` to match.

### Testing Cards

Tests live in `magical-vibes-backend/src/test/java/.../cards/{letter}/CardNameTest.java`. Use `GameTestHarness` which sets up a full two-player game with fake connections. Key harness methods: `skipMulligan()`, `setHand()`, `addMana()`, `addToBattlefield()`, `castCreature()`, `castInstant()`, `activateAbility()`, `passBothPriorities()`, `forceStep()`, `forceActivePlayer()`. Tests use JUnit 5 + AssertJ.
