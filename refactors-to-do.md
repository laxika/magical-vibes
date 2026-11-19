# Refactors to do

Refactor opportunities spotted during other work, deferred so they don't bloat unrelated changes.
Each entry describes the problem, why it matters, the proposed direction, and a ready-to-use prompt
that kicks off the refactor.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once its refactor has landed.

---

## 1. Unify library-search bookkeeping behind one choke point

**Status:** open · **Found:** 2026-10-10, while fixing the Druid of the Emerald Grove hang

**Problem.** `LibrarySearchTriggerHelper.recordSearchAndQueueTriggers` (records the search in
`playersWhoSearchedLibraryThisTurn` and queues "whenever ... searches a library" triggers) is
called from ~37 sites across ~24 engine files. The class Javadoc claims a "unified library-search
choke point", but there isn't one: the shared `LibrarySearchSupport` flow handles the normal case,
while many handlers have early exits (empty library, no matching cards, search prevented, "up to
X" with X = 0) and bespoke per-card handlers (Intuition, Transmute, Emergent Ultimatum, Turtles
Forever, ...) that must each remember to record the search, fire triggers, and shuffle.

**Why it matters.**
- Missed bookkeeping on any exit path silently breaks search-trigger cards and "searched this
  turn" conditions. Concrete example: `SearchLibraryForUpToTwoBasicLandsThenRollD20EffectHandler`'s
  no-match branch logged "Library is shuffled" but never recorded the search, fired triggers, or
  shuffled (and its stale-index insert caused an infinite loop).
- Double-firing is possible if a handler calls the helper and then also enters the shared flow.
- The shuffle that accompanies a search is coupled just as loosely.

**Proposed direction.** One entry point (e.g. in `LibrarySearchSupport`) that owns the whole
search lifecycle and always performs record + triggers + shuffle regardless of outcome (cards
found, none found, empty library, X = 0). Handlers supply only the candidate filter and what to
do with the chosen cards; no handler returns before reaching it. Prevention ("can't search")
is handled in the same place. `recordSearchAndQueueTriggers` becomes private to that service.

**Prompt to start it:**

```
Refactor library-search bookkeeping into a single choke point (see refactors-to-do.md, entry 1).
Today LibrarySearchTriggerHelper.recordSearchAndQueueTriggers is called from ~37 sites; many
search handlers have early exits (empty library, no matches, search prevented, X = 0) or are
bespoke per-card handlers that each have to remember to record the search, queue search
triggers, and shuffle.

1. Inventory every caller of recordSearchAndQueueTriggers and every LibraryShuffleHelper.shuffleLibrary
   call tied to a search; classify each path (normal choice, no match, empty library, prevented,
   bespoke card handler). Also find search handlers that never call it (missed-trigger bugs) and
   paths that could call it twice (double-trigger bugs). Write the inventory to a scratchpad file.
2. Design one entry point in LibrarySearchSupport that owns the lifecycle (prevention check,
   record + triggers, shuffle) for every outcome, and route all handlers through it. Make the
   helper method private/internal afterwards. Read agent-docs/ARCHITECTURE.md first.
3. Rules accuracy first: verify via the rules MCP how searching a library, failing to find, and
   shuffling work before citing any CR number; check official rulings for cards like Aven
   Mindcensor, Stranglehold, Ob Nixilis Unshackled, Archive Trap, and "up to X" with X = 0.
4. Add or extend tests for every outcome path (found / none found / empty library / prevented /
   X = 0) including opponent-search triggers, and list every engine file changed.
Do not run the full test suite; ask me to run it. Don't commit until I say so.
```

---

## 3. Small cleanups left by the D-card test fixes

**Status:** open · **Found:** 2026-10-10

- Discord, Lord of Disharmony's copy ability is now queued in `SpellCastingService`; the old
  collector path for it is dead code — remove it.
- Driftgloom Coyote added a narrow last-known-power special case in
  `EffectResolutionService.isConditionMet`; replace it with a general last-known-information
  aware `TargetPermanentMatches` evaluation.
- `SourceBecomesSubtypeUntilEndOfTurnEffect` Javadoc still says it sets
  `transientCreatureTypeOverride` (it uses a floating `GrantSubtype`).
- Soulbond code cites "CR 702.94" in several files (CreatureControlService, SoulbondSupport,
  ConditionEvaluationService); soulbond is 702.95 — re-verify and fix.
- About 12 cards create Mutagen tokens with `List.of()` subtypes instead of `CardSubtype.MUTAGEN`.
- `TurnCleanupService` still reads raw card STATIC effects in several places, so permanents that
  lost their abilities are still honoured there.

**Prompt to start it:**

```
Do the small cleanups in refactors-to-do.md, entry 3. Verify every CR number with the rules MCP
before writing it. Run only the affected card test classes (ask me for anything broader).
Don't commit until I say so.
```

---

## 4. Delayed step abilities judge `lastsWhile` only as a step begins

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Problem.** A `TemporaryGlobalTriggeredAbility` made with `atEveryStep(...)` and a `lastsWhile` condition
(`CardsStillInZone`, `SourceRemainsTapped`; Ertai's Meddling) is checked only in
`RulesEventPipeline.putStepAbility`, when its step begins. Between steps the ability stays in
`GameData.temporaryGlobalTriggeredAbilities` after its condition stopped holding.

**Why it matters.** Nothing triggers it in between today, but any reader that lists delayed abilities (a "has a
boon"-style count, a view, a non-step family matching it) sees an ability that has already ended.

**Proposed direction.** End it where the state changes, as `DurationClock` does for `Duration.AsLongAs` (the card
leaving its zone, CR 400.7; the source untapping), or have the clock own `lastsWhile` too.

**Prompt to start it:**

```
Do refactors-to-do.md entry 4: make a TemporaryGlobalTriggeredAbility's lastsWhile end the ability as its
condition stops holding (card leaves the zone, source untaps or leaves), not only when RulesEventPipeline.putStepAbility
judges it as a step begins. Read agent-docs/ARCHITECTURE.md and agent-docs/architecture/triggers.md (grep
"lastsWhile"). Add tests (Ertai's Meddling's card leaving exile mid-turn). Verify CR numbers with the rules MCP.
Don't commit until I say so.
```

---

## 5. State-based actions poll for counter removal

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Problem.** `StateBasedActionService` emits `CounterChangeEvent.ofPlayer(Kind.REMOVED, null, 0)` on every
state-based-action check (CR 704.3), whether or not a counter was removed, so the counter-removal matchers poll.

**Why it matters.** Every check builds a rules event batch and runs matchers for nothing; an ability that triggers on
"a counter is removed" can't tell which removal it saw.

**Proposed direction.** Emit one `CounterChangeEvent` per removal where the counter is removed (the counter
choke point in `PermanentCounterSupport` and the player counter paths), and drop the polling emission.

**Prompt to start it:**

```
Do refactors-to-do.md entry 5: replace the CounterChangeEvent(REMOVED) that StateBasedActionService emits on every
check with events emitted where counters are actually removed. Grep every counter-removal path first and list them;
check the counter-removal triggered abilities' tests before and after (the trigger ledger, MV_TRIGGER_LEDGER, shows
the difference). Read agent-docs/architecture/triggers.md (grep "CounterChangeEvent"). Don't commit until I say so.
```

---

## 6. The tap matcher writes turn history while matching

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Problem.** `TapTriggers` records `GameData.creatureTapCountsThisTurn` (in its tapped-permanent matcher) and
`playersWhoTappedLandForManaThisTurn` (`landTapped`) while it matches abilities, not where the tap happens.

**Why it matters.** Matching should only read (CR 603.2): history written by a matcher is skipped on a path that
doesn't reach it and depends on matcher order; "first time this turn" reads can see a count that includes the event
being matched.

**Proposed direction.** Record both at the `TapEvent` emission point (`TapUntapSupport` / the mana-tap path) before
the batch is matched; the matcher only reads them.

**Prompt to start it:**

```
Do refactors-to-do.md entry 6: move the creatureTapCountsThisTurn and playersWhoTappedLandForManaThisTurn writes out
of TapTriggers into the places that emit TapEvent, keeping the moment each is visible to "first time this turn" and
Desolation-style readers. Run the tap-trigger card tests (Desolation, Manabarbs, Don't Move, the "becomes tapped for the
first time" cards) before and after. Don't commit until I say so.
```

---

## 7. `PermanentRemovalServiceTest`'s mocks no longer reflect the engine

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Problem.** `PermanentRemovalServiceTest` (application module) mocks `DurationClock`, so the clock's `sourceLeft` /
`affectedLeft` work as a permanent leaves never runs, and its wiring leaves `layerSystemServiceProvider` null, so its
cases (the `ResolveDelayedPermanentAction` ones too) fail on HEAD.

**Why it matters.** The service test neither passes nor guards the departure behaviour; the card tests are the only
coverage.

**Proposed direction.** Rewire it with a real `DurationClock` and a provider for `LayerSystemService` (or turn the
cases that need them into harness tests), and delete the cases that only assert mock calls.

**Prompt to start it:**

```
Do refactors-to-do.md entry 7: make PermanentRemovalServiceTest pass. Run it alone first and list the failures. Give it
a real DurationClock and a LayerSystemService provider, or move the cases that depend on them to BaseCardTest-style
harness tests; stub withQueryScope to run its supplier. Don't run the full suite. Don't commit until I say so.
```

---

## 8. `scripts/duration-inventory.py` misses some duration fields

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Problem.** Its `EFFECT` field regex doesn't match names containing `WhileSource`, `TurnsRemaining` or `Revealed`, and
its docstring still counts `EffectDuration` constants (the enum is gone; `Duration` replaced it).

**Why it matters.** A new field holding a duration under such a name would be counted as neither effect nor history.

**Proposed direction.** Add the three to `EFFECT` and drop `EffectDuration` from the docstring and the constant count.

**Prompt to start it:**

```
Do refactors-to-do.md entry 8: extend scripts/duration-inventory.py's EFFECT regex with WhileSource, TurnsRemaining and
Revealed, remove its EffectDuration references, and run python3 -I scripts/duration-inventory.py --list FILE to check no
new field is flagged. Don't commit until I say so.
```

---

## 9. Pile-separation restrictions have no source name

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Problem.** Fight or Flight's and Stand or Fall's "until end of turn" pile restrictions are floating effects recorded
with an empty source card name, because `PendingPileSeparation` carries no source card.

**Why it matters.** The game log, the views and the trigger ledger show the effect with no source.

**Proposed direction.** Add the source card (or its name) to `PendingPileSeparation` and pass it to the floating effect.

**Prompt to start it:**

```
Do refactors-to-do.md entry 9: give PendingPileSeparation its source card so the pile restrictions Fight or Flight and
Stand or Fall create name their source. Copy the new field for simulations (GameDataDeepCopyTest). Run both cards'
tests. Don't commit until I say so.
```

---

## 10. Two architecture tests fail on HEAD

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Problem.** `GameMutationBoundaryRatchetTest` (engine) flags `GameService.completeTurnFaceUpAfterCostSacrificeAnswers`,
and `CardImmutabilityArchTest` (application) fails with "failed to check any classes".

**Why it matters.** Both guard invariants in `agent-docs/ARCHITECTURE.md`; while red they guard nothing.

**Proposed direction.** Route the flagged mutation through the boundary the ratchet expects (or lower its allowlist if
the count is right); fix `CardImmutabilityArchTest`'s class import so it scans the card classes again.

**Prompt to start it:**

```
Do refactors-to-do.md entry 10: run GameMutationBoundaryRatchetTest and CardImmutabilityArchTest alone, fix the
GameService.completeTurnFaceUpAfterCostSacrificeAnswers mutation boundary and the arch test's class import so both pass.
Don't commit until I say so.
```

---

## 11. `SpellFilter.firstThisGame` is read by one matcher only

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Problem.** `firstThisGame` (Chancellor of the Annex's "their first spell of the game") is judged only by the generic
cast matcher (`SpellTriggers`, against `SpellEvent.Cast.firstSpellOfGame`); the other readers of a `SpellFilter` ignore it.

**Why it matters.** A future ability using it through another family (copy, delayed or granted readers) would trigger on
every spell.

**Proposed direction.** Judge it with the filter's other fields in the one place every family calls, or reject it in
the factories of families that don't support it.

**Prompt to start it:**

```
Do refactors-to-do.md entry 11: list every reader of SpellFilter and make firstThisGame judged wherever the filter is
(or rejected where it can't be). Add a test for each family that can carry it. Don't commit until I say so.
```

---

## 12. Kjeldoran Royal Guard: two redirects have no test

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Problem.** With two activated Kjeldoran Royal Guards, both redirects are offered to the affected player (CR 616.1),
but no card test covers it.

**Why it matters.** The behaviour came with the shields becoming floating effects and nothing guards it.

**Proposed direction.** Add a `KjeldoranRoyalGuardTest` case: two Guards activated, the player chooses which redirect
applies.

**Prompt to start it:**

```
Do refactors-to-do.md entry 12: add a KjeldoranRoyalGuardTest case with two activated Guards and unblocked attackers,
asserting both redirects are offered (CR 616.1, verify with get_rule) and the chosen Guard takes the damage. Don't commit
until I say so.
```

---

## 13. `EndOfTurnStateField`'s Javadoc names removed stores

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Problem.** The enum's Javadoc still describes Stolen Uniform's control-loss entries as a game-wide `LIST` (they are
delayed abilities now, ended by `GameData.endControlLossDelayedAbilitiesOfThisTurn`), and its "a new until-end-of-turn
field of `Permanent` needs a constant here" invites a field where a floating effect belongs.

**Why it matters.** It is the documented "how to add one" for the cleanup step (`agent-docs/architecture/resolution.md`
points at it); a new effect must be a floating effect instead.

**Proposed direction.** Rewrite the Javadoc to the constants that remain (turn history and per-turn bookkeeping) and say
that an effect a resolution makes is a floating effect with a `Duration`, never a new field here.

**Prompt to start it:**

```
Do refactors-to-do.md entry 13: rewrite EndOfTurnStateField's Javadoc to the constants it has now; check every name it
mentions exists. Javadoc only. Don't commit until I say so.
```

---

## 15. Two "enters with an additional counter" grants take different paths

**Status:** open · **Found:** 2026-11-19, Triggering-class audit

**Problem.** `GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect` (Yuna, Patchplate Resolute,
Benalish Knight-Counselor, Boreal Outrider, Gluttonous Hellkite, Torgal) stores into
`GameData.spellAdditionalEnterCounters` / `spellAdditionalEnterCounterAmounts`, applied by
`BattlefieldPlacementService.applySpellAdditionalEnterCounters` inside `applyEnterWithCounters` with one
`doublePlusOnePlusOneCounters` pass, cleared on recast by `GameData.recordSpellCast`, and never calling
`firePlusOnePlusOneCountersPutOnAnotherCreatureTriggers`. `GrantAdditionalCounterToTriggeringCreatureSpellEffect`
(Tenacious Pup, March Toward Perfection, Loch Larent, Jade Orb of Dragonkind, Chocobo Camp, Champions of Tyr, Arcane
Archery, `SpecializeKlementEffectHandler`) stores into `GameData.spellEntryCounters` (shared with graveyard/exile cast
permissions in `SpellCastingService`), applied by `applySpellEntryCounters` → `applyEntryCounters`, which does fire that
trigger; it is cleared only by `StackResolutionService.disposeFizzledPermanentSpell` and end-of-turn cleanup.

**Why it matters.** The six +1/+1 cards don't fire "whenever one or more +1/+1 counters are put on another creature"
triggers (CR 122.6 counts counters a permanent enters with). A countered card recast the same turn may keep a stale
`spellEntryCounters` grant. Counters from several sources are applied in separate replacement passes, though all the
counters a permanent enters with are one event (CR 614.1c), so e.g. Hardened Scales should apply once (check its
rulings).

**Proposed direction.** One path: `spellEntryCounters` / `applyEntryCounters` taught the `countedOnEntry` amounts, cleared
when the spell is countered or recast, and all entry counters of a permanent replaced as one event. Merge the two effects
if nothing else differs.

**Prompt to start it:**

```
Do refactors-to-do.md entry 15: route GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect and
GrantAdditionalCounterToTriggeringCreatureSpellEffect through one entry-counter path in BattlefieldPlacementService
(spellEntryCounters/applyEntryCounters with countedOnEntry amounts), clear grants when the spell is countered or recast,
and apply all counters a permanent enters with as one replacement event. Read AGENTS.md and agent-docs/ARCHITECTURE.md
first; verify CR numbers with get_rule and Hardened Scales' rulings with get_rulings. Add tests: the +1/+1-counters-put-
on-another-creature trigger for the six +1/+1 cards, a countered-then-recast card keeping no stale grant, and Hardened
Scales with two entry-counter sources. Run only touched test classes by FQCN. Don't commit until I say so.
```

---

<!--
Template — copy everything between the lines, give it the next number, and append above.

## N. <Short title>

**Status:** open · **Found:** YYYY-MM-DD, <context in which it was spotted>

**Problem.** <What's wrong / duplicated / fragile, with concrete file and method names.>

**Why it matters.** <Bugs it caused or could cause; concrete examples.>

**Proposed direction.** <Target design, kept short.>

**Prompt to start it:**

```
<A self-contained prompt that can be pasted to kick off the refactor.>
```
-->
