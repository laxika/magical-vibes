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

> **Status: latent, NOT a confirmed shipped bug.** Both producers of `PLAYER_OR_PERMANENT` were
> checked. `DistributeCountersAmongTargetsEffect` selects it deliberately as a documented
> null-tolerance escape hatch (its own comment says "PLAYER_OR_PERMANENT is a no-op in the spec
> interpreter"), and creature-only legality is enforced downstream by
> `CreatureModTargetValidators` plus the cast-time assignment loop in `SpellCastingService`.
> `ClashEffect` only computes `PLAYER_OR_PERMANENT` when its child effects target both kinds — e.g.
> Fire Juggler ("creature or player"), where narrowing to creature/planeswalker is correct.
> **Step 2 must confirm or refute this against real cards before claiming a fix.**

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
| `GRAVEYARD_CARD` | `graveyardCards(TRUE, OPPONENT_GRAVEYARD)` |
| `ANY_GRAVEYARD_CARD` | `graveyardCards(TRUE, ALL_GRAVEYARDS)` |
| `CONTROLLERS_GRAVEYARD_CARD` | `graveyardCards(TRUE, CONTROLLERS_GRAVEYARD)` |
| `EXILE_CARD` | `exiledCards(TRUE)` |

Two wins fall out of the table itself: `ANY_TARGET` and `PLAYER_OR_PERMANENT` become structurally
distinct (defect 2), and the three graveyard constants collapse onto the `GraveyardSearchScope`
enum that `GraveyardCardPredicateTargetFilter` already carries — deleting the hand-copied mapping
in `GraveyardTargetingSupport:37`, `ValidTargetService:895`, `AiTargetSelector:740`,
`SpellCastingService:1148`, and `TriggeredAbilityQueueService:236`.

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
| `TargetSpecRatchetTest` + `scripts/targetspec-audit.py` | Both regex for `\bTargetCategory\.(\w+)`. They will silently stop matching once the enum is gone, turning a guard into a no-op. Update them in the same step that deletes the enum, not later. |

---

## Step Index

Phase 1 is additive and reversible. Phase 2 changes behavior at two known points. Phase 3 is
mechanical high-churn. Phase 4 is deletion.

| # | Step | Risk | Status |
|---|---|---|---|
| 1 | Introduce `TargetPredicate` + `TargetPredicates` factories + adapter evaluator. `TargetSpec` gains a derived `targetPredicate()` computed from the existing `category`+`predicate`; **no call site changes**. Fix the stale Jackson javadoc. Add the equivalence harness: for each of the 14 factories, assert it accepts/rejects exactly the candidate set the category does. | LOW | TODO |
| 2 | Move `TargetValidationService.validateSpec` and `ValidTargetService` enumeration onto `targetPredicate()`. Delete the inferred `allAnyTarget` block. **Confirm or refute defect 2** against Fire Juggler, Spoils of War, Blessings of Nature, Contagion before claiming a fix; record the finding here. | MED | TODO |
| 3 | Route `MayAbilityHandlerService` through the shared evaluator; delete both copies of the open-coded switch (defect 3). Add a regression test for a `LAND`-spec may-ability. | MED | TODO |
| 4 | Migrate the ~400 effect records' `targetSpec()` to the factories. Mechanical and scriptable; the Step 1 equivalence harness is the safety net. Batch by category, smallest first (`EXILE_CARD` 2, `CONTROLLERS_GRAVEYARD_CARD` 2, `LAND` 4, `PLAYER_OR_PLANESWALKER` 4 … `PLAYER` 155 last). | MED | TODO |
| 5 | Collapse the three graveyard categories onto `GraveyardCards.scope`; delete the five hand-copied scope mappings. | LOW | TODO |
| 6 | Migrate the derived-boolean readers (`includesPermanents` / `includesPlayers` / `isGraveyard`) in the trigger collectors, `StepTriggerService`, AI, and `EffectResolution.collectTargetTypes`. ~30 call sites across engine + AI. | MED | TODO |
| 7 | Delete `TargetCategory`; update `TargetSpecRatchetTest`, `scripts/targetspec-audit.py`, and the `EFFECTS_INDEX.md` category table. | LOW | TODO |
| 8 | **Optional, separate decision.** Add `PermanentIsBattlePredicate` and include battles in the `ANY_TARGET` factory per CR 115.4 (defect 4). Needs a rules review of the battle-damage path first — do not bundle into Step 7. | MED | TODO |

Steps 1-3 are independently valuable and can be shipped without 4-8. If the plan is abandoned
after Step 3, the codebase is strictly better than it is today and `TargetCategory` still exists.

---

## Do not regress these

- `TargetSpec.harmful` drives `checkProtection` (CR 702.16b). `ExileTargetPermanentAndImprintEffect`
  is `harmful`, not `benign` — this was fixed in commit `9b8147333`.
- `RedirectNextDamageEffect` derives harmfulness from `destinationRole == TARGET` (CR 702.16b), set
  in commit `9933614c2`. Its `TargetCategory` is a genuine record component; so is
  `DestroyAttachmentsOnTargetCreatureEffect`'s, `GrantProtectionFromColorUntilEndOfTurnEffect`'s,
  and `LockTargetPermanentEffect`'s. Those four need their component migrated too, and they are
  constructed from card classes (`StreetSweeper`, `EightAndAHalfTails`, `ArchonOfTheTriumvirate`,
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
