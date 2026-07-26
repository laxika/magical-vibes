# Oracle Loader Refactor — Plan

Goal: one Spring-selected oracle loader, one copy of the registry-population loop, one copy of the
oracle-mapping policy. Today there are two hand-synced loaders and a dispatcher bean that picks
between them at runtime.

Status: **stages 0 and 1 are done; stages 2–3 not started.** The two oracle-data bugs found while
writing this plan are **fixed** — see
[Pre-existing bugs](#pre-existing-bugs-found-while-checking-the-above). The refactor has a verified
baseline: both loaders agree, and the behavior it must preserve is the corrected behavior, not the
buggy one.

**Stage 1b — `CardRegistry` (done, after stage 1).** The registry-ownership change asked for after
stage 1 landed. `CardSet`'s three static maps (`scannedPrintings`, `setNameRegistry`,
`setCardTotalRegistry`) are gone; the enum is now just set codes. In their place:

- `CardCatalog` (interface, `magical-vibes-card`) — `getPrintings`, `findByCollectorNumber`,
  `getName`, `getSetCardTotal`, `getImplementedFraction`, all keyed by `CardSet`.
- `CardRegistry` (`@Service`, `magical-vibes-card-data`) implements it, constructor-injects
  `OracleLoader`, and in `@PostConstruct` scans the classpath then drives the loader per set.

The interface split is forced by the module graph: `OracleLoader` is in card-data and the dependency
runs card-data → card, so the implementation cannot live next to `CardSet`. `magical-vibes-card` has
**no Spring dependency at all** and keeping it that way was explicit — so the three readers there
that cannot inject (the `CardSet` and `PrebuiltDeck` enums, the `RandomDeckGenerator` static
utility) take a `CardCatalog` as a parameter instead. `RandomDeckGenerator` became an instance class
wired by a `@Bean` in `CardDataConfiguration`; its pool cache was JVM-wide static state silently
tied to whichever catalog warmed it first.

**`OracleLoaderPresenceGuard` was deleted.** It existed because nothing injected `OracleLoader`, so
a typo'd property produced zero loaders silently — verified at the time by booting the real engine
context with the guard removed (1102 beans, clean refresh, cards with `name=null`). Now
`CardRegistry` requires a loader, so the same typo is an ordinary unsatisfied-dependency failure at
refresh. `OracleLoaderSelectionTest` asserts `NoSuchBeanDefinitionException` instead of the guard's
message.

`SetCompletenessTest` lost its `@BeforeEach`/`@AfterEach` hooks: they only ever scrubbed the global
map between cases, and a per-test registry makes that structural.

Decisions taken when stage 1 landed:

- **The silent MTGJSON fallback is gone.** Behavior change 1 below was accepted as recommended:
  a failed Scryfall load now fails startup instead of quietly swapping sources.
- **Typo protection is `OracleLoaderPresenceGuard`**, the guard-bean option rather than
  `@ConfigurationProperties`. `OracleDataProvider` survives as the canonical list of accepted
  property values, which the guard's error message is built from.

What stage 1 actually changed: `OracleLoader` interface added; both loaders are now
`@ConditionalOnProperty` `@Service` beans with a `cacheDir` constructor param and a `@PostConstruct`
`loadAll()`; `ScryfallDataService` deleted; `magical-vibes-scryfall/` deleted; three test call sites
became `new XOracleLoader(CACHE_DIR).loadAll()`; `OracleLoaderSelectionTest` added (bean selection
per property value + the typo guard, with no network — it asserts on bean *definitions* and never
refreshes a context in which a loader would survive to load); `ARCHITECTURE.md:19,46` and the
`MtgjsonOracleLoader`/`OracleDataProvider` javadoc updated. Every parse helper stayed `static`, so
the parsing unit tests were untouched.

## Non-goals

- No change to what ends up in `Card.oracleRegistry`, `CardSet`, or `CardPrintingRegistry` for any
  currently-loaded printing, except the deliberate behavior changes listed under
  [Behavior changes](#behavior-changes) — each of which needs a yes/no before it lands.
- No change to the cache-file layout (`{set}.json`, `mtgjson-{set}.json`) or the cache directory.
  `CardBrowserService.java:53` and `DraftService` read `card-data.cache-dir` independently and must
  keep working untouched.
- No property renames. `oracle.data-provider` and `card-data.cache-dir` keep their current names —
  `.github/workflows/build.yml:43` and `build.gradle.kts:64` depend on the former.

## Current state

| Class | Path | Shape |
|---|---|---|
| `ScryfallDataService` | `carddata/scryfall/` | `@Service`, one package-private `@PostConstruct void init()`, zero call sites |
| `ScryfallOracleLoader` | `carddata/scryfall/` | static utility, no interface, 448 lines |
| `MtgjsonOracleLoader` | `carddata/mtgjson/` | static utility, no interface, 347 lines |
| `OracleDataProvider` | `carddata/` | enum `SCRYFALL`/`MTGJSON`, read only by the `@Value` in `ScryfallDataService` |

Wiring: `CardDataConfiguration` component-scans the package → `GameEngineConfig` imports it →
both `MagicalVibesApplication` and `GameTestDoublesConfig` import that. Tests boot a plain
`AnnotationConfigApplicationContext` (`GameTestEngineContext.java:26`), **not** Spring Boot, so
`application.properties` is never read in tests — the `SCRYFALL` default comes from the `@Value`
default string, and CI overrides it with the `-Doracle.data-provider=MTGJSON` system property.

Neither loader is ever injected; every use is a static call. `ScryfallDataService` is referenced by
nothing. Blast radius outside `carddata` is zero.

## Target architecture

```
OracleRegistryLoader          @Service, unconditional, @PostConstruct
  └─ owns the CardSet loop and every registry write
       ↓ depends on
OracleSource                  interface — @ConditionalOnProperty picks the impl
  ├─ ScryfallOracleSource     JSON → RawFace, Scryfall quirks only
  └─ MtgjsonOracleSource      JSON → RawFace, MTGJSON quirks only
       ↓ both produce
RawFace                       provider-neutral, self-contained, stringly-typed
       ↓ consumed by
FaceOracleMapper              RawFace → OracleData, all rules policy, one copy
```

**The split rule.** An `OracleSource` owns *provider quirks*: field naming, face resolution,
array ordering, syntax normalization, HTTP and caching. `FaceOracleMapper` owns *rules policy*:
what a back face is, name splitting, land color fallback, keyword stripping. Anything a source
does "to match what the other loader outputs" is a smell that the logic belongs in the mapper.

## Stage 0 — Cleanup

Delete `magical-vibes-scryfall/`. It contains only `build/` with compiled classes from the removed
`com.github.laxika.magicalvibes.scryfall` package (including a nested `ScryfallOracleLoader$TokenImageData`
that no longer exists in source). No `src/`, absent from `settings.gradle.kts`. It currently pollutes
every grep for these class names.

## Stage 1 — Interface + conditional bean

Lands independently and is a strict subset of stages 2–3.

1. New `carddata/OracleLoader.java`:
   ```java
   public interface OracleLoader {
       void loadAll();
   }
   ```
2. Both loaders `implements OracleLoader`, `loadAll` drops `static`, `cacheDir` moves to a
   constructor param:
   ```java
   @Service
   @ConditionalOnProperty(name = "oracle.data-provider", havingValue = "SCRYFALL", matchIfMissing = true)
   public class ScryfallOracleLoader implements OracleLoader {
       private final String cacheDir;
       ScryfallOracleLoader(@Value("${card-data.cache-dir:./card-data-cache}") String cacheDir) { ... }
       @PostConstruct @Override public void loadAll() { ... }
   }
   ```
   MTGJSON gets the same with `havingValue = "MTGJSON"` and no `matchIfMissing`.
3. Delete `ScryfallDataService`.
4. Keep every parse helper `static` — they are pure functions and the unit tests call them directly.

**`matchIfMissing = true` is load-bearing.** Tests run without `application.properties`, so absent-property
must resolve to Scryfall exactly as the old `@Value` default did. `havingValue` matching is
case-insensitive, so enum-cased property values are fine. `@ConditionalOnProperty` is evaluated by
core Spring's `ConditionEvaluator`, so it works in the plain `AnnotationConfigApplicationContext`
the tests use; `StandardEnvironment` includes system properties, so CI's `-D` flag still selects MTGJSON.

**Keep `OracleDataProvider`.** With `@ConditionalOnProperty` alone, a typo (`SCYRFALL`) creates *zero*
loaders and the app starts with an empty oracle registry, failing far from the cause. Today the
`@Value` enum binding rejects that at startup. Preserve it with either a `@ConfigurationProperties("oracle")`
record binding the enum (binding alone rejects bad values; nothing needs to read it), or a guard bean
taking `ObjectProvider<OracleLoader>` that throws when none is present. Pick one — the guard bean is
fewer moving parts.

**Test churn:** `OracleLoaderIntegrationTest.java:33,41` and `AuraEnchantTargetInvariantTest.java:50`
become `new XOracleLoader(CACHE_DIR).loadAll()`. The conditional gates only *Spring* creation, so that
integration test can still exercise both loaders in one JVM — which it must, since it asserts they
produce identical registries.

## Stage 2 — `RawFace` + shared mapper

### Why the parse signatures are asymmetric today

Not sloppiness — the upstream shapes differ in self-containedness.

- **MTGJSON** splits faces upstream: each face is its own entry in `cards[]` tagged `side: a/b`,
  carrying its own `keywords`, `colors`, `text`. So `parseOracleData(face, isBackFace)` always gets a
  complete node and `isBackFace` is pure policy.
- **Scryfall** ships one object per card. Face fields live top-level *or* in `card_faces[i]` depending
  on `layout` (hence `getFrontFaceNode` at `ScryfallOracleLoader.java:366` and the
  `faceNode.has(x) ? faceNode : card` ladder repeated seven times at `:227-260`), and **`keywords`
  exists only at top level, combined across both faces**. Back-face parsing therefore needs *two*
  nodes — `parseCardText(face, card)` at `:331`, `parseKeywords(card)` at `:338`. A single
  `(node, isBackFace)` signature cannot express that.

Fixing this by adding a `JsonNode card` param to the Scryfall version and passing `(face, face, ...)`
from MTGJSON would match the signatures while making one of them a lie. Normalize the *data* instead.

### `RawFace`

```java
/** One printed face, provider-neutral and self-contained by construction. */
public record RawFace(
        String name, String manaCost, String typeLine, String text,
        List<String> colors, List<String> colorIndicator, List<String> colorIdentity,
        String power, String toughness, String loyalty, String defense,
        List<String> keywords, String watermark) {}
```

Deliberately stringly-typed so the extractors stay dumb and every interpretation —
`CardDataSupport.parseIntField`, `COLOR_MAP`, `TypeLineParser`, keyword mapping,
`OracleTextNormalizer` — lives in one place. Cost is one allocation per face on a startup-only path.

**`keywords` must hold raw upstream spellings, not mapped `Keyword` enums.**
`OracleTextNormalizer.capitalizeKeywordLines` (`:40`) matches text segments against the raw
lowercased upstream strings, which include keywords absent from `CardDataSupport.KEYWORD_MAP`
("Ward {2}", "Protection from red"). Mapping to the enum first would silently stop capitalizing those.

Change `capitalizeKeywordLines(String, JsonNode)` → `capitalizeKeywordLines(String, Collection<String>)`,
which also drops the last Jackson dependency from the normalizer.

### `FaceOracleMapper`

One `public static OracleData toOracleData(RawFace face, boolean isBackFace)` absorbing the policy
that exists twice today:

| Rule | Scryfall | MTGJSON |
|---|---|---|
| strip `" // "` from name | `:221` | `:204` |
| back face prefers color indicator | `:316` | `:218` |
| back face drops `TRANSFORM` | `:339` | `:246` |
| back face drops `PREPARED` | `:341` | **absent** |
| back face nulls loyalty/defense/watermark | `:356-358` | `:241`, `:250` |
| land color falls back to color identity | `:409-417`, `:432-444` | `:224-226` |

Five rules, ten implementations, already one divergence (see below).

### Extractors

- `ScryfallOracleSource` absorbs `getFrontFaceNode`, the `faceNode.has(x) ? faceNode : card` ladder,
  and copies the parent's combined `keywords` array onto **both** faces' `RawFace.keywords`.
- `MtgjsonOracleSource` is close to a field rename (`faceName`→`name`, `manaCost`, `type`, `text`),
  and keeps its two compensating quirks locally: the `[+1]:` → `+1:` loyalty-bracket regex (`:234`)
  and the color-array sort (`:317-322`).

**Do not move the color sort into the mapper.** It compensates for MTGJSON's ordering differing from
Scryfall's serialization; Scryfall is the default provider, so its ordering is the reference and must
not be perturbed. Ordering normalization is a provider quirk by the split rule above. Likewise
`KEYWORD_MAP_LOWERCASE` disappears — keyword mapping moves into the mapper and should just be
case-insensitive unconditionally.

### Invariant to preserve exactly

Keyword stripping happens on the **mapped `Set<Keyword>` only, after capitalization** — never on
`RawFace.keywords()` before it. Today Scryfall calls `parseCardText(face, card)` with the full parent
keyword list (Transform and Prepared included), and only afterwards removes them from the mapped set
(`:339`, `:341`). Stripping earlier would change which text lines get capitalized.

## Stage 3 — `OracleSource` SPI + single registry loop

```java
public interface OracleSource {
    SetOracleData loadSet(String setCode);
    Map<String, CardPrintingRegistry.TokenImageData> loadTokens(String setCode);
}

public record SetOracleData(
        String setName,
        int cardTotal,
        Map<String, String> rarityByCollectorNumber,
        Map<String, RawFace> frontFaces,
        Map<String, RawFace> backFaces) {}
```

`OracleRegistryLoader` (unconditional `@Service`, owns the `@PostConstruct`) runs the loop that is
currently duplicated at `ScryfallOracleLoader.java:37-100` and `MtgjsonOracleLoader.java:66-124`:
register set name and card total → register rarities → per `CardPrinting`, build the temp card via
`printing.factory().get()`, `Card.registerOracle(className, map(front, false))` and
`Card.registerOracleIfAbsent(backFaceClassName, map(back, true))` → register tokens.

`@ConditionalOnProperty` moves from the loader to the two `OracleSource` beans. The conditional then
selects a *data source* rather than a *lifecycle*, which is what it always meant.

`loadTokens` stays on the SPI because the two paths are genuinely different, not just differently
written: Scryfall fetches separate `t{code}` sets over HTTP and writes an empty `[]` cache on failure
to avoid refetching (`:102-138`); MTGJSON reads `setData.tokens` inline from the already-loaded file
and additionally skips `side != "a"` entries (`:278-311`). Both already register under the *card* set
code with a `t`-prefixed `TokenImageData` set code, so the registry contract is unchanged.

The cache-or-fetch dance (`ScryfallOracleLoader.java:140-157`, `MtgjsonOracleLoader.java:155-174`) is
the same shape twice; fold it into a `SetJsonCache` helper next to `CardDataSupport.writeCacheFile`,
parameterized by filename prefix and a fetch lambda.

**Known cost:** this parses every front face into `RawFace` eagerly, where today rarity is read
straight off the raw `JsonNode` and only implemented printings are parsed. A few hundred extra
record allocations per set on a startup path — measure only if startup time visibly regresses.

## Behavior changes

Each needs an explicit decision before landing.

1. **The silent MTGJSON fallback disappears.** `ScryfallDataService.java:31-34` currently catches any
   `RuntimeException` from the Scryfall load and retries with MTGJSON, logging only a `WARNING`.
   One conditional bean cannot do that.

   *Recommendation: drop it.* Rules accuracy is the project's stated first priority, and a silent
   source swap means the same build can produce different oracle text on different runs. Failing
   startup with "Scryfall unreachable, set `oracle.data-provider=MTGJSON`" is the better failure.
   It is also close to dead in practice: CI pins MTGJSON explicitly (`build.yml:43`) and excludes the
   `scryfall-api` tag entirely when `CI` is set (`build.gradle.kts:54-56`), so only a fresh dev
   machine during a Scryfall outage ever hits it. The current implementation is also subtly wrong —
   Scryfall can register several sets before throwing, and MTGJSON's `registerOracleIfAbsent` back-face
   path will not overwrite that partial state.

   *If you want it kept:* a `FallbackOracleLoader implements OracleLoader` composite behind a separate
   property, ~10 lines — but then both loaders exist as beans again, which is what stage 1 is for
   avoiding.

   *Nothing else.* The two provider divergences originally listed here — back-face land colors and
   `PREPARED` on prepare-spell back faces — turned out to be Scryfall bugs rather than refactor
   consequences, and are already fixed. See the next section. With those out of the way, stages 1–3
   are pure structure: the only behavior change left is the fallback removal above.

## Pre-existing bugs found while checking the above

Both are **fixed** (commit pending), ahead of the refactor, so stages 1–3 preserve correct behavior
rather than freezing a bug into a new abstraction.

### 1. Back faces inherited the front face's keywords — FIXED

**Scryfall back faces inherited the front face's keywords. 21 implemented printings were affected.**

`parseBackFaceOracleData` builds the back face's keyword set with `parseKeywords(card)` (`:338`) —
the **top-level `keywords` array, which Scryfall populates with the union of both faces' keywords** —
then hand-removes `TRANSFORM` and `PREPARED`. Those two removals are patches for the two most visible
symptoms of a general defect: every *other* front-face keyword still lands on the back face.

Confirmed live (back-face class owns no printing of its own, so `registerOracleIfAbsent` data is
authoritative; `Card()` at `Card.java:167-184` copies `oracle.keywords()` straight onto the instance):

| Leaked | Cards |
|---|---|
| `DEFENDER` | Awoken Horror, Bane of Hanweir (ISD + INR), Biolume Serpent, Ludevic's Abomination |
| `DISTURB` | Ghostly Castigator, Lanterns' Lift, Luminous Phantom, Twinblade Invocation |
| `VIGILANCE` | Unhallowed Cathar, Thraben Militia, Avacyn the Purifier |
| `FLYING` | Ghastly Haunting, Stalking Vampire, **Heroic Stanza (a Sorcery)** |
| `HASTE` | Vildin-Pack Alpha, Moonrise Intruder |
| `FIRST STRIKE` | Terror of Kruin Pass (ISD + INR) |
| `FLASH` | Avacyn the Purifier, Deluge of the Dead |
| `COVEN` | Seasoned Cathar |

The `DEFENDER` ones are rules-breaking, not cosmetic: `CombatAttackService.java:858` refuses to
declare an attacker with `Keyword.DEFENDER`, so **Thing in the Ice flips into a 7/8 Awoken Horror
that cannot attack**, and Bane of Hanweir gets `DEFENDER` on a creature whose own text says it
attacks each combat if able. MTGJSON does not have this bug — it reads per-face `keywords`.

**How it was fixed.** Dropping the parent's list entirely would strip legitimate back-face keywords
(Avacyn, the Purifier really does have flying), so instead the combined list is narrowed to the
keywords the back face's own text states, via a new
`OracleTextNormalizer.keywordsStatedIn(String, Collection<String>)` that reuses the same
"is every comma-separated segment a keyword" matching `capitalizeKeywordLines` already had.
`ScryfallOracleLoader.parseBackFaceKeywords` calls it. That reproduces MTGJSON's per-face semantics
out of Scryfall's combined array, and the `TRANSFORM`/`PREPARED` special cases are gone — neither
heads a keyword line of any back face's text, so both fall out for free.

It also fixes a third case nobody had noticed: a back face that *grants* a keyword ("Creatures you
control have flying") no longer claims that keyword for itself.

Proof: `ThingInTheIceTest` transforms Thing in the Ice and attacks with Awoken Horror. Before the fix
it failed with `IllegalStateException: Invalid attacker index: 0`; after, it passes.

### 2. Back-face lands came out colorless — FIXED

The land → color-identity fallback in `parseColor`/`parseColors` read `color_identity` off whichever
node it was given, and Scryfall puts that field on the top-level card only. Both now take an explicit
`identitySource` parameter, so a face node gets its colors from the face and its identity from the
parent. The five XLN back-face lands listed above now match MTGJSON.

## Test plan

Per CLAUDE.md: do not run the full suite — ask, and the module's tests get run.

- **New:** table-driven `FaceOracleMapperTest` covering the six policy rules above against `RawFace`
  literals — no JSON. This is where policy is tested exactly once.
- **Rewrite:** `ScryfallOracleLoaderTest` and `MtgjsonOracleLoaderTest` currently re-test the same
  policy twice through provider-shaped fixtures. They shrink to extraction only: "this JSON yields
  this `RawFace`." Keep every existing fixture — the prepare-layout front/back cases, the transform
  DFC, the color-indicator back face, the land color-identity case, the loyalty-bracket
  normalization, the keyword-capitalization trio.
- **Keep as-is:** `OracleLoaderIntegrationTest` is the safety net for this entire refactor — it runs
  both providers end-to-end and asserts identical registry invariants. It only needs the constructor
  change from stage 1. Run it before and after each stage.
- **Keep as-is:** `BackFaceOracleRegistrationTest` (registry precedence), `SetCompletenessTest`
  (already clears `CardSet.clearSetCardTotalRegistry()` in `@BeforeEach`/`@AfterEach` because the
  loaders mutate JVM-wide statics — still true, the beans do not make that state instance-scoped),
  `AuraEnchantTargetInvariantTest`.
- **Add:** a context test asserting exactly one `OracleSource` bean exists for each property value,
  and that the typo guard fires on a bad value.

## Docs to update

- `agent-docs/ARCHITECTURE.md:19` — module map still names `ScryfallDataService` and, incorrectly,
  a `ScryfallTypeLineParser`; the real class is `TypeLineParser` in the parent `carddata` package.
- `agent-docs/ARCHITECTURE.md:46` — the "Scryfall Oracle Data" section describes the `@PostConstruct`
  dispatcher and the automatic fallback.
- `CardPrintingRegistry.java:12-17` — javadoc references `OracleDataProvider` and "whichever loader
  the property selects"; reword for the source SPI.
- `MtgjsonOracleLoader.java:38` — class javadoc mentions the fallback.

## Suggested order

Stage 0 and 1 are independent of 2 and 3 and can land first; 2 is the largest and highest-value;
3 is mostly mechanical once 2 exists.

```
0. delete magical-vibes-scryfall/                          DONE
1. OracleLoader + @ConditionalOnProperty + typo guard      DONE
1b. CardCatalog + CardRegistry                             DONE
2. RawFace + FaceOracleMapper + extractors                 DONE
3. OracleSource + OracleRegistryLoader + SetJsonCache      DONE
```

**The refactor is complete.** `SetJsonCache` holds the cache-or-fetch dance once, parameterized by
filename prefix, source name and a fetch lambda; each loader owns only its own HTTP shape. Scryfall's
inter-request sleep moved into `fetchFromScryfall`, so the fetcher owns its own rate limiting and the
cache stays dumb. `SetJsonCacheTest` covers the write path with a stub fetcher — the real loaders
only ever exercise the cache-*hit* path once a machine has warmed its cache, so a regression in the
write path would otherwise stay invisible until someone cloned the repo fresh.

Note for stage 3: `CardPrintingRegistry.java:12-17`'s javadoc was left alone — "whichever loader the
property selects" is still accurate. It needs the reword once the `OracleSource` SPI makes the
conditional select a *source* rather than a *loader*.

**Stage 3 is now essentially done, ahead of stage 2.** `OracleLoader` became a pure function:

```java
SetOracleData loadSet(String setCode, Set<String> implementedCollectorNumbers);
```

`SetOracleData` carries set name, card total, rarities, front/back oracle data keyed by collector
number, and token images. Loaders write nothing — `CardRegistry.register` performs every
registration, so the per-set loop that stage 3 wanted to deduplicate exists exactly once.
`implementedCollectorNumbers` preserves today's laziness (only implemented printings get their
oracle text parsed), which is what the "known cost" note below was worried about; it no longer
applies. Tokens are a field on the record rather than a separate SPI method: MTGJSON ships them
inline in the set file, so a second call would mean re-reading and re-parsing it.

What is left of stage 3: the cache-or-fetch dance is still written twice
(`ScryfallOracleLoader.loadSetJson`, `MtgjsonOracleLoader.loadSetJson`) and could fold into a
`SetJsonCache` helper. Minor.

**Stage 2 is done.** `RawFace` + `FaceOracleMapper` landed; every rules decision about a face is now
written once. `ScryfallOracleLoader` and `MtgjsonOracleLoader` keep only extraction (the
prefer-face-else-card ladder, MTGJSON's loyalty-bracket and watermark-suffix fixes, its colour-array
sort) and each ends at a `RawFace`. `KEYWORD_MAP_LOWERCASE` is gone — keyword mapping is
case-insensitive unconditionally via `CardDataSupport.keyword`. `OracleTextNormalizer` lost its last
Jackson dependency: `capitalizeKeywordLines` now takes a `Collection<String>`.

`FaceOracleMapperTest` (14 cases) pins the policy against `RawFace` literals with no JSON. The
provider-shaped loader tests were kept as-is: they still assert end-to-end behaviour through
`parseOracleData`, which is more valuable than asserting `RawFace` shape, and they cover the
extraction the mapper tests deliberately do not.

### The divergence stage 2 actually caught

The plan assumed only Scryfall inherited front-face keywords onto back faces, on the strength of a
line in this doc saying "MTGJSON does not have this bug — it reads per-face `keywords`". That was
wrong; `MtgjsonOracleLoader`'s own class javadoc said the opposite, and it was right. MTGJSON's
per-face `keywords` is *also* the combined list, so it leaked exactly the same way and had only ever
been patched for `TRANSFORM`.

Proof: `ThingInTheIceTest` passed under Scryfall and **failed under MTGJSON** with
`IllegalStateException` on the attack — and CI runs `-Doracle.data-provider=MTGJSON`, so it was
failing there. Unifying the rule in `FaceOracleMapper` fixed it for both; the test now passes under
each provider. This is the concrete argument for stage 2: the bug was invisible while the policy
existed twice.
