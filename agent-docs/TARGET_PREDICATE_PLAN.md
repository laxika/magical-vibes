# Target Predicate Migration Plan

Working document for replacing `TargetCategory` (14 constants in
`magical-vibes-domain/.../model/effect/TargetCategory.java`) with a composable `TargetPredicate`
sum type layered over the predicate hierarchies that already exist in
`magical-vibes-domain/.../model/filter/`.

`TargetCategory` and the predicate system are **not** duplicates today — the category picks the
candidate *domain* (battlefield / players / stack / graveyard / exile) and the predicate filters
*within* one domain. But the domain axis is currently expressed as a flat enum that gets squeezed
through two booleans (`includesPermanents()` / `includesPlayers()`), which loses information, and
three of its constants duplicate an enum (`GraveyardSearchScope`) that already exists. This plan
keeps the domain axis but makes it a composable value instead of an enum.

---

## How to run this plan

Steps are executed **one at a time, each in a fresh context**. The context is cleared between
steps, so every step must be self-contained: this file is the only state that carries across.

**Protocol for each step:**

1. Read this file and the step's entry below.
2. **Verify the premise yourself before editing.** The audit behind this plan was thorough but is
   not a substitute for looking. If a premise no longer holds, say so and stop rather than forcing
   the change.
3. Read `agent-docs/ARCHITECTURE.md` before any change beyond a card class + its test
   (per `CLAUDE.md`) — card freezing, CR 613 layers, thread safety, Jackson 3 imports,
   view immutability.
4. **Behavior must stay identical** unless the step explicitly says otherwise. Where a step notes
   a behavior change or a latent bug, make the change deliberately and cover it with a test.
5. Verify every Comprehensive Rules number with the `rules` MCP `get_rule` tool before writing it
   anywhere.
6. Update the relevant `agent-docs/` files — `EFFECTS_INDEX.md` (the category table at lines
   ~115-133 is the canonical spec and **must** be rewritten), `PREDICATES_REFERENCE.md`,
   `TRIGGER_SLOT_TARGETING.md`, `CARD_IMPLEMENTATION_PLAYBOOK.md`, `ARCHITECTURE.md:33`.
7. Add or update tests. **Never run the full test suite** — run only the affected test classes
   with `--tests` filters.
8. Mark the step `DONE` in the Step Index, with a one-line note if anything deviated.
9. **Finish by printing the prompt for the next step** (template at the bottom).
10. **Do not commit.** Commits happen only when explicitly asked.

---

## Why this is worth doing

Four concrete defects in the current design, in descending order of confidence:

**1. `AnyTargetPredicateTargetFilter` requires writing one restriction twice.**

```java
public record AnyTargetPredicateTargetFilter(PermanentPredicate permanentPredicate,
                                             PlayerPredicate playerPredicate,
                                             String errorMessage) implements TargetFilter {}
```

The two predicates are supposed to express the same restriction over two different kinds, and
nothing checks that they agree. This is the clearest symptom that the type system is missing a
cross-kind disjunction.

**2. `ANY_TARGET` is inferred, not declared.** `ValidTargetService:531-545` never compares against
`TargetCategory.ANY_TARGET`. It reconstructs it:

```java
boolean allAnyTarget = !permanentEffects.isEmpty()
        && permanentEffects.stream().allMatch(e -> e.targetSpec().category().includesPlayers());
if (allAnyTarget) {
    if (!gameQueryService.isCreature(gameData, perm) && !isPlaneswalker(perm)) return false;
}
```

`PLAYER_OR_PERMANENT` is `(includesPermanents=true, includesPlayers=true)`, so it satisfies
`allAnyTarget` and receives the creature/planeswalker narrowing even though it means "a player or
*any* permanent". The two categories are semantically distinct and the enumerator cannot tell them
apart.

> **REFUTED by Step 2, then FIXED by Step 2b — read those two outcome sections instead of this
> paragraph.** The claim below (two producers; `ClashEffect` reaching `PLAYER_OR_PERMANENT` for Fire
> Juggler; `CreatureModTargetValidators` covering `DistributeCountersAmongTargetsEffect`) is wrong on
> every particular, and the direction of the defect is inverted: `PLAYER_OR_PERMANENT` was an
> *unchecked escape hatch*, and the `allAnyTarget` inference was the only thing narrowing eight real
> cards. Step 2b made those effects declare honestly and deleted the inference. Original text, kept
> for the record:
>
> ~~Both producers of `PLAYER_OR_PERMANENT` were checked. `DistributeCountersAmongTargetsEffect`
> selects it deliberately as a documented null-tolerance escape hatch (its own comment says
> "PLAYER_OR_PERMANENT is a no-op in the spec interpreter"), and creature-only legality is enforced
> downstream by `CreatureModTargetValidators` plus the cast-time assignment loop in
> `SpellCastingService`. `ClashEffect` only computes `PLAYER_OR_PERMANENT` when its child effects
> target both kinds — e.g. Fire Juggler ("creature or player"), where narrowing to
> creature/planeswalker is correct.~~

**3. `MayAbilityHandlerService` open-codes the category switch and gets it wrong.** Duplicated
verbatim at lines 367-399 **and** 776-805:

```java
matches = switch (permanentTargetCategory) {
    case CREATURE -> gameQueryService.isCreature(gameData, p);
    case CREATURE_OR_PLANESWALKER, ANY_TARGET -> ...;
    case PLAYER_OR_PERMANENT, PERMANENT -> true;
    default -> false;
};
```

`LAND` and `PLAYER_OR_PLANESWALKER` fall into `default -> false`, so a may-ability with a bare
`LAND` spec and no target filter finds zero legal permanents — while `TargetValidationService`
would accept the same target. The `default` arm defeats the sealed-switch exhaustiveness that
protects the rest of the codebase.

> **CONFIRMED and fixed by Step 3** — see "Step 3 outcome". Boggart Shenanigans (LRW 155) was a live
> victim: its `PLAYER_OR_PLANESWALKER` may-trigger never offered a planeswalker.

**4. "Any target" omits battles.** CR 115.4 (verified): *"These targets may be creatures, players,
planeswalkers, or battles."* `CardType.BATTLE` exists and is handled in combat code, but
`grep Battle` over `model/filter/` returns zero hits — there is no battle predicate, and
`ANY_TARGET` resolves to creature/planeswalker/player in all three enumeration paths. Under the new
design this is a one-line fix in a single factory instead of an edit to every path that
special-cases `ANY_TARGET`.

### What is NOT a blocker

The `TargetSpec` javadoc warns that predicates sit inside Jackson-serialized effect records, so the
hierarchy cannot change shape:

> *"Effects are Jackson-serialized by their record components, so adding a component would change
> the wire format and `equals()`."*

**This is stale.** Nothing deserializes effects. `ObjectMapper`/`readValue` appear only in
`JacksonConfig`, `FakeConnection` (test fixture), `CardBrowserService`, and `DeckService`, none of
which touch `CardEffect`. `magical-vibes-networking` has zero references to `TargetCategory`,
`TargetFilter`, or `PermanentPredicate` — the client receives resolved UUID lists
(`ValidTargetsResponse`) and derived booleans (`CardView.needsTarget`), never the targeting model.
No effect record carries `@JsonTypeInfo`/`@JsonSubTypes`, so polymorphic round-tripping is not
possible today regardless. Correct the javadoc in Step 1; do not let it veto the design.

---

## The design

```java
public sealed interface TargetPredicate {

    /** A battlefield permanent matching {@code inner}. */
    record Permanents(PermanentPredicate inner) implements TargetPredicate {}

    /** A player matching {@code inner}. */
    record Players(PlayerPredicate inner) implements TargetPredicate {}

    /** A card in a graveyard within {@code scope} matching {@code inner}. */
    record GraveyardCards(CardPredicate inner, GraveyardSearchScope scope) implements TargetPredicate {}

    /** A card in exile matching {@code inner}. */
    record ExiledCards(CardPredicate inner) implements TargetPredicate {}

    /** A spell or ability on the stack matching {@code inner}. */
    record Spells(StackEntryPredicate inner) implements TargetPredicate {}

    /** Disjunction across kinds — the ONLY cross-kind combinator. See the algebra rules. */
    record AnyOf(List<TargetPredicate> options) implements TargetPredicate {}
}
```

### Algebra rules — the part to get right

This is where a naive unified predicate hierarchy goes wrong, so the constraints are structural,
not documentary.

1. **Only `AnyOf` crosses kinds. There is no `AllOf` and no `Not` at the `TargetPredicate` level.**
   Conjunction and negation are only sound *within* a kind, where the existing
   `PermanentAllOfPredicate` / `PermanentNotPredicate` / `CardNotPredicate` / … already provide
   them. The reason: if a kind-mismatched leaf evaluates to `false`, then `Not(Permanents(isCreature))`
   is `true` for every player, graveyard card and spell — so `Not` stops being
   complement-within-domain and silently produces over-permissive targeting. And `AllOf` spanning
   two kinds is unsatisfiable by construction. Forbidding both at this level removes the hazard
   instead of documenting it.

2. **`AnyOf` holds at most one leaf per kind, enforced in the compact constructor.** Flatten nested
   `AnyOf`s, then reject duplicates. This makes `AnyOf` isomorphic to a `kind -> predicate` map,
   which is precisely what enumeration needs: "what is the permanent restriction? what is the
   player restriction?" — a lookup, not a search. Two leaves of the same kind should be merged by
   the caller into one `PermanentAnyOfPredicate`, which is the sound spelling.

3. **Enumeration reads the top-level leaves to decide which collections to iterate.** No scan of
   the whole game universe; `ValidTargetService` keeps bucketing into `validPermanentIds` /
   `validPlayerIds` / `validGraveyardCardIds` exactly as it does now.

4. **The kind leaves are adapters, not rewrites.** Each delegates to the evaluator that already
   owns that hierarchy, so `FilterContext`, the CR 613 static whitelist, and the four existing
   evaluator signatures are untouched. The 132 existing predicate records do not change.

### Category → factory mapping

All 14 constants become factories on a `TargetPredicates` helper. Migration is mechanical.

| Category | Replacement |
|---|---|
| `NONE` | `null` spec / `TargetSpec.NONE` (unchanged) |
| `PLAYER` | `players(ANY)` |
| `PERMANENT` | `permanents(TRUE)` |
| `CREATURE` | `permanents(isCreature)` |
| `LAND` | `permanents(isLand)` |
| `CREATURE_OR_PLANESWALKER` | `permanents(anyOf(isCreature, isPlaneswalker))` |
| `PLAYER_OR_PERMANENT` | `anyOf(players(ANY), permanents(TRUE))` |
| `PLAYER_OR_PLANESWALKER` | `anyOf(players(ANY), permanents(isPlaneswalker))` |
| `ANY_TARGET` | `anyOf(players(ANY), permanents(anyOf(isCreature, isPlaneswalker)))` |
| `SPELL_ON_STACK` | `spells(TRUE)` |
| `GRAVEYARD_CARD` | `graveyardCard(OPPONENT_GRAVEYARD)` |
| `ANY_GRAVEYARD_CARD` | `graveyardCard(ALL_GRAVEYARDS)` |
| `CONTROLLERS_GRAVEYARD_CARD` | `graveyardCard(CONTROLLERS_GRAVEYARD)` |
| `EXILE_CARD` | `exiledCards(TRUE)` |

Two wins fall out of the table itself: `ANY_TARGET` and `PLAYER_OR_PERMANENT` become structurally
distinct (defect 2), and the three graveyard constants collapse onto the `GraveyardSearchScope`
enum that `GraveyardCardPredicateTargetFilter` already carries — deleting the hand-copied mapping
in `GraveyardTargetingSupport`, `ValidTargetService`, `AiTargetSelector`, `SpellCastingService`,
`TriggeredAbilityQueueService` and elsewhere. *(Step 5 did this. There were fourteen copies, not
five, and they disagreed with each other — see "Step 5 outcome".)*

`TargetSpec` keeps `harmful`, `selfTargeting`, and `playerTargetCount` unchanged — those are
orthogonal axes, not targeting domains. `harmful` in particular drives the CR 702.16b protection
check (verified: *"A permanent or player with protection can't be targeted by spells with the
stated quality…"*) and must not be folded into the predicate.

---

## Risk hotspots

Read these before touching the corresponding step. Each is a place where widening the targeting
model can silently break rules correctness.

| Hotspot | Why it is dangerous |
|---|---|
| `PredicateEvaluationService.matchesStaticFilter` (line ~926) | A **whitelist** that `throw`s on unsupported predicates, plus a CR 613.6 memo `LayerSystemService.activeL4FilterVerdict` keyed by *filter instance*. Layer 4 is type-changing, so a new leaf reaching this path can recurse through `computeStaticBonus`. New leaves must not widen what reaches it. |
| `TargetLegalityService.matchesStackEntryPredicate` (line ~1918 region) | An `if/instanceof` chain ending in `return false;` — no compile-time exhaustiveness, unlike every other evaluator. Stack predicates are evaluated in **two** services with a deliberate split (`PredicateEvaluationService` returns hard-coded `false` for the targeting-only ones). Do not make this the fallback for the new hierarchy. |
| `EffectResolution.targetsSpellOnStack` | Special-cases four effect classes by `instanceof` (`ChangeColorTextEffect`, `SetTargetColorEffect`, `GrantColorUntilEndOfTurnEffect`, `PutTargetSpellOrPermanentIntoLibraryNFromTopEffect`) because they target a spell *or* a permanent. These become honest `AnyOf(spells(...), permanents(...))` — but the `instanceof` list must be deleted in the same step, not left as a parallel truth. |
| `PutCounterOnTargetPermanentEffect` | Carries **three** `PermanentPredicate` components with different semantics (`predicate` = resolution-time chooser, `targetPredicate` = target legality with no cast-time gate, `resolutionCondition` = placement gate) and is read through `EffectResolution.targetPredicateOf`. Do not collapse these. |
| Card-level `TargetFilter` vs effect-level `TargetSpec` | Both are enforced, independently. Unifying them is **out of scope** for this plan — it is a second, larger refactor. Keep both paths working. |
| CR 608.2b resolution recheck | Verified: targets are re-checked on resolution using last known information if the source has left its zone. `FilterContext.sourcePermanentSnapshot` exists for exactly this. Any new evaluation path must carry the snapshot through. |
| `TargetSpecRatchetTest` + `scripts/targetspec-audit.py` | Both used to regex for `\bTargetCategory\.(\w+)`. *(Handled: Step 4 taught them `\bTargetPredicates\.\w+\s*\(` and Step 7 dropped the dead arm. They stay in lockstep with each other — change an invariant in one and you must change it in the other.)* |

---

## Step Index

Phase 1 is additive and reversible. Phase 2 changes behavior at two known points. Phase 3 is
mechanical high-churn. Phase 4 is deletion.

| # | Step | Risk | Status |
|---|---|---|---|
| 1 | Introduce `TargetPredicate` + `TargetPredicates` factories + adapter evaluator. `TargetSpec` gains a derived `targetPredicate()` computed from the existing `category`+`predicate`; **no call site changes**. Fix the stale Jackson javadoc. Add the equivalence harness: for each of the 14 factories, assert it accepts/rejects exactly the candidate set the category does. | LOW | **DONE** — see "Step 1 outcome" below. Two deliberate divergences found and pinned; `StackEntryTruePredicate` added. |
| 2 | Move `TargetValidationService.validateSpec` and `ValidTargetService` enumeration onto `targetPredicate()`. Delete the inferred `allAnyTarget` block. **Confirm or refute defect 2** against Fire Juggler, Spoils of War, Blessings of Nature, Contagion before claiming a fix; record the finding here. **Also decide the two Step 1 divergences** (LAND / planeswalker layer-awareness) — they land the moment the interpreter moves. | MED | **DONE** — see "Step 2 outcome". Both divergences adopted. **Defect 2 refuted as written**: the `allAnyTarget` block was NOT deleted, and a new Step 2b owns that. |
| 2b | **Delete the `allAnyTarget` inference for real.** Blocked on making eight cards' effects declare honestly instead of using `PLAYER_OR_PERMANENT` as an unchecked escape hatch — see "Step 2 outcome" for the list, the two tiers, and the two mechanisms that must be replaced first (null-target tolerance, and the fact that validators are skipped for multi-target positions). Also covers the ability twin of the same inference (`ValidTargetService.isAnyTargetAbility`, read by `TargetLegalityService.validateMultiTargetAbility:391`), which must change in the same step or enumeration and cast-time validation will disagree. | HIGH | **DONE** — see "Step 2b outcome". Inference and its ability twin deleted; null tolerance now comes from `EffectResolution.distributesAmountsAmongTargets`, not from the target count. |
| 3 | Route `MayAbilityHandlerService` through the shared evaluator; delete both copies of the open-coded switch (defect 3). Add a regression test for a `LAND`-spec may-ability. | MED | **DONE** — see "Step 3 outcome". Defect 3 confirmed verbatim; both copies collapsed into one helper. No implemented card has a `LAND` may-ability, so that regression is a service test; `PLAYER_OR_PLANESWALKER` had a real card (Boggart Shenanigans) and got a card test. |
| 4 | Migrate the ~400 effect records' `targetSpec()` to the factories. Mechanical and scriptable; the Step 1 equivalence harness is the safety net. Batch by category, smallest first (`EXILE_CARD` 2, `CONTROLLERS_GRAVEYARD_CARD` 2, `LAND` 4, `PLAYER_OR_PLANESWALKER` 4 … `PLAYER` 155 last). | MED | **DONE** — see "Step 4 outcome". Batching was unnecessary (one scripted sweep, no import churn); `TargetSpec` now *stores* the declared target and `category()` became a bridge. |
| 5 | Collapse the three graveyard categories onto `GraveyardCards.scope`; delete the five hand-copied scope mappings. | LOW | **DONE** — see "Step 5 outcome". Not LOW and not five: there were **fourteen** copies and they disagreed, so six effects were declaring a scope they did not mean. Six rules-visible fixes. |
| 6 | Migrate the derived-boolean readers (`includesPermanents` / `includesPlayers` / `isGraveyard`) in the trigger collectors, `StepTriggerService`, AI, and `EffectResolution.collectTargetTypes`. ~30 call sites across engine + AI. | MED | **DONE** — see "Step 6 outcome". 152 call sites, not ~30, across 31 files; `TargetSpec.admits` was changed to read `declaredTarget()` so it stops allocating. |
| 7 | Delete `TargetCategory`; update `TargetSpecRatchetTest`, `scripts/targetspec-audit.py`, and the `EFFECTS_INDEX.md` category table. | LOW | **DONE** — see "Step 7 outcome". Genuinely LOW; the two `== ANY_TARGET` readers became `TargetSpec.declares(...)`, a new identity reader added for exactly that purpose. |
| 8 | **Optional, separate decision.** Add `PermanentIsBattlePredicate` and include battles in the `ANY_TARGET` factory per CR 115.4 (defect 4). Needs a rules review of the battle-damage path first — do not bundle into Step 7. | MED | TODO |

Steps 1-3 are independently valuable and can be shipped without 4-8. If the plan is abandoned
after Step 3, the codebase is strictly better than it is today and `TargetCategory` still exists.

---

## Step 1 outcome

What landed: `TargetPredicate` + `TargetPredicates` (domain, `model/effect/`),
`TargetPredicateEvaluationService` (engine, `service/target/`), a derived `TargetSpec.targetPredicate()`,
the corrected `TargetSpec` javadoc, and `TargetPredicateEquivalenceTest`
(`magical-vibes-application/.../service/target/`). No call site reads the new type yet.

**Every premise in this document was re-verified and held**, including the two hand-copied graveyard
scope mappings and `MayAbilityHandlerService`'s duplicated `default -> false` switch.

Three things a later step needs to know:

**1. Two deliberate divergences, both in the rules-correct direction.** They are pinned by their own
tests in `TargetPredicateEquivalenceTest` (named `KNOWN DIVERGENCE: …`) rather than hidden inside the
sweep, and they take effect the moment **Step 2** moves `validateSpec` onto `targetPredicate()`:

| Category | Today | Under the predicate |
|---|---|---|
| `LAND` | `target.getCard().hasType(LAND)` — the *printed* type | `gameQueryService.isLand` — layer-aware |
| `ANY_TARGET` / `CREATURE_OR_PLANESWALKER` | `target.getCard().hasType(PLANESWALKER)` — printed | `gameQueryService.isPlaneswalker` — layer-aware |

Only a type-**replacing** effect (Imprisoned in the Moon; `StaticBonus.cardTypeOverriding`) can tell
them apart — an *animated* land is a land under both, which is why the harness's animated-land case
stays an equality assertion. Both new answers are correct per CR 613.1d (verified: "Layer 4:
Type-changing effects are applied"): a creature turned into a land *is* a legal "target land", and a
planeswalker that stopped being one is *not* a legal "any target" (CR 115.4, verified). `CREATURE`
does **not** diverge — both halves already went through `gameQueryService.isCreature`. Step 2 should
adopt both and cover them with a card-level test, not paper over them.

**2. `StackEntryTruePredicate` is new.** `SPELL_ON_STACK` maps to `spells(TRUE)` and no true-predicate
existed for the stack hierarchy, unlike `PermanentTruePredicate` / `CardTruePredicate`. Added to the
sealed `permits`, to `PredicateEvaluationService.matchesStackEntryPredicate` (exhaustive switch,
`-> true`) and to `TargetLegalityService.matchesStackEntryPredicate` (the `if/instanceof` chain,
early `return true`). It is deliberately NOT admitted by `predicateAdmitsAbilityTarget`: "a spell on
the stack" does not include abilities. The alternative — a nullable inner predicate — was rejected
because `null` already means "matches nothing" for permanents and "matches everything" for cards.

**3. `TargetSpecRatchetTest` invariant 1 forbids a method named `targetPredicate` under
`model/effect/` — but only with return type `boolean|int|PermanentPredicate`.** `TargetSpec`'s new
`public TargetPredicate targetPredicate()` does not match and the guard still passes. Step 7 must
keep that in mind when it rewrites the ratchet: the deleted legacy `CardEffect.targetPredicate()`
returned `PermanentPredicate`, and the name is now legitimately in use for something else.

Also worth carrying forward: `TargetLegalityService.matchesPlayerPredicate` was made public (it is
the null-controller-safe copy; `ValidTargetService` still has a private duplicate that NPEs on a null
controller — Step 6 territory). And the zone-wide `canGraveyardCardsBeTargeted` gate (Ground Seal) is
*not* part of the predicate and must stay in `validateSpec`.

---

## Step 2 outcome

What landed: `TargetValidationService.validateSpec` is now one predicate interpretation instead of a
14-arm category switch; `ValidTargetService` reads `TargetSpec.admits(Kind)` everywhere it used to
read `includesPermanents()` / `includesPlayers()` / `isGraveyard()`, and its "any target" narrowing
now evaluates `TargetPredicates.anyTarget()` instead of an open-coded `isCreature || printed
PLANESWALKER`. Two small additions carry it: `TargetPredicate.permanentRestriction()` and
`TargetSpec.admits(Kind)`.

**Both Step 1 divergences adopted, in the layer-aware direction.** `validateSpec` no longer reads a
printed type line anywhere: "target land" accepts a permanent a type-*replacing* effect turned into
a land, and "any target" rejects a planeswalker that stopped being one (CR 613.1d and CR 115.4, both
verified). Covered behaviourally by `ImprisonedInTheMoonTest` (Field of Ruin destroys a moon'd
creature; Lightning Bolt can no longer be pointed at a moon'd Jace) and pinned in
`TargetPredicateEquivalenceTest`, whose two `KNOWN DIVERGENCE` tests are now equality tests.

Error messages are generated from the predicate rather than hard-coded per category. A conjunction
blames the first component that actually failed, so "target artifact creature" still reports "Target
must be a creature" for a land and keeps the generic wording for the artifact half. One wording
changed: `PLAYER_OR_PLANESWALKER` now reads "a planeswalker or player" (player last, matching
`ANY_TARGET`); no test asserted it.

### Defect 2 is refuted as written — the `allAnyTarget` block stays for now

The premise in "Why this is worth doing" is **stale on every particular**:

- It says `PLAYER_OR_PERMANENT` has **two** producers. It has **eighteen** effect records.
- It says `ClashEffect` computes it "e.g. Fire Juggler". `ClashEffect` never reaches
  `PLAYER_OR_PERMANENT` for any implemented card — all twelve filterless clash cards have
  single-category children. And Fire Juggler (MOR 90) is "4 damage to each creature blocking it",
  not an any-target burn spell.
- `DistributeCountersAmongTargetsEffect`'s own comment claims `CreatureModTargetValidators` enforces
  creature-only legality. It does not: that validator registers only `StaticBoostEffect` and
  `AttachedBoostEffect`, and no `@ValidatesTarget` for the distribute effect exists anywhere.

The real shape of the problem is the opposite of the one described. `PLAYER_OR_PERMANENT` is not a
*declaration* that any permanent is legal — it is an **unchecked escape hatch**, chosen precisely
because it is a no-op in the interpreter, and the `allAnyTarget` inference is what silently supplies
the narrowing those effects decline to declare. Deleting it widens targeting on eight real cards:

| Tier | Cards | What breaks |
|---|---|---|
| A | Stomping Slabs, Blessings of Nature, Bounty of the Hunt, Spoils of War | single-target, no validator → **every** permanent (lands, artifacts, enchantments) becomes a legal target |
| B | Arc Trail, Cone of Flame, Fireball, Jaya's Immolating Inferno | filterless multi-target → falls to the `isMultiTarget && !isCreature` branch, so planeswalkers stop being offered for an "any target" spell |

Every other filterless `PLAYER_OR_PERMANENT` card (Rock Slide, Remedy, Aurelia's Fury, Fire Covenant,
Flames of the Firebrand, Hail of Arrows, Infernal Harvest, Jaws of Stone, Meteor Shower, Pyrokinesis,
Pyrotechnics, Fight with Fire) is already covered by `DamageTargetValidators.validateDealDividedDamage`
or `PreventionTargetValidators.validatePreventDividedDamage`, so the inference is dead weight there.

> **Both mechanisms below turned out to be the wrong boundary — Step 2b replaced neither of them.
> Read "Step 2b outcome" for what actually blocked the deletion.** Kept for the record:

**Step 2b must therefore replace two mechanisms before it can delete anything:**

1. **Null-target tolerance.** These effects' per-target amounts ride on
   `StackEntry.damageAssignments`, so the `targetId` validated at cast time is null. Today that is
   bought by picking a category the interpreter no-ops on, which `validateSpec.demandsPermanentTarget`
   reproduces exactly: a predicate that accepts every player *and* every permanent restricts nothing,
   so it demands nothing. An honest `anyTarget()` predicate would demand a target and break the cast
   path. The honest fix is to move "a target is required" off the spec and onto the declared target
   count (`TargetLegalityService.checkAbilityTargeting:475` is the one caller that relies on the spec
   throwing; `checkSpellTargeting` already rejects a null target before the spec is consulted).
2. **Validators are skipped for multi-target positions** (`ValidTargetService.isValidPermanentTarget`
   runs `checkEffectTargets` only when `positionFilter == null && !isMultiTarget`), which is why
   Tier B has no second line of defence. Either give those four cards real
   `AnyTargetPredicateTargetFilter`s (Injury already does) or extend the validator pass to
   multi-target positions.

Until then the inference stays, but it is now honest about what it is: it *recognises* the slot with
the same lossy `admits(PLAYER)` test, and gets the restriction itself from `TargetPredicates.anyTarget()`.
The comment at the call site says so and points here. *(Step 2b has since deleted it.)*

### Two other things a later step needs

- **`TargetValidationService` deliberately does not use `TargetPredicateEvaluationService`.**
  Injecting it closes a Spring constructor cycle (`TargetValidationService` →
  `TargetPredicateEvaluationService` → `TargetLegalityService` → `TargetValidationService`), so it
  reads `permanentRestriction()` and calls `PredicateEvaluationService.matchesPermanentPredicate`
  itself — the same call the adapter makes. `ValidTargetService` does take the adapter; its 5-arg
  constructor is built by hand in `AiTargetSelector` and in `ValidTargetServiceTest`.
- **Mocked-`PredicateEvaluationService` tests now need a real one.** Both
  `TargetValidationServiceSpecTest` and `ValidTargetServiceTest` construct a genuine
  `PredicateEvaluationService` over their mocked `GameQueryService`, because the type restrictions
  the interpreter used to open-code are now real predicate evaluations. Any future test that mocks
  predicate evaluation and expects targeting to work will silently reject every candidate.

---

## Step 2b outcome

The inference is gone, in both its spell form (`ValidTargetService.isValidPermanentTarget`) and its
ability form (`ValidTargetService.isAnyTargetAbility`, deleted, plus its reader
`TargetLegalityService.validateMultiTargetAbility`). What replaced it:

**An unfiltered slot is restricted by what its effects declare.**
`EffectResolution.declaredPermanentRestriction(List<CardEffect>)` conjoins every permanent
restriction the slot's effects carry (skipping the ones that carry none — a bare `PERMANENT` spec
and `PLAYER_OR_PERMANENT` both yield `PermanentTruePredicate`), and
`EffectResolution.allowsPlayerTargets` answers the player half. Four call sites read them:
enumeration for spells and for multi-target abilities, and validation in `validateMultiSpellTargets`
and `validateMultiTargetAbility`. A slot no effect restricts keeps the pre-existing creature-only
default when it is multi-target — that default is a separate legacy heuristic, not the inference,
and Karn's Temporal Sundering's bare "target player" group is its only live user.

**Both premises in the Step 2 outcome were re-verified and both were wrong about the boundary.** The
blockers named there do not describe the code:

1. *"Move 'a target is required' onto the declared target count"* does **not work**. Every
   `ActivatedAbility` convenience and loyalty constructor hard-codes `minTargets = 1`, including for
   abilities that target nothing, so a count-based check would reject every non-targeting activation.
   (`Card` is the opposite: `getMinTargets()` sums the declared groups, so a card with no `target()`
   call is 0/0.) The honest discriminator is a property of the *effect*, not of the count: an effect
   that announces a division (CR 601.2d) keeps its targets in `StackEntry.damageAssignments`, so
   there is no `targetId` to judge. `EffectResolution.needsAmountDistribution` already modelled
   exactly that set; it is now public as `distributesAmountsAmongTargets` and
   `TargetValidationService.demandsPermanentTarget` reads it. The old shape-sniffing arm ("accepts
   every player and every permanent") is kept underneath it, so no existing tolerance was withdrawn.
2. *"Extend the validator pass to multi-target positions"* was **not needed**, and would have been
   wrong — the `@ValidatesTarget` escape hatches are not written for a per-position call. Only the
   declarative spec had to reach those positions.

Also refuted: the spell cast path never reaches `validateSpec` with a null `targetId` at all
(`SpellCastingService:1169` / `:3275` gate on `targetId != null`, and `:1158-1166` already exempts
`needsDamageDistribution` from "Spell requires a target"). The null tolerance only ever mattered on
the activated-ability path — Huatli, Warrior Poet's −X and Samut, the Tested's −2.

### The escape hatch is closed

Five effect records now declare what they target. The audit behind this is exhaustive: these are the
*only* `PLAYER_OR_PERMANENT` producers that ever sit on a slot with no `TargetFilter`; every other
one (fight / attach / move-counter / untap-two-targets families, and `ClashEffect`, which no
implemented card drives to `PLAYER_OR_PERMANENT`) is on a slot whose per-position filters carry the
restriction, so deleting the inference cannot widen them.

| Effect | Now declares |
|---|---|
| `DealDividedDamageEffect` | `ANY_TARGET` when `canTargetPlayers`, else `CREATURE`, narrowed by `targetRestriction` (which stopped being "informational") |
| `DistributeCountersAmongTargetsEffect` | `CREATURE` in both modes |
| `PreventDividedDamageEffect` | `ANY_TARGET` (Remedy's oracle text is "any number of targets") |
| `RevealTopCardsBottomThenDamageIfCopyRevealedEffect` | `ANY_TARGET` |
| `DealDamageToEachTargetEffect` | `harmful(ANY_TARGET)` — was `benign`, see below |

Behaviour changes, all rules-correct:

| Card(s) | Before | Now |
|---|---|---|
| Arc Trail, Cone of Flame, Fireball, Jaya's Immolating Inferno | enumeration offered a planeswalker but `validateMultiSpellTargets` then threw "is not a creature" | a planeswalker is legal (CR 115.4) |
| Fire Covenant, Infernal Harvest, Pyrokinesis, Blessings of Nature, Bounty of the Hunt, Spoils of War | players and planeswalkers offered for a creatures-only spell | creatures only |
| Hail of Arrows, Rock Slide | any creature/planeswalker + players offered | attacking (resp. attacking-or-blocking non-flying) creatures only |
| Huatli, Warrior Poet −X | every permanent + players offered | creatures only |
| Samut, the Tested −2 | every permanent + players offered | creature/planeswalker + players |
| Chandra, the Firebrand −6 | protection from the source was not checked (`isValidAbilityPermanentTarget` keys that on `harmful`) | protection blocks targeting (CR 702.16b) |

The `benign` → `harmful` flip on `DealDamageToEachTargetEffect` is in scope because it is the same
defect: an effect that deals damage was declaring the opposite of what it does. On the spell path it
changes nothing (protection is checked structurally there regardless); on the ability path it fixes
Chandra's −6.

Pinned by three card tests confirmed to fail against a restored copy of the old code —
`ArcTrailTest` (a planeswalker is enumerated *and* castable; a land is rejected with the new
predicate-derived message) and `BlessingsOfNatureTest` (enumeration offers creatures only, no
planeswalker, no player) — plus `TargetLegalityServiceTest`, which now also covers the legacy
creature-only default explicitly (a slot whose only effect is `UntapPermanentsEffect(ALL_TARGETS)`).

### Two things a later step should know

- **A multi-target ability's `damageAssignments` are never validated.** `SpellCastingService` has
  ~200 lines validating sums, per-target legality and positivity for spells (`:1764-1957`); the
  ability path threads the map straight from `AbilityActivationService` into the stack entry
  (`ActivatedAbilityExecutionService:1233`) with no checks at all. Huatli's −X and Samut's −2 are
  affected. Out of scope here — it is a cast/activation-parity gap, not a targeting-model one.
- **Karn's Temporal Sundering enumerates permanents for its "target player" group.** Position 0 is a
  bare unfiltered group bound only to `ExtraTurnEffect`, but enumeration passes *all* spell effects
  to the slot check, so `ReturnToHandEffect`'s `PERMANENT` spec keeps permanents in play there and
  the legacy creature-only default narrows them to creatures. The fix is per-position effect scoping
  (`Card.getEffectTargetIndex` already binds effects to groups, and `doesPositionAllowPlayerTargets`
  already uses it for the player half). Deliberately left alone: it is pre-existing and orthogonal.

### Test-harness note (third time this has bitten)

`TargetLegalityServiceTest` mocks `PredicateEvaluationService`, and the unfiltered-slot narrowing is
a real predicate evaluation, so a mocked evaluator silently rejects every candidate. Its `setUp` now
delegates `matchesPermanentPredicate` to a genuine evaluator over the same mocked `GameQueryService`
and answers `isCreature` from the card type; `ValidTargetServiceTest` got the `isCreature` default
too. Any future test that mocks predicate evaluation and expects targeting to work will fail the
same way.

---

## Step 3 outcome

**Defect 3 held exactly as written.** Both switches were still there verbatim (lines 387-394 and
795-802), both with `default -> false` swallowing `LAND` and `PLAYER_OR_PLANESWALKER`, and both
reading the *printed* planeswalker type line.

They are now one private helper, `MayAbilityHandlerService.mayAbilityPermanentTargets`, shared by the
accept path (`handleTargetedMayAbilityAccepted`) and the CR 603.5 resolution-time path
(`handleResolutionTimeTargetSelection`). The service takes `TargetPredicateEvaluationService` as a new
constructor dependency — no cycle risk, since it already depended on `ValidTargetService`, which takes
the same adapter.

**The three-way precedence is unchanged**, and it is worth writing down because it is not what a
reader expects: a card-level `TargetFilter` or an effect-level `PermanentPredicate`
(`EffectResolution.targetPredicateOf`) *replaces* the spec's type restriction rather than stacking
with it. Only when the ability declares neither does the spec restrict anything — and that is the arm
that now evaluates `targetSpec().targetPredicate()` through the shared evaluator. Documented in
`TRIGGER_SLOT_TARGETING.md` under "May-ability target enumeration".

Behaviour changes, both in the rules-correct direction and both pinned by a failing-before test:

| Spec | Before | Now |
|---|---|---|
| `LAND` | no legal permanent at all | layer-aware `isLand` |
| `PLAYER_OR_PLANESWALKER` | no legal permanent at all (players only) | layer-aware `isPlaneswalker` |
| `ANY_TARGET` / `CREATURE_OR_PLANESWALKER` | `isCreature \|\| printed PLANESWALKER` | `isCreature \|\| isPlaneswalker` (CR 613.1d, verified) |

`CREATURE`, `PERMANENT` and `PLAYER_OR_PERMANENT` are unchanged.

**No implemented card has a `LAND`-spec may-ability** (checked: the four `LAND` producers are
`DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect`, `GrantBasicLandTypeToTargetEffect`,
`TargetLandBecomesForestUntilSourceLeavesEffect`, `DestroyAttachmentsOnTargetCreatureEffect(…, LAND)`,
and no card combines one with a `MayEffect`). That regression therefore lives in a new
`MayAbilityHandlerServiceTest` (`service/input/`), which builds the service by hand over a **real**
`PredicateEvaluationService` + `TargetPredicateEvaluationService` — the Step 2 note applies here too,
a mocked predicate evaluator silently rejects every candidate. `PLAYER_OR_PLANESWALKER` *did* have a
real card: `BoggartShenanigansTest` now covers a planeswalker being offered and losing a loyalty
counter, plus a creature staying illegal. Both new tests were confirmed to fail against a restored
copy of the old switch.

Two things a later step should know:

- **The prompt label is still hard-coded and now visibly wrong for the widened cases.**
  `handleTargetedMayAbilityAccepted` ends with `targetDescription = … else "creature"`, so a `LAND`
  may-ability would prompt "Choose target creature." Step 2 built real predicate-derived wording in
  `TargetValidationService.rejectionMessage`, but it is `private` and reusing it would add a
  `MayAbilityHandlerService → TargetValidationService` edge. Left alone deliberately; it is
  pre-existing (a bare `PERMANENT` spec said "creature" too), not introduced here.
- **`MayAbilityHandlerService` still has legacy category reads** (`includesPermanents()` /
  `includesPlayers()` / `isGraveyard()`): the `isTargeted*Effect` triples at the top of
  `handleMayAbilityChosen` and in `handleResolutionTimeMayChoice`, and the two `canTargetPermanent` /
  `canTargetPlayer` guards around the new helper. Step 6 territory. *(Step 6 migrated all eight onto
  `admits(Kind)`.)* *(The graveyard-scope
  comparisons that were duplicated across `handleGraveyardTargetedMayAbility` and
  `handleResolutionTimeGraveyardTargetSelection` are gone — Step 5 collapsed both loops into one
  `graveyardTargetOf` helper.)*

---

## Step 4 outcome

The direction of the dependency is now flipped: `TargetSpec` **stores** a `TargetPredicate` and
`TargetCategory` is derived from it, not the other way round. Every one of the ~475 spec construction
sites (411 effect records, 12 card classes, `CounterAbilityAndLockSourceEffectHandler`, six test
classes) names a `TargetPredicates` factory. The 14 production files that still name a
`TargetCategory` constant are all *readers* — the graveyard-scope comparisons Step 5 collapses and
the derived-boolean / identity-comparison sites Step 6 migrates. Nothing *declares* a category.

**`TargetSpec`'s shape.** `TargetCategory category` became `TargetPredicate declaredTarget`
(`null` = targets nothing, replacing `TargetCategory.NONE`); `harmful`, `predicate`, `selfTargeting`
and `playerTargetCount` are untouched, and `targetPredicate()` still derives
`narrowPermanents(declaredTarget, predicate)` exactly as before. `TargetSpec.NONE` is
`new TargetSpec(null, false, null, false, 1)`.

**`predicate` deliberately stays a separate component rather than being folded into
`declaredTarget`.** Two readers need the narrowing *on its own*: `EffectResolution.targetPredicateOf`
and `PermanentCounterSupport`. Folding would change what they see — `targetPredicateOf` on a bare
`benign(creature())` would start returning `PermanentIsCreaturePredicate` where it returns `null`
today, and on `benign(creature(), artifact)` it would return the conjunction where it returns the
artifact half. That is arguably the *more* correct answer (Step 3 documented that an effect-level
predicate **replaces** the spec's type restriction in may-ability enumeration rather than stacking
with it), but it is a behaviour change and belongs to whoever revisits that precedence, not here.

**`category()` is a lookup, not a structural match — and it throws.** `TargetPredicates.categoryOf`
inverts `forCategory` through a `Map` built from `TargetCategory.values()`, so a declared target no
category can express (say `anyOf(player(), permanents(isArtifact))`) fails loudly instead of being
rounded to the nearest constant and silently widening or narrowing a card. Nothing constructs such a
target today; the throw is the tripwire that keeps that true until Step 7 deletes the bridge. Pinned
by three new tests in `TargetPredicateEquivalenceTest` (round-trip of all 14, narrowing does not
disturb the reported category, unmappable target throws).

The 14 canonical values are now interned `private static final`s behind the factories. `targetSpec()`
is rebuilt on every call and enumeration calls it in a loop, so the factories must not allocate.

**The ratchet had to move early.** "Risk hotspots" scheduled `TargetSpecRatchetTest` +
`scripts/targetspec-audit.py` for Step 7, but their invariant-2 detection recognises a non-NONE spec
by `benign(`/`harmful(` **or** a non-`NONE` `TargetCategory.X` — and `DealDividedDamageEffect` (a
`@ValidatesTarget` effect) declares its spec through the canonical constructor, so it stopped
matching the moment the enum left its body. Both files learned `\bTargetPredicates\.\w+\s*\(` in
lockstep. The remaining Step 7 work there is deleting the now-dead `TargetCategory` arm.

**No behaviour change.** No rules-visible arm moved; the whole step is a change of vocabulary.
`ClashEffect`, `FlipCoinWinEffect` and the four effects that carry the target as a record component
(`RedirectNextDamageEffect`, `DestroyAttachmentsOnTargetCreatureEffect`,
`GrantProtectionFromColorUntilEndOfTurnEffect`, `LockTargetPermanentEffect` — the four flagged under
"Do not regress these") were migrated by hand; the other 405 effect files were a scripted
constant→factory rewrite with no import churn, since `model/effect/` is the package both types live
in. `FlipCoinWinEffect` now asks `declaredTarget() != null` where it compared against
`TargetCategory.NONE`.

Two things a later step needs:

- **`SequenceEffect` compares `spec != TargetSpec.NONE` by identity**, and a self-targeting effect
  returns `new TargetSpec(null, false, null, true, 1)` — equal to `NONE` but not identical, which is
  what makes that comparison mean "declares something". Pre-existing and load-bearing; do not
  "simplify" it to `declaredTarget() == null`.
- **`ClashEffect` still reads `category().includesPermanents()` / `includesPlayers()` on its child
  effects** to compute its own spec. That is a Step 6 reader like any other, but it is the only one
  inside `model/effect/`, so a grep scoped to the engine will miss it. *(Step 6 found it and collapsed
  the four reads into one `admitsAny(Kind)` helper.)*

---

## Step 5 outcome

`TargetPredicates` has one graveyard factory now — `graveyardCard(GraveyardSearchScope)` — instead of
three named ones, and the scope is read back through `TargetPredicate.graveyardScope()` /
`TargetSpec.graveyardScope()`. The expansion "which players' graveyards does this search" is a single
method, `GraveyardSearchScope.graveyardOwners(orderedPlayerIds, controllerId)`, on the domain enum.

### The premise was wrong in two ways, and the second one is the whole step

**There were fourteen copies, not five**, spread over `GraveyardTargetingSupport`, `ValidTargetService`
(×3), `SpellCastingService`, `TriggeredAbilityQueueService` (×3, including an `isInScope` helper the
plan never mentioned), `GraveyardTargetingService` (×4), `MayAbilityHandlerService` (×2, the two
duplicated `handleGraveyard*` loops), `AiTargetSelector` and `GameSimulator`. All are gone.

**And they did not agree with each other.** Six of them expanded "not `ANY_GRAVEYARD_CARD`" to the
**controller's** graveyard; the other eight expanded it to **opponents'** graveyards — while
`TargetCategory.GRAVEYARD_CARD` documents itself as opponent-scoped. A mechanical collapse onto the
leaf's scope would therefore have silently broken every card on the controller's-graveyard side. The
step could only be done by first making each effect declare the scope its oracle text names:

| Effect | Was | Now |
|---|---|---|
| `ReturnCardFromGraveyardEffect` | `OPPONENT_GRAVEYARD` for all 216 construction sites | its own `source()` component (191 sites `CONTROLLERS_GRAVEYARD`, 25 `ALL_GRAVEYARDS`, 0 opponent) |
| `CastTargetInstantOrSorceryFromGraveyardEffect` | `OPPONENT_GRAVEYARD` always | its own `scope()` component |
| `ExileTargetCardFromGraveyardAndImprintOnSourceEffect` | `OPPONENT_GRAVEYARD` always | a new `scope` record component — Myr Welder is "from a graveyard", Rona is "from your graveyard" (both verified on Scryfall), so one constant could not serve both |
| `ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect` | `ownGraveyardOnly` → `OPPONENT_GRAVEYARD` | `ownGraveyardOnly` → `CONTROLLERS_GRAVEYARD` |
| `ExileTargetCardFromGraveyardAndCreateTokenCopyEffect` | `ownGraveyardOnly` → `OPPONENT_GRAVEYARD` | `ownGraveyardOnly` → `CONTROLLERS_GRAVEYARD` |
| `ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect` | `ALL_GRAVEYARDS` | `OPPONENT_GRAVEYARD` |

The three `// reproduces the legacy (graveyard=T, any=F) booleans exactly` comments that justified the
inverted declarations are deleted with them. The pattern they encoded — declare a wrong scope, let a
`@ValidatesTarget` validator carry the real one — is now called out as forbidden in
`EFFECTS_INDEX.md`.

### Behaviour changes, all rules-correct

| Card(s) | Before | Now |
|---|---|---|
| Myr Welder | imprint enumerated opponents' graveyards only (its validator has no scope check, so your own artifacts were simply unreachable) | "from a graveyard" — both graveyards |
| Snapcaster Mage, Flashback, Horde of Notions | enumeration offered opponents' graveyards; the validator then rejected the pick | the controller's own graveyard |
| Coffin Queen, Nomad Mythmaker, Liliana Vess −8, Grazing Kelpie, Nighteyes, Grimoire of the Dead, and every other `ReturnCardFromGraveyardEffect` **activated ability** | `computeValidGraveyardTargetsForAbility` offered opponents' graveyards regardless of `source()` | the declared `source()` |
| Nita, Forum Conciliator | enumerated every graveyard, validator rejected your own | opponents' graveyards only |
| Séance, Practiced Scrollsmith | unchanged (the readers on those paths already used the controller's graveyard) | unchanged, now honestly declared |
| A `CONTROLLERS_GRAVEYARD`-scoped prompt | said "an opponent's graveyard" / "a graveyard" | says "your graveyard" |

Pinned by two card tests confirmed to fail against a restored copy of the old declaration —
`MyrWelderTest.offersArtifactsFromEitherGraveyard` and
`CoffinQueenTest.offersCreatureCardsFromEitherGraveyard`, both asserting through
`ValidTargetService.computeValidTargetsForAbility` — plus `ValidTargetServiceTest` (a controller-scoped
spell effect reaches the controller's graveyard and no opponent's) and `AiTargetSelectorTest`.

### Three things a later step should know

- **Enumeration for an ability does not apply `ReturnCardFromGraveyardEffect.filter()`.**
  `ValidTargetService.matchesGraveyardEffectTypeFilter` has no arm for it, so Coffin Queen's
  activated ability offers noncreature cards too; the `@ValidatesTarget` validator rejects them on
  activation. Pre-existing and orthogonal to scope — the Coffin Queen test deliberately seeds only
  creature cards rather than asserting the gap.
- **`PutTargetCardsFromGraveyardOnTopOfLibraryEffect` declares `TargetSpec.NONE`** and keeps its scope
  in its own `source` component; its cards (Lodestone Bauble, Misinformation) target through a
  card-level `GraveyardCardPredicateTargetFilter`. Giving it a declared target is a behaviour change
  beyond this step.
- **`GraveyardTargetingService.handleBeginningOfCombatGraveyardTargeting` has a dead scope branch.**
  `StepTriggerService:3553` only ever hands it a `TARGET_CARDS_ANY_GRAVEYARD` effect
  (`.orElseThrow()` on that filter), so its non-`ALL_GRAVEYARDS` path was and is unreachable.

---

## Step 6 outcome

Every derived-boolean reader is gone. `TargetSpec.admits(TargetPredicate.Kind)` is now the single way
to ask "which kinds can this spec target", across 154 call sites in 31 files — not the ~30 the Step
Index guessed. The three booleans map onto the five kinds exactly:
`includesPermanents()` → `admits(PERMANENT)`, `includesPlayers()` → `admits(PLAYER)`,
`isGraveyard()` → `admits(GRAVEYARD_CARD)`.

**No behaviour change, and the equivalence is structural rather than case-by-case.** Two facts carry
it, both re-verified against the code rather than taken from this document:

1. Each of the fourteen categories admits exactly the kinds its booleans reported — the graveyard
   trio admits only `GRAVEYARD_CARD`, `SPELL_ON_STACK` and `EXILE_CARD` admit neither permanents nor
   players, and `PLAYER_OR_PERMANENT` / `PLAYER_OR_PLANESWALKER` / `ANY_TARGET` admit both.
2. `TargetPredicates.narrowPermanents` cannot add or remove a kind: it returns its base untouched
   unless the base has a permanent leaf, and otherwise swaps that leaf for another permanent leaf.

Both are pinned by two new sweeps in `TargetPredicateEquivalenceTest`
(`admitsMatchesTheLegacyDerivedBooleans`, `narrowingDoesNotDisturbWhichKindsAreAdmitted`), which
compare `admits(Kind)` against the legacy booleans for every category, bare and narrowed.

### `admits` stopped allocating

It read `targetPredicate()`, which rebuilds the composed predicate on every call — for a narrowed
`AnyOf` that means an `EnumSet`, an `ArrayList`, a sort and a `List.copyOf` per question. Fact 2
above says the narrowing is irrelevant to the answer, so `admits` and `graveyardScope()` now read
`declaredTarget()` directly. That matters because these readers sit in per-effect loops in the
trigger collectors, `StepTriggerService` and target enumeration — the step would otherwise have made
those loops measurably more expensive than the enum lookup they replaced.

### Two arms migrated beyond the literal three booleans

`EffectResolution.collectTargetTypes` and `AiTargetSelector.computeBaseAllowedTargets` each ended in
`category == TargetCategory.EXILE_CARD`. `EXILE_CARD` is the only category with an exiled leaf, so
that is `admits(EXILED_CARD)` — the same question, spelled as an identity comparison only because the
enum never grew an `isExile()`. Both blocks were being rewritten anyway and keeping one enum read in
each would have left the import alive for no reason.

### What Step 7 inherits

Six production readers still name the enum, all genuine identity comparisons, all needing a decision
rather than a rename:

| Site | Comparison | Note |
|---|---|---|
| `EffectResolution.targetsSpellOnStack:214` | `== SPELL_ON_STACK` | `admits(Kind.SPELL)`, but the four `instanceof` special cases beside it are the "Risk hotspots" entry |
| `ActivatedAbility:446` | `allMatch(== SPELL_ON_STACK)` | same |
| `DrawService:738` | `== ANY_TARGET` | must NOT become `admits(PLAYER) && admits(PERMANENT)` — that is the lossy test the whole plan exists to kill; compare against `TargetPredicates.anyTarget()` |
| `TriggerTargetCollector:178` | `allMatch(== ANY_TARGET)` | same |
| `TriggerCollectionService:1389` | `== NONE` | `declaredTarget() == null` |
| `PermanentCounterSupport:507` | `!= NONE` | `declaredTarget() != null`, as `FlipCoinWinEffect` already does |

**One lossy shape-sniff survives and is not a Step 7 rename.** `StepTriggerService:448` recognises an
"any target" upkeep trigger as `admits(PLAYER) && admits(PERMANENT)` and names the result
`hasAnyTarget`. That is exactly the inference Step 2b deleted from `ValidTargetService`, still alive
on the upkeep path: it cannot tell `anyTarget()` from `playerOrPermanent()`. It is a *routing*
decision (which pending-interaction record to queue) rather than a narrowing, so nothing is currently
mis-targeted by it — `TriggerTargetCollector` applies the real narrowing afterwards, via the
`== ANY_TARGET` comparison two rows above. Deliberately left alone; whoever fixes the
`TriggerTargetCollector` row should fix this one in the same breath, because the two must agree.

---

## Step 7 outcome

`TargetCategory` is deleted, together with the `TargetSpec.category()` bridge and the
`TargetPredicates.forCategory` / `categoryOf` pair that backed it. Every premise held: the six
readers the Step 6 outcome listed were still there verbatim, and no seventh had appeared.

### The six readers

| Site | Was | Now |
|---|---|---|
| `EffectResolution.targetsSpellOnStack` | `== SPELL_ON_STACK` | `admits(Kind.SPELL)` |
| `ActivatedAbility.isSpellOnlyTarget` | `allMatch(== SPELL_ON_STACK)` | `allMatch(admits(Kind.SPELL))` |
| `DrawService:738` | `== ANY_TARGET` | `declares(TargetPredicates.anyTarget())` |
| `TriggerTargetCollector:178` | `allMatch(== ANY_TARGET)` | `allMatch(declares(TargetPredicates.anyTarget()))` |
| `TriggerCollectionService:1389` | `== NONE` | `declaredTarget() == null` |
| `PermanentCounterSupport:507` | `!= NONE` | `declaredTarget() != null` |

**`TargetSpec.declares(TargetPredicate)` is new** — an identity test against one interned factory
value, ignoring the `predicate()` narrowing exactly as `category()` did. It exists so the two
`ANY_TARGET` rows have a spelling that is *not* `admits(PLAYER) && admits(PERMANENT)`; its javadoc
says so, and `TargetPredicateEquivalenceTest.declaresDistinguishesWhatAdmitsCannot` pins it by
showing both specs answer the `admits` pair identically while `declares` separates them.

**The two `SPELL_ON_STACK` rows deliberately did NOT become `!admits(PERMANENT)`**, even though
`isSpellOnlyTarget`'s own javadoc ("no effect offers a permanent target as an alternative") reads
that way. The set those two see is `{declares spellOnStack()} ∪ {the four `instanceof` duals}`, and
`GrantColorUntilEndOfTurnEffect` with `scope == TARGET_PLAYERS_CREATURES` declares `player()` — so
`!admits(PERMANENT)` would call it spell-only where the enum did not. `admits(SPELL)` is exactly
equivalent for every effect in the set. Checked all four duals' specs individually; none declares
`NONE`, which is the other way the two spellings could have diverged.

### The equivalence test kept its name but changed what it proves

`TargetPredicateEquivalenceTest` was built to compare the predicate against the enum, so with the
enum gone every sweep would have been tautological. It now guards two things that survive the enum:

1. **The two evaluation paths agree.** `TargetPredicateEvaluationService` (enumeration) and the spec
   interpreter inside `TargetValidationService` are separate implementations of one restriction —
   the Step 2 outcome records *why* (injecting the adapter closes a Spring constructor cycle). Every
   canonical declared target is driven through both against the same board.
2. **Each factory admits exactly the kinds it claims**, as an explicit `ADMITTED_KINDS` table rather
   than something derived. That table plus `CANONICAL_TARGETS` is now the enumerable list of declared
   targets the enum used to be; a new factory must be added to both, and the test asserts the two
   key sets match so a half-added factory fails.

Deleted with the bridge: the round-trip test, the "narrowing does not disturb the reported category"
test (kept as `narrowingStaysOffTheDeclaredTarget`, asserting `declaredTarget()` instead), and
`anUnmappableDeclaredTargetThrows`. That last one is replaced by its inverse,
`aHandComposedCrossKindTargetIsEvaluatedByBothPaths`: a target no factory produces
(`anyOf(player(), permanents(isArtifact))`) is now a legal thing to build and both paths handle it.
The Step 4 throw was a migration tripwire, not a rule.

### Ratchet and audit script

Both lost only their `\bTargetCategory\.(\w+)` arm of invariant-2 detection — `benign(`/`harmful(`
and `\bTargetPredicates\.\w+\s*\(` already covered every effect, as Step 4 arranged when it moved
these files early. Still in lockstep; the script reports both invariants OK over 35 `@ValidatesTarget`
effects. One note added to both: invariant 1's `boolean|int|PermanentPredicate` return-type
alternation is load-bearing for the `targetPredicate` entry, because `TargetSpec.targetPredicate()`
legitimately uses that name for a `TargetPredicate`-returning method. Widening the alternation would
turn the guard into a false positive.

### What Step 8 inherits

- **The lossy shape-sniff at `StepTriggerService:448` is still there**, unchanged and still paired
  with `TriggerTargetCollector`'s narrowing. The Step 6 outcome asked that whoever fixes one fixes
  both; this step only changed *how* the collector spells its comparison
  (`declares(anyTarget())`), not the fact that the upkeep router cannot tell `anyTarget()` from
  `playerOrPermanent()`. Nothing is mis-targeted today because the collector applies the real
  narrowing afterwards.
- **The `EffectResolution.targetsSpellOnStack` `instanceof` list is untouched** — it is the "Risk
  hotspots" entry, and turning those four effects into honest `AnyOf(spells(...), permanents(...))`
  is its own change. Note that when it happens, `ActivatedAbility.isSpellOnlyTarget` must flip to
  `!admits(PERMANENT)` in the same commit: an honest dual would admit `SPELL`, and `admits(SPELL)`
  would then wrongly call it spell-only.
- **No behaviour change anywhere in this step.** Confirmed by `TargetPredicateEquivalenceTest`,
  `TargetSpecRatchetTest`, `MayAbilityHandlerServiceTest`, `ValidTargetServiceTest`,
  `TargetLegalityServiceTest`, `TargetValidationServiceSpecTest`, `EffectResolutionServiceTest`, and
  the card tests covering each migrated reader (Niv-Mizzet the Firemind / Dracogenius for
  `DrawService`, Flameblast Dragon + Form of the Dragon for `TriggerTargetCollector`, Livewire Lash
  for `TriggerCollectionService`, Defiant Greatmaw for `PermanentCounterSupport`, Spiketail Hatchling
  + Eight-and-a-Half-Tails for `ActivatedAbility`, Glamerdye + Magical Hack for the duals, plus Arc
  Trail and Boggart Shenanigans).

---

## Do not regress these

- `TargetSpec.harmful` drives `checkProtection` (CR 702.16b). `ExileTargetPermanentAndImprintEffect`
  is `harmful`, not `benign` — this was fixed in commit `9b8147333`.
- `RedirectNextDamageEffect` derives harmfulness from `destinationRole == TARGET` (CR 702.16b), set
  in commit `9933614c2`. Its declared target is a genuine record component; so is
  `DestroyAttachmentsOnTargetCreatureEffect`'s, `GrantProtectionFromColorUntilEndOfTurnEffect`'s,
  and `LockTargetPermanentEffect`'s. Step 4 migrated all four from `TargetCategory targetCategory`
  to `TargetPredicate declaredTarget`, together with the card classes that construct them (`StreetSweeper`, `EightAndAHalfTails`, `ArchonOfTheTriumvirate`,
  `LyevSkyknight`, `NewPrahvGuildmage`, `Martyrdom`, `DaughterOfAutumn`, `HazduhrTheAbbot`,
  `VassalsDuty`, `ZhalfirinCrusader`, `ZealousInquisitor`, `PersonalIncarnation`) and from
  `CounterAbilityAndLockSourceEffectHandler:68`.
- `PermanentIsCreaturePredicate` and `PermanentIsLandPredicate` are layer-aware **only when
  `gameData` is non-null** (`PredicateEvaluationService:382-398` delegates to
  `gameQueryService.isCreature` / `isLand`; the `gameData == null` branch falls back to raw card
  types plus animation flags). `TargetValidationService.sourceFilterContext` always supplies
  `gameData`, so `CREATURE` → `permanents(isCreature)` is equivalent there — but any new call site
  that evaluates without a `FilterContext` will silently mis-handle animated lands. Assert
  non-null `gameData` on the targeting path.
- `matchesPermanentPredicate` treats a `null` predicate as **`false`**, while card predicates and
  static filters treat `null` as **`true`**. Do not "normalize" this during migration.

---

## Next-step prompt template

After finishing a step, print the next step's prompt in a copy-pasteable block, in this form:

```
Read agent-docs/TARGET_PREDICATE_PLAN.md and execute Step <N> — <title>.

Follow the protocol in that file: verify the premises yourself before editing, keep behavior
identical unless the step says otherwise, respect the "Risk hotspots" and "Do not regress these"
sections, verify any CR number with the rules MCP, and update the affected agent-docs. Run only
the affected test classes — never the full suite. Then mark the step DONE in the plan file and
print the prompt for the next step.

Do not commit.
```
