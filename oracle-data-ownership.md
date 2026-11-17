# Oracle data ownership

A step-by-step plan to stop `Card()` looking up its own oracle data. Card metadata (name, types, mana
cost, colours, P/T, keywords, text) is instead applied to a card by whoever builds it: the catalog for
every printing, the engine for every card a running game creates, and the test harness for the cards a
test puts into a game. The JVM-wide oracle map on `Card` and the resolver hook that lets a constructor
load a set both go away.

To start a step, use the prompt at the bottom with the step number. Steps are meant to be done in
order; each one lists what it depends on.

## Status

| Step | Title | Status |
|---|---|---|
| 1 | Constructors stop writing their own metadata | open |
| 2 | Hand-written oracle data moves out of static blocks | open |
| 3 | `Card.applyOracle` | open |
| 4 | The registry owns the oracle data and printings apply it | open |
| 5 | Cards a running game creates are dressed by the engine | open |
| 6 | The test harness dresses the cards a test puts into a game | open |
| 7 | Remove the constructor lookup and the static registry | open |
| 8 | Docs and the card-authoring guidance | open |

## Why

`Card()` reads `Card.oracleRegistry` (a static `ConcurrentHashMap` keyed by simple class name) and copies
the entry into its fields. In the ON_DEMAND test context, a miss calls the `OracleDataResolver` that
`CardRegistry` installs, which loads a whole set from `card-data-cache`. This exists because every card
is a no-arg `new X()` built from places no dependency injection reaches, and `Card` (in `domain`) cannot
call `CardRegistry` (in `card-data`, above it in the module graph). It costs:

- **JVM-global mutable state.** Two catalogs cannot coexist in one JVM, so a test cannot hold its own
  catalog (the lifecycle `CardCatalog`'s Javadoc wanted). Tests call `Card.clearOracleRegistry()` (11
  sites), and a test in the shared card-test JVM must not clear it at all.
- **A constructor that can do I/O.** `new X()` can load a set. Nobody expects that, and it hid the cost
  of catalog-wide scans (a conjure test took 21.85 s until `CardCatalog.getEntries()` stopped building
  cards to read their data).
- **Lost hand-written data.** 38 card classes register oracle data for printings missing from the feeds
  in a `static {}` block with `registerOracle`; 2 use `registerEmbeddedOracle`, the only variant that
  survives `clearOracleRegistry()`. After a clear, the 38 lose their data for the rest of the JVM,
  because a static block does not run twice.

## Measurements (2026-11-18, at `456da4f2f2`)

- **Card constructors don't read their own metadata.** No card class calls a metadata getter on itself;
  `Card`'s builder methods (`add*`, `with*`) don't read metadata fields either. So applying the data
  after construction instead of before is safe, except for the three writers in step 1.
- **Three constructors write metadata, overriding the oracle data:** `HymnToTheAges` and `LegionsChant`
  (`setSubtypes(CHORUS)`), `Spirit` (`setKeywords(FLYING)`). Applied after construction, the oracle data
  would replace their values.
- **Cards built inside card classes:**
  - 717 back faces (`setBackFaceCard(new X())`, or a `backFace` local passed to it). Each is a plain
    construction; none is mutated beyond set code and collector number.
  - 73 `X::new` suppliers in 15 files, passed to the 11 effect records that take a `Supplier<Card>`
    (conjure, draft, spellbook, random-copy effects).
  - 12 `new X()` instances held by effect records in 7 files (`DwarvenConfluencer`, `GarthOneEye`,
    `JessieZaneFangbringer`, `JuggernautPeddler`, `MapToLorthossTemple`, `TheColossalDreadmaw`,
    `Transcantation`), whose data the engine reads at runtime (a named card, a copy source).
- **Cards built by engine code:** 56 `X::new` in 4 spellbook handlers
  (`DraftSlimefootThallidTransplantSpellbookEffectHandler`, `DraftProteanWarEngineSpellbookEffectHandler`,
  `ConjureGoblinInfluxArraySpellbookEffectHandler`, `ConjureDarigaazShivanChampionSpellbookEffectHandler`),
  and one reflective construction (`MeldWithNamedCreatureEffectHandler.instantiateMeldResult`).
  `CardPrinting.createCard()` has 29 production callers. Every one gets the data through the
  printing's factory once step 4 wraps it.
- **Cards built by tests:** about 536,000 `new X()` sites in 31,353 card-test files, 2,448 in AI tests,
  1,321 in application tests. Almost all of them reach a game through harness methods (`setHand`
  111k calls, `addToBattlefieldAndReturn` 102k, `addToBattlefield` 84k, `setLibrary` 38k,
  `setGraveyard` 25k, …). About 2,500 bypass the harness: 1,114 `new Permanent(new X())` and 1,385
  direct `gd.player….get(id).add(…)` calls. No test chains a metadata getter onto `new X()` directly.
  How many read data off a card before it enters a game can't be counted statically; step 6 finds them.

## Design

**A card is dressed by whoever builds it.** `Card.applyOracle(OracleData)` copies the data into the
fields (the block `Card()` runs today). It is a mutator, so it can only run before the card freezes.
"Dressing" a card means applying its class's data to it and to its back face.

**The registry owns the data.** `CardRegistry` keeps the class → `OracleData` map as instance state,
filled by `register` and by the hand-written data (step 2). Its printings wrap the scanner's factory, so
`CardPrinting.createCard()` returns a dressed card and none of its 29 callers change. `getEntries()`
reads the registry's own map. Back faces are dressed through the back-face class the registry already
records (`backFaceClasses`).

**Runtime-created cards go through the catalog.** `CardCatalog.dress(Card)` applies the registry's data
to a card built outside a printing (a supplier's result, a meld result) and returns it. Where a class's
data isn't loaded yet, it loads the set explicitly. That's the same lazy load ON_DEMAND does today, but
in a catalog call rather than in a constructor. Effect records that hold a built sample card instead
hold its class or a supplier, so the engine builds and dresses it when it needs it.

**Tests dress at the point of entry.** The harness's zone-entry methods dress each card they are given
(in place: the test's own reference sees the data). `@CardUsed` preloading stays as it is. The bypass
sites move to harness helpers. A card a test never puts into a game has no data, which is correct: it
is not a game object.

**A card never joins a game undressed.** Freezing a card whose class has registered printings but no
applied data is an error in tests and a logged warning in production (some printings have no data in
any provider, like the `FEM` printings the loader warns about today).

### Rejected: a scoped oracle source

`Card()` could read the data from a `ScopedValue<OracleSource>` bound by the catalog, the engine and a
JUnit extension instead of a static map. It would remove the global state with almost no test
migration, but the constructor would still look itself up. Every thread that builds a card (game
threads, AI simulation threads, websocket handlers) would need a binding, and a missing binding would
fail silently. Applying data from the outside has no ambient state to forget.

## Ground rules for every step

- Card behaviour must not change: a dressed card carries exactly the data `Card()` gives it today.
  Rules accuracy comes first (AGENTS.md).
- Read `agent-docs/ARCHITECTURE.md` and `agent-docs/architecture/oracle-data.md` before changing
  engine, card-data or domain code.
- A change to `FaceOracleMapper`, `TypeLineParser`, `CardDataSupport`, `StoredSetConverter`, either
  `SetSource`, or a new `CardSubtype` / `Keyword` constant bumps `StoredSet.CONVERSION_VERSION`.
- Run only the tests for what you touched. Never run the full suite; ask for it (steps 6 and 7 need it).
- No commit until asked.

## Steps

### 1. Constructors stop writing their own metadata

Depends on: nothing.

`HymnToTheAges` and `LegionsChant` set the `CHORUS` subtype, and `Spirit` sets `FLYING`, in their
constructors. Find out why each overrides its oracle data (a subtype the type-line parser drops, a
token printing whose feed data lacks the keyword, or something else). Fix the data at its source,
usually the mapper or the parser (with the version bump), and delete the constructor calls. Their card
tests must still pass. Add an architecture test that no card constructor calls a metadata setter.

### 2. Hand-written oracle data moves out of static blocks

Depends on: nothing.

The 40 classes with hand-written data (38 `registerOracle`, 2 `registerEmbeddedOracle`) declare it in
a form the registry discovers instead of a static block that writes the global map. For example, a
`public static final OracleData ORACLE` constant the scanner finds by name, or an annotation naming a
resource. For now the registry registers it into `Card`'s map, and it re-registers it after
`clearOracleRegistry()`. That fixes the 38 classes that lose their data today. `registerEmbeddedOracle`
and the embedded map go.

### 3. `Card.applyOracle`

Depends on: nothing.

Move the field-copying block of `Card()` into `public void applyOracle(OracleData data)`
(`assertMutable()` first) and call it from `Card()`. No behaviour change. Unit test: a card built from
data equals one dressed with it, field by field (extend `OracleDataTest`).

### 4. The registry owns the oracle data and printings apply it

Depends on: 3.

`CardRegistry` keeps the class → data map itself and mirrors every write into `Card`'s static map, which
`Card()` still reads. Wrap each scanned printing's factory so that `createCard()` dresses the front and
its back face. That means every printing-built card is dressed twice with the same data, which proves the
factory path covers production before step 7 removes the constructor path. `getEntries()` and
`resolvedOracle` read the registry's map.

Tests: a printing-built double-faced card has both faces' data with `Card()`'s lookup disabled (a
package-private switch for the test). Two `CardRegistry` instances in one JVM hold different data for
one class without seeing each other's.

### 5. Cards a running game creates are dressed by the engine

Depends on: 4.

Add `Card CardCatalog.dress(Card card)`. Route every runtime-built card through it:

- the 11 `Supplier<Card>` effect records' handlers, where they call `get()`;
- the 4 spellbook handlers' `X::new` lists;
- `MeldWithNamedCreatureEffectHandler.instantiateMeldResult`;
- the effect records holding a sample `Card` (12 sites in 7 card files): hold a supplier or class
  instead, and dress when the engine builds the card.

Grep `engine`, `ai` and `webservice` main code for any other `new X()` of a card class, or reflective
construction, and route it the same way. Card tests for each converted card must pass with
`Card()`'s lookup disabled (the step 4 switch, now settable from the harness).

### 6. The test harness dresses the cards a test puts into a game

Depends on: 5.

Every `GameTestHarness` and `BaseCardTest` method that puts a card into a zone dresses it (setHand,
setLibrary, setGraveyard, the addToBattlefield family, exile and stack helpers, the AI test fixtures).
Move the ~2,500 bypass sites (`new Permanent(new X())`, direct zone `add`) to harness helpers with a
script, and check the diff. Turn on the "no constructor lookup" switch for the card-test context and
ask for a full card-test run. Each failure is either a missed entry point (fix the harness) or a test
reading data off a card it never put into a game (dress it explicitly, or put it into the game). Record
the categories under Findings.

### 7. Remove the constructor lookup and the static registry

Depends on: 6, and a green full run with the switch on.

`Card()` no longer touches oracle data. Delete `Card.oracleRegistry`, `registerOracle`,
`registerOracleIfAbsent`, `clearOracleRegistry`, `findRegisteredOracle`, `OracleDataResolver` and its
installation, and the step 4 mirror and switch. ON_DEMAND loads happen only in catalog calls (`dress`,
`getEntries`, `ensureCardDataLoaded`). Card-data tests build their own registry instead of clearing
global state. Add the freeze guard from the Design section. Ask for a full run.

### 8. Docs and the card-authoring guidance

Depends on: 7.

Replace the `oracle-data.md` entries that say `Card()` fills the metadata. Add the invariant "a card
never joins a game undressed" with its owner. Update `ARCHITECTURE.md`'s testing paragraph, AGENTS.md's
card-constructor line, and the implement-card skill and templates wherever they build cards in tests.

## Findings

(Nothing yet.)

## Prompt

Replace `<N>` with the step number:

```
Implement step <N> of oracle-data-ownership.md. First read the whole file (Why, Measurements, Design,
Ground rules) and step <N>'s section, and check in the Status table that every step it depends on is
done; if one isn't, stop and tell me. Follow the ground rules: read agent-docs/ARCHITECTURE.md and
agent-docs/architecture/oracle-data.md before touching engine, card-data or domain code, keep every
card's data exactly as it is today, bump StoredSet.CONVERSION_VERSION for mapping changes, and test
behaviour through the engine. Run only the tests for what you touched — never the full suite; ask me
to run it. When the step is done, mark it done in Status, add anything later steps need under
Findings, and tell me which tests changed and why. Don't commit until I say so.
```
