- Do not start committing changes until I tell you to do so.
- You should always work on the main branch!
- Rules accuracy is the number one priority — never ship rules-incorrect engine behavior. If a card's behavior is at all ambiguous, search the web for the official ruling.
- Reuse over creation: build effects by combining existing ones (e.g. "2 damage to any target and 3 to you" = `DealDamageToAnyTargetEffect` + `DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER)`) and parameterize with predicates (`DestroyTargetPermanentEffect` + a `PermanentPredicate`, not `DestroyTargetArtifactEffect`) rather than adding new classes. When you do add a new effect/predicate, update the relevant `agent-docs/` files.
- If unit tests exist for a service you extend, add tests for the new behavior too.
- Do not ever run the full test suite! Ask me and I'll run it for you.

## Implementing cards

Use the **`implement-card`** skill (`/implement-card <SET> <COLLECTOR_NUMBER>`). It owns the full workflow — reprint check, Scryfall lookup, mapping oracle text to effects via `agent-docs/` (grep, never read in full), writing the card class, and writing focused tests. Reprints just get a new `@CardRegistration` annotation and nothing else.

## Architecture

Gradle multi-module project: a collectible card game with WebSocket-based real-time multiplayer.

### Module Dependency Graph

```
application → webservice → {ai, engine, websocket} → {card, networking, card-data} → domain
              ai → engine
              ai → websocket → networking
application → engine, websocket, ai (config @Imports)
```

- **`magical-vibes-domain`** — Core domain model: `Card`, `Permanent`, `GameData`, `StackEntry`, `ManaPool`, `ManaCost`, enums (`CardType`, `CardColor`, `CardSubtype`, `Keyword`, `TurnStep`), and all `CardEffect` records (in `model/effect/`).
- **`magical-vibes-networking`** — Wire protocol: WebSocket message records (in `message/`), view DTOs (`CardView`, `PermanentView`, `StackEntryView`), and their factory services (`CardViewFactory`, `PermanentViewFactory`, `StackEntryViewFactory`). The `SessionManager` interface and `Connection` interface live here too.
- **`magical-vibes-card`** — Card definitions: each card is a `Card` subclass (organized in alphabetical subpackages like `cards/a/`, `cards/b/`), annotated with `@CardRegistration(set, collectorNumber)`. `CardSet` enum discovers printings at runtime via `CardScanner` (ClassGraph classpath scan). `CardPrinting` stamps `setCode`/`collectorNumber`/`flavorText` onto cards.
- **`magical-vibes-card-data`** — Oracle data loading (`com.github.laxika.magicalvibes.carddata`): `CardDataConfiguration` plus per-provider subpackages — `carddata.scryfall` (`ScryfallOracleLoader`, `ScryfallDataService`, `ScryfallTypeLineParser`) and `carddata.mtgjson` (`MtgjsonOracleLoader`). Populates `Card.oracleRegistry` on startup. The `oracle.data-provider` property (SCRYFALL/MTGJSON) picks the source; with SCRYFALL, MTGJSON is the automatic fallback when Scryfall is unreachable. Both loaders fill the same registries (oracle data, set names, rarity, token images).
- **`magical-vibes-engine`** — The game engine and its Spring wiring (`GameEngineConfig`, `JacksonConfig`). `GameService` (~1700 lines) is the protocol-agnostic game-action API: turn progression, combat, stack resolution, effect dispatch, plus actions like `playCard`/`activateAbility`/`declareAttackers`. `GameSetupService` seats players and runs the opening sequence (custom decks resolved via the optional `CustomDeckSource` interface). The shared card/AI test harness (`GameTestHarness`, `BaseCardTest`, …) lives in this module's `src/testFixtures`.
- **`magical-vibes-websocket`** — WebSocket infrastructure: `WebSocketSessionManager` (implements `SessionManager`), `WebSocketHandler`, Spring config. Depends on networking for the `SessionManager` interface.
- **`magical-vibes-ai`** — Computer opponents. `AiDecisionEngine` (+ `EasyAiDecisionEngine`/`MediumAiDecisionEngine`/`HardAiDecisionEngine`) plays by calling the engine's `GameService` via the `AiGameActions` adapter; `AiPlayerService` seats an AI via `GameSetupService`. Headless MCTS simulation lives in `ai/simulation`. Wired into the app via `AiConfig`.
- **`magical-vibes-webservice`** — Web service layer. `GameMessageHandler` (the `MessageHandler` impl) adapts WebSocket wire messages to engine `GameService` calls (and routes login/lobby/draft/deck to `LoginService`/`LobbyService`/`DraftService`/`DeckService`). `LobbyService` builds lobby views and delegates seating to the engine's `GameSetupService`. Owns the JPA entities/repositories (`User`/`Deck`), the SPA forwarding controller (`SpaController`), and its own `WebServiceConfig` (component-scans `webservice` + `handler`).
- **`magical-vibes-application`** — Spring Boot application (composition root only). Holds just `MagicalVibesApplication` (in its own `application` package so the default component scan picks up nothing else), which `@Import`s each module's config (`GameEngineConfig`, `WebSocketConfiguration`, `AiConfig`, `WebServiceConfig`) and declares `@EntityScan`/`@EnableJpaRepositories` for the webservice module's persistence packages. Owns the runtime DB wiring (Liquibase changelogs, SQLite driver, Hibernate dialect) and `application.properties`. The card/engine unit tests live in this module's `src/test`.
- **`magical-vibes-frontend`** — Angular standalone components. `websocket.service.ts` defines all TypeScript interfaces (`Card`, `Permanent`, `Game`, `StackEntry`) and handles WebSocket communication. `game.component.ts` is the main game UI.

### Key Patterns

- **Jackson 3.x**: Import `tools.jackson.databind.ObjectMapper` (not `com.fasterxml`).
- **Domain model is mutable, views are immutable**: `Permanent` has mutable state. View records (`CardView`, `PermanentView`) are created by factory services for serialization — never mutate domain objects for serialization purposes.
- **Cards freeze once live**: `Card` objects are shared between the real game and AI simulation copies. They are mutable during construction/assembly but freeze (every mutator throws) once they join a game — deck stamping, `Permanent` or `StackEntry` creation. Runtime state belongs on the `Permanent`, the `StackEntry`, or `GameData` (e.g. imprint: `gameData.get/setImprintedCard`); modal casts mutate a `Card.createRuntimeCopy()`. Enforced by `Card.freeze()` at runtime and `CardImmutabilityArchTest` at build time; tests that tweak a wrapped card's stats use `TestCards.mutableCard(permanent)`. When adding a field to `Card`, also copy it in `Card(Card source)` and bump the count in `CardFreezeTest`.
- **Effect system**: Effects are records implementing the marker interface `CardEffect`. `GameService.resolveStackEntry()` dispatches each effect to a `resolve*` method via `instanceof` pattern matching.
- **Static/continuous effects**: Computed on-the-fly by the CR 613 layered pass — `LayerSystemService` applies every continuous effect (static slots + floating effects) in layer, timestamp, and dependency order, and `GameQueryService.computeStaticBonus()` assembles the result (see `agent-docs/LAYER_SYSTEM.md`) — then baked into `PermanentView`, never stored on the `Permanent`. The finished board state is memoized per `GameData` behind a structural fingerprint (LAYER_SYSTEM.md §10) — if the layered pass starts reading a new game-state input, extend `LayerSystemService.computeBoardFingerprint` to cover it.
- **Thread safety**: `GameData` uses `ConcurrentHashMap` + `synchronized(gameData)` blocks in `GameService`. Validation checks must go INSIDE synchronized blocks.
- **Frontend signals**: `game = signal<Game | null>(null)`, updated via spread + `game.set()`. RxJS Subjects for WebSocket messages.

### Scryfall Oracle Data

All card metadata (name, type, mana cost, color, supertypes, subtypes, card text, power/toughness, keywords) comes from Scryfall via `ScryfallOracleLoader`. On startup, `ScryfallDataService` (`@PostConstruct`) fetches oracle data for each set in `CardSet`, caches it to `./scryfall-cache/`, and populates `Card.oracleRegistry`. If Scryfall is unreachable (or `oracle.data-provider=MTGJSON` is set), `MtgjsonOracleLoader` loads the same data from MTGJSON set files instead (cached as `mtgjson-{set}.json` in the same directory). Card subclass constructors contain only game-engine logic (effects, abilities, targeting) — metadata is auto-populated from the registry via `Card`'s no-arg constructor. Tests load the registry via `GameTestHarness` (first run requires network; subsequent runs use disk cache).

### Testing

Card tests live in `magical-vibes-application/src/test/java/.../cards/{letter}/CardNameTest.java` and extend `BaseCardTest` (JUnit 5 + AssertJ). The harness (`GameTestHarness`, `BaseCardTest`, `FakeConnection`, `TestGameRegistry`, `TestWebSocketSessionManager`, `GameTestDoublesConfig`) lives in `magical-vibes-engine/src/testFixtures` and is shared with the AI suite via `testFixtures(project(":magical-vibes-engine"))`; AI tests live in `magical-vibes-ai/src/test/.../ai/`. Never assert Scryfall-loaded metadata (name, type, mana, color, P/T, subtypes, keywords) — test only engine logic (effects, abilities, targeting, interactions). Do NOT write white-box "wiring" tests (commonly named `hasCorrectProperties`) that inspect a freshly-constructed card's `getEffects(...)`, `EffectSlot`, `EffectResolution.needsTarget`, or effect field values by reflection — they duplicate what the behavioral tests already prove and break on every effect refactor. Assert behavior by resolving the card through the engine (`harness`/`gs`/`gd`) instead. The `implement-card` skill covers the card-authoring and test-writing workflow in detail.
