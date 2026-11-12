# ARCHITECTURE

The module map and the invariants every engine change must respect. Read this file whole before
changing anything beyond a card class and its test. Then read the topic file for the area you are
changing (table below). Do not read the other topic files.

Gradle multi-module project: a collectible card game with WebSocket-based real-time multiplayer.

## Topic files

Each holds the invariants of one area as short entries (rule, owner, why). The mechanism is in the
Javadoc of the owner class.

| Changing | Read |
|---|---|
| Casting paths, costs, stack object identity, spell copies, what a stack entry retains | [architecture/casting-stack.md](architecture/casting-stack.md) |
| A new effect or handler, prompts, parked resolutions, the resolution scratchpad | [architecture/resolution.md](architecture/resolution.md) |
| Replacement and prevention, damage, death replacements, losing the game, token creation | [architecture/replacement-damage.md](architecture/replacement-damage.md) |
| How permanents enter the battlefield | [architecture/entry.md](architecture/entry.md) |
| Triggered abilities, rules events, what a stack entry records about its trigger | [architecture/triggers.md](architecture/triggers.md): 137 KB, grep it, never read it whole |
| Turn, dungeon, suspend, face-down exile state; domain events | [architecture/game-state.md](architecture/game-state.md), [DOMAIN_EVENTS.md](DOMAIN_EVENTS.md) |
| Card metadata loading, the card and card-data modules | [architecture/oracle-data.md](architecture/oracle-data.md) |
| Static and continuous effects | [LAYER_SYSTEM.md](LAYER_SYSTEM.md) |
| Nested games | [SUBGAMES.md](SUBGAMES.md) |
| Planechase | [PLANECHASE.md](PLANECHASE.md) |

**Keeping these short.** An entry is a title and at most three lines: the rule, its owner, why.
How the code does it goes in the owner's Javadoc. When a change makes an entry wrong, replace the
entry; do not append a paragraph. No card lists, no history, no references to plans or their steps.

## Modules

```
application → webservice → {ai, engine, websocket} → {card, networking, card-data} → domain
              ai → engine
              websocket → networking
application → engine, websocket, ai (config @Imports)
```

- **`magical-vibes-domain`**: the domain model (`Card`, `Permanent`, `GameData`, `StackEntry`,
  `ManaPool`, `ManaCost`, the enums) and every `CardEffect` record (`model/effect/`).
- **`magical-vibes-networking`**: WebSocket message records (`message/`), view DTOs (`CardView`,
  `PermanentView`, `StackEntryView`) and their factories, and the `SessionManager` and
  `Connection` interfaces. The engine's `GameViewProjectionFactory` composes the factories into
  player-specific views.
- **`magical-vibes-card`**: an umbrella over `:core` (infrastructure), `:a` … `:z` (cards by first
  letter) and `:shared` (cards constructed by a card of another letter). Letter modules never
  depend on each other. Each card is a `Card` subclass annotated `@CardRegistration`.
- **`magical-vibes-card-test`**: card tests only, one module per letter. Engine, service, layer
  and architecture tests are in `magical-vibes-application`.
- **`magical-vibes-card-data`**: loads Oracle data. `CardRegistry` scans for printings and
  registers what the one `OracleLoader` returns.
- **`magical-vibes-engine`**: the game engine and its Spring wiring (`GameEngineConfig`).
  `GameService` is the protocol-agnostic game-action API. `GameSetupService` seats players. The
  shared test harness (`GameTestHarness`, `BaseCardTest`) is in `src/testFixtures`.
- **`magical-vibes-websocket`**: `WebSocketSessionManager`, `WebSocketHandler`.
  `WebSocketConnection` turns typed outbound messages into JSON as it writes them.
- **`magical-vibes-ai`**: computer opponents. `AiDecisionEngine` reads live `GameData` and
  `AiGameActions` calls `GameService`; an AI seat is never a transport consumer. MCTS simulation
  is in `ai/simulation`.
- **`magical-vibes-webservice`**: `GameMessageHandler` adapts wire messages to `GameService`
  calls and routes login, lobby, draft and deck messages. Owns the JPA entities and repositories.
- **`magical-vibes-application`**: the Spring Boot composition root (`MagicalVibesApplication`
  imports each module's config) and the runtime DB wiring. Engine and service unit tests live in
  its `src/test`.
- **`magical-vibes-frontend`**: Angular standalone components. `websocket.service.ts` holds the
  TypeScript interfaces and the WebSocket communication; `game.component.ts` is the game UI.

## Invariants for every change

### Cards freeze once live
A `Card` is shared between the real game and AI simulation copies, so every mutator throws once it joins a game. Runtime state goes on the `Permanent`, the `StackEntry` or `GameData`; a modal cast mutates `Card.createRuntimeCopy()`.
Owner: `Card.freeze()`, `CardImmutabilityArchTest`.
A new `Card` field is also copied in `Card(Card source)` and counted in `CardFreezeTest`. Tests use `TestCards.mutableCard(permanent)`.

### Continuous effects are computed, never stored
Static and floating continuous effects are applied by the CR 613 layered pass and baked into `PermanentView`; nothing writes the result to the `Permanent`.
Owner: `LayerSystemService`, `GameQueryService.computeStaticBonus()`.
The board state is memoized behind a fingerprint: a new game-state input to the layered pass must be added to `LayerSystemService.computeBoardFingerprint`.

### New game state is copied for simulations
New state on `GameData`, `StackEntry` or `Permanent` is copied by the simulation copy, with a deep-copy test case, unless it is derived data that rebuilds itself.
Owner: `GameData.simulationFrameCopy()`, `GameDataDeepCopyTest`.
Why: AI simulations run on copies; shared mutable state leaks a simulated move into the real game.

### Thread safety
`GameData` uses `ConcurrentHashMap` and `synchronized(gameData)` blocks in `GameService`. A validation check goes inside the synchronized block.
Owner: `GameService`.

### Views are immutable; the domain model is not mutated for serialization
View records are built by the factory services. Derive one `CardView` from another with `view.toBuilder().component(x).build()`, never by re-listing the components in `new CardView(...)`.
Owner: `CardViewFactory`, `PermanentViewFactory`.
Why: a re-listed constructor call silently drops the next component added to the record.

### Saturating arithmetic
Power, toughness, damage and life are `int`. Where a value can grow without bound (doubling, multipliers, sums), use `SaturatingMath` so it clamps instead of wrapping.
Owner: `SaturatingMath` (domain `model/`).
A saturated value is no longer exact under later arithmetic; that deviation is accepted.

### Jackson 3
Import `tools.jackson.databind.ObjectMapper`, not `com.fasterxml`.

### Frontend signals
`game = signal<Game | null>(null)`, updated by spread and `game.set()`. WebSocket messages arrive through RxJS Subjects.

## Testing

Card tests are in `magical-vibes-card-test/{letter}/src/test/java/.../cards/{letter}/CardNameTest.java`
(project `:magical-vibes-card-test:{letter}`). They extend `BaseCardTest` and declare every card
class they construct with `@CardUsed`, which preloads those cards' Oracle sets. The harness
(`GameTestHarness`, `BaseCardTest`, `FakeConnection`, `TestGameRegistry`) is in
`magical-vibes-engine/src/testFixtures` and is shared with the AI tests. The testing rules are in
`AGENTS.md`.
