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

**Status:** done 2026-11-24 (effect cleanup step 9, fold run 14b): the restrictions are now `CantAttackThisTurnEffect` and `CantBlockThisTurnEffect` steps after `SeparateIntoPilesEffect`, named by their stack entry's source · **Found:** 2026-11-19, closing the durations refactor

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

## 16. Effect cleanup leftovers: blocked records and step-10 parts

**Status:** open · **Found:** 2026-11-26, closing the effect cleanup (step 11)

The effect cleanup's plan and queues were deleted when it closed (2026-11-26). Their last versions:
`git show 3b0d60d42d4:effect-cleanup-refactor.md` (plan, Standing knowledge, the owner's decisions, step 10's Findings) and
`git show 3b0d60d42d4:effect-cleanup-queues.md` (fold queue, defect queue). What the cleanup built for card and engine authors is in
`agent-docs/architecture/effect-building-blocks.md`; `scripts/effect-audit-ledger.csv` keeps every class's verdict and
`python3 -I scripts/effect-inventory.py` the inventory.

**Problem.** 695 effect records whose latest ledger verdict is `blocked-*` still fuse a verb with one subject or two instructions,
each waiting for a missing part; and 28 step-10 leftovers stay on their own records. One line each: the class or card, what
it needs, why. The blocked lines are the ledger note, condensed; read the full note in the ledger before starting one.

**Why it matters.** Each is a record a new card can't reuse and a handler that copies rules knowledge the shared pieces already
have; three or more blocked by the same cause are worth building the missing part for.

**Proposed direction.** Take one cause at a time: build the missing step, subject or loop, fold every record waiting for it
(update its ledger row), and lower `EffectCountRatchetTest`'s baselines.

**Prompt to start it:**

```
Do refactors-to-do.md entry 16 for the cause "<cause>": build the part its lines need and fold the records waiting for it onto
the existing verbs and subjects. Read AGENTS.md, agent-docs/ARCHITECTURE.md, agent-docs/architecture/resolution.md and
agent-docs/architecture/effect-building-blocks.md first; read each record and handler to the end and its ledger note in
scripts/effect-audit-ledger.csv. Check Oracle text with get_card and rulings with get_rulings, verify CR numbers with get_rule.
Rewrite the cards, delete the records and handlers, add a ledger row per class, update agent-docs, lower EffectCountRatchetTest.
Run the test classes of every card that used a deleted class (scripts/effect-inventory.py --tests-for). Don't commit.
```

#### Step-10 parts left on their records (one line each: class or card, what it needs, why)

- **The T merge**: `CreateTokenCopyOfSourceEffect` (+ the one-card `CreateTokenCopyOfSourceForEachOtherPlayerEffect`, `CreateTokenCopyOfSourceForTargetPlayerEffect`) into `CreateTokenCopyOfTargetPermanentEffect`: needs a card-or-permanent source subject with its fallbacks, five components each set by one to a few cards, and the attack-target choice; more than the subject, so step 10 skipped it. Its tapped-and-attacking copies wait on the owner (below).
- `ReturnSourceCardFromGraveyardToBattlefieldEffect` (Runeblade Raiser's perpetual loss of the ability, Bail Out's returned permanent as the dealer): two one-card components; fold onto `PutCardsOntoBattlefieldEffect(sourceObjectCard())` once a second card needs either.
- Roalesk, Prime Specimen (`PayXManaConjureRandomCreatureWithManaValueAndCloakEffect`): its inner part (a random creature by mana value, cloaked, a face-down-only grant) is one card's; it could take `UnlessPaysEffect.youMayPayX`. Its Oracle conjures into the hand and then cloaks; the handler puts the card straight onto the battlefield cloaked (defect below).
- Decoy Gambit (`ReturnTargetCreaturesUnlessControllersDrawEffect`): target objects with a draw per payer before the returns; and its can't-draw question (owner, below).
- Stench of Evil (`DamageEachDestroyedPermanentControllerUnlessPaysEffect` + `DestroyAllPermanentsEffect`'s per-destroyed rider): departed objects, damage summed per player.
- Heroism (`PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect`): needs combat-damage prevention over a subject.
- Tidal Flats (`GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect`): needs a "creatures blocking that creature" subject.
- Tariff (`EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect`): needs a ransom read from the object (its mana cost).
- Whirlwind Denial (`CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect`): stays its own record (owner's decision U2: APNAP choices, then one counter); its payment is on `ResolutionCostPaymentService`.
- Fade Away, Killing Wave (`EachCreatureControllerSacrificesPermanentUnlessPaysEffect`, `KillingWaveEffect`): stay (a chosen subset paid as one event, their rulings).
- Behemoth of Vault 0 (`PayEnergyThenDestroyTargetPermanentEffect`): its "when you do, destroy that permanent" is about the declared target, which `QueueReflexiveAbilityEffect` would target again.
- Enchanter's Bane, Star Athlete (`TargetPermanentControllerMaySacrificeOrDamageEffect`): an "unless" whose damage is dealt by the target; fold onto `UnlessPaysEffect` with a `DamageFromSourceCost` dealt by another permanent.
- Unnatural Hunger (`SacrificeOtherCreatureOrDamageEffect`): a sacrifice ransom; the record's other cards are "if you can't" steps already.
- `ReturnMultiplePermanentsToHandCost` (26 activation-cost cards): fold into `ReturnPermanentToHandCost.youControl(count, filter, words)` with the activation-cost path.
- The cost pairs beside `MayEffect` (`Sacrifice…Cost`, `Exile…Cost`, `Return…ToHandCost`, `PutCounterOn…Cost`): one cost per verb with a subject, CR 118.12's "if you do" being the choice to pay.
- `PayLifeOrPayManaCost` (Redirect Lightning, the Defiler units): stays beside `AnyOfCost` (orchestrator's decision U7 Q5).
- Rescuer Sphinx, Mariposa Military Base, Channel (the last of May's 11): unique (as-enters choices; Channel is a timed special action).
- The subtype-grant pair `GrantSubtypeToTargetCreatureEffect` / `TargetCreatureBecomesSubtypeUntilEndOfTurnEffect`: needs an `overriding` component, Goatnapper's perpetual half and a per-mode AI polarity.
- Choose-one for a target, `ChooseOneForTargetCreatureEffect` / `ChooseOneForTargetPermanentEffect`: folding needs the per-mode AI polarity and keeps the permanent form's no-target prompt.
- `RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect` (Season of the Bold, Nuka-Nuke Launcher): its watched player is a permanent target's controller or the defending player, which `RegisterGlobalTriggeredAbilityEffect` can't take (three new parts for two cards).
- Boost: the `StaticBoostEffect` family (per-creature amounts, a layered set), the while-tapped record, the chosen-type and Hand of Vecna records (a choice at resolution); `BoostOpponentCreaturesByPoisonCountersEffect` and `BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect` fold onto `BoostEffect.each` once its `BOOSTED` amounts read per permanent of a set.
- Counters: `RemoveAllCountersFromTargetPermanentEffect` (every kind and player counters: its own verb), `RemoveAllCountersAsCostEffect` (a cost), Bomb Squad's fused record, the as-enters removals (Thief of Blood, Sin), `PutCounterOnEachControlledPermanentOfChosenSubtypeEffect` (a choice at resolution, one card): stay.
- Damage: `DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffect` (an amount per recipient's controller), `DiscardRandomCardThenMassDamageEffect` (its own repeat loop), `EachCreatureDealsPowerDamageToItselfEffect`, the one-card dealer records (Mage Slayer, Aura Barbs, Grim Contest, Spinning Wheel Kick, the target-land, enchanted-creature and up-to-one-source records), `DealDividedDamageEffect` (divided at cast, CR 601.2d), fight (CR 701.14b): stay out of `DealDamageEffect` until a second user of each part.
- Draw: `EachOpponentDrawsCardEffect` (Struggle for Project Purity counts a replaced draw's extra cards: the owner's replaced-draw question), `EachOtherPlayerDrawsCardEffect` (the gift files: the owner's gift opponent).
- Destroy and exile: `DestroyAllPermanentsEffect` (Seeds of Innocence's and Stench of Evil's per-destroyed riders), `ExileAllPermanentsEffect` (five one-card riders: source tracking, the upkeep return, perpetual changes, play permissions), `DestroyTargetPermanentThenEffect`, `ExileChosenPermanentsEffect`.
- Conjure: `ConjureCardOntoBattlefieldEffect` (the `Supplier` draft path), `ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect` (one card), `ConjureCardToOpponentBattlefieldEffect` (gift), `ConjureDuplicateOntoBattlefieldEffect` (three cards).
- Other: `SacrificeEachMatchingPermanentEffect` (7 cards), `RegenerateAllOwnCreaturesEffect` (6), the reveal heads, Mirrorform's copier set (`EachControlledPermanentBecomesCopyOfTargetNonAuraPermanentEffect`): not audited for a fold yet.
- Kept on purpose (checked in step 10, no work): `CreaturesCantAttackControllerUnlessPredicateEffect` (whom a creature may attack, also floated), `MakeAllCreaturesUnblockableEffect` (a rule, not a locked set, CR 611.2c), `MustBlockTargetCreatureEffect`, `GoadCreaturesUntilNextTurnEffect` (covers later creatures), `GoadEquippedCreatureEffect` (static), `ExileGraveyardCardsEffect.eventValueFilter` (7 card files).
- `MayCastDiscardedCardWithoutPayingManaCostEffect(CardSubject)` and `CastCardFromGraveyardEffect.of(CardSubject)` are two graveyard free-cast offers over a card subject: a step-10 merge. Sure.
- `AllPermanentsGainChosenColorEffect` (Painter's Servant) and `ControlledNonlandPermanentsAreColorEffect` (Celestial Dawn) are `GrantColorEffect` parameters now; `BecomeColorlessUntilEndOfTurnEffect`, `SetTargetColorEffect`'s colorless branch, `SetChosenColorUntilEndOfTurnEffect` and `GrantColorUntilEndOfTurnEffect` overlap `BecomeChosenColorsUntilEndOfTurnEffect`: step-10 merges. Sure.
- Confiscation Coup and Behemoth of Vault 0 keep their pay-energy records: Coup is now `UnlessPaysEffect.youMayPay(new PayEnergyCost(new TargetManaValue()), new GainControlOfTargetEffect(Duration.PERMANENT))` (a fold run; mind the AI's control-stealing view); Behemoth's reflexive "that permanent" is a rules defect, entry 18.
- Transcantation (`TargetSpellBecomesCopyOfCardEffectHandler`) queues its own new-targets offer; fold it onto `MayEffect(ChooseNewTargetsForTargetSpellEffect…)` (CR 115.7d). Sure.
- Demonic Hordes' `OpponentChoosesPermanentToSacrificeEffect` and Infernal Denizen's `OpponentMayGainControlOfCreatureYouControlEffect` were `blocked-other` on the old forced-cost fallback list; both are now ordinary steps of their resolution (U4a), so their folds (`ChoosePlayerEffect` + `ChoosePermanentsEffect.chosenBy(playersThisWay(CHOOSE))` + the verb) are open for a fold run. Sure of the code.

#### Blocked records by cause (695)

#### The first half prompts mid-step (blocked-step-model) (127)

The fused record's first half pauses mid-step for a prompt, so it can't be an earlier step; unblocked by moving the handler onto the step model (`leave-battlefield-redesign.md` step 14).

- `ActivePlayerDiscardsHandThenDrawsEffect`: fused: discard hand then draw (that many); subject siblings of TargetPlayer/EachPlayerDiscardsHandThenDrawsThatManyEffect; step 8.
- `BeholdEffect`: behold prompts and records nothing; no BEHOLD step kind.
- `CastTargetInstantOrSorceryFromGraveyardEffect`: its rider follows a cast prompt; no CAST step kind.
- `CastTopCardFromLibraryForManaEffect`: no card (engine-built): the paid-cast continuation of run 13's blocked-step-model RevealTopCardMayCastForManaOrPutLandOntoBattlefieldEffect, after its resolution-time mana payment; goes with that record.
- `CollectEvidenceEffect`: collect evidence prompts and records nothing; no step kind for it.
- `CompleteAmassGoblinsEffect`: no card (engine-built by AmassGoblinsEffectHandler and Azog's DestroyTargetCreatureThenAmassGoblinsEffectHandler): the bespoke continuation of an amass whose token creation paused for a replacement choice.
- `ConjureDuplicateOfCardIntoHandEffect`: Vona no longer uses it; the payload of TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect's handler alone (blocked-step-model: Grave Choice, Soul Servitude); goes with that record.
- `DealDamageToOneOfTwoTargetCreaturesThenOtherCantBlockEffect`: the targets' controller picks which creature is dealt damage at a choice prompt first; Trial of Agony.
- `DefendingPlayerDiscardsHandThenDrawsThatManyEffect`: fused: discard hand then draw (that many); subject siblings of TargetPlayer/EachPlayerDiscardsHandThenDrawsThatManyEffect; step 8.
- `DiscardAndDrawCardEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardAnyNumberThenEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardCardAndPutCounterOnSourceEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardCardAndUntapSelfEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardCardsAndPutCountersOnSourceEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardHandThenDrawAndReturnArtifactOrCreatureCardsEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardOwnHandThenDrawAndThenEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardOwnHandThenDrawEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardOwnHandThenDrawEqualToTargetPlayerHandSizeEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardOwnHandThenDrawThatManyAndDealDamageEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardOwnHandThenDrawThatManyEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardOwnHandThenEachOpponentSacrificesCreaturePerCardEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardRandomCardDealDamageEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardRandomCardThenMassDamageEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardThenReturnFromGraveyardToHandEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardTwoUnlessCardPredicateEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardTwoUnlessCardTypeEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardTwoUnlessCreatureEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardUnlessReturnedLandHadNonbasicLandTypeEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DiscardUpToThenDrawThatManyEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `DrawCardRevealDiscardUnlessLandEffect`: DiscardCardsEffect and the hand reveal exist; left: it draws by rerunning itself and needs the unreplaced draw's card (CR 121.6c), as Pact Weapon and Jace do.
- `EachOpponentDiscardsOrControllerDrawsEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachOpponentDiscardsOrControllerGainsLifeEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachOpponentDiscardsOrLosesLifeEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachOpponentDiscardsThenControllerDrawsEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachOpponentDiscardsTwoUnlessNonlandEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachOpponentDrawsThenControllerDrawsEffect`: the draw step records before a replaced draw's question is answered, and this counts who drew across those pauses by rerunning itself (Cut a Deal).
- `EachOpponentSacrificesCreatureCreateTokensEffect`: fused: sacrifice then tokens per sacrificed; step 7/8.
- `EachOpponentSacrificesCreatureOrVehicleOrDiscardsEffect`: fused: sacrifice or discard; step 8.
- `EachOpponentSacrificesNontokenCreatureConjuresDuplicatesEffect`: fused: sacrifice then conjure duplicates; step 7/8.
- `EachOpponentSacrificesPermanentCreateTokensEffect`: fused: sacrifice then tokens; step 7/8.
- `EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachPlayerDiscardsCreatureOrLosesLifeEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachPlayerDiscardsHandThenConjuresFromAdjacentPlayerLibraryEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachPlayerDiscardsHandThenCreatesTokensEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachPlayerDiscardsHandThenDrawsThatManyEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachPlayerDiscardsOneThenControllerDrawsIfDiscardedEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachPlayerDiscardsOrLosesLifeEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `EachPlayerSacrificesCreatureCreateTokenEqualToTotalPowerEffect`: fused: sacrifice then act on what was sacrificed; step 7/8.
- `EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnAnotherPermanentEffect`: fused: sacrifice then return; step 8.
- `EachPlayerSacrificesPermanentOrDiscardsEffect`: fused: sacrifice or another instruction; step 8.
- `EachPlayerSacrificesPermanentOrLosesLifeEffect`: fused: sacrifice or another instruction; step 8.
- `EachTargetPlayerLosesLifeAndSacrificesCreatureEffect`: fused: life loss and sacrifice; step 8.
- `ExileAnyNumberOfOwnGraveyardCardsWithFourCardTypesThenPutPermanentOntoBattlefieldEffect`: multi-graveyard choice by rerunCurrentEffectAfterInteraction, as ExileNCardsFromGraveyardThenEffect; moving handlers onto the step model is outside this plan (Winter).
- `ExileOneFromTopCardsFaceDownWithHatchingCounterEffect`: look-and-choose through a LibrarySearch destination that exiles mid-step (step 8 closing summary); outside this plan (The Dragon-Kami Reborn).
- `ExileSelfAndSaddledCreatureEffect`: saddler choice then flicker; Fortune, Loyal Steed.
- `ForageEffect`: forage chooses exile-three or sacrifice-a-Food and records nothing; no step kind for it.
- `GrantSubtypeToChosenPermanentEffect`: no card uses it any more; the amass-Goblins continuation (CompleteAmassGoblinsEffectHandler, itself blocked-step-model) still inserts it after its bespoke Army choice, which records no CHOOSE step; goes with that continuation.
- `KinshipEffect`: its payload follows a look-and-reveal prompt; no REVEAL step kind.
- `LookAtTopCardsEffect`: its no-card / selected-card riders run inside the look-and-choose continuation; no LOOK step kind.
- `LookAtTopCardsMayExileOneAndPlayThisTurnEffect`: the look-and-choose continuation; no LOOK step kind.
- `MillActivePlayerAndCreateTokensByManaValueEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillAndRegisterDelayedAttackerBoostEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillAndTransformIfTypesEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillControllerAndCreateTokensByMilledCardTypeEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillControllerAndCreateTokensForMilledCreaturesEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillControllerAndMayCastMilledSpellEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillControllerAndMayPlayFromGraveyardThisTurnEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillControllerAndMayPutMilledLandsOntoBattlefieldEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillControllerAndMayPutMilledSagaAndLandOntoBattlefieldEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillControllerAndPutMilledCardOntoBattlefieldEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillControllerAndPutMilledCreatureOntoBattlefieldEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillControllerAndPutMilledCreaturesOntoBattlefieldEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillControllerAndTemporarilyReanimateMilledCreaturesEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillControllerThenIfConditionEffect`: fused: mill then a conditional instruction; step 8.
- `MillControllerThenIfMilledEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillEachOpponentThenIfMilledEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillEachPlayerAndCreateTokensForMilledCreaturesEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillEachPlayerAndExileUpToTwoCreaturesCreateTokenWithTotalPowerEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillTargetPlayerAndBoostSelfByManaValueEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillTargetPlayerAndDrawIfTypeMilledEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillTargetPlayerAndDrawPerTypeMilledEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillTargetPlayerAndMayCastMilledSpellEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `MillTargetPlayerAndMayCastSpellFromGraveyardEffect`: fused: mill then another instruction; step 8.
- `MillTwoThenSacrificeSelfIfMilledCardsShareAllTypesEffect`: fused: mill then act on the cards milled this way; step 7/8.
- `OpponentDiscardsAnyNumberThenDrawsThatManyEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `PutCounterAndWatchTargetCreatureDeathEffect`: fused counter plus death watch (step 8).
- `PutCounterOnSelfThenCastTargetInstantOrSorceryFromGraveyardEffect`: fused (step 8).
- `PutCounterOnSelfThenDrawAndLoseLifePerCounterEffect`: fused (step 8).
- `PutCounterOnSelfThenDrawPerCounterEffect`: fused (step 8).
- `PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect`: fused (step 8).
- `PutTargetPermanentIntoLibraryNFromTopThenEffect`: put-into-library is not an earlier-step primitive and has no StepKind; ThenEffectRecipient stays for it.
- `RemoveCounterFromControlledCreatureThenDrawEffect`: fused (step 8).
- `RemoveCounterFromTwoCreaturesThenEffect`: fused (step 8).
- `RemoveCounterOrSacrificeSelfEffect`: fused removal or sacrifice then effects (step 8).
- `RevealMatchingCardsFromTargetHandAndKeepEffect`: the hand reveal and choice steps exist; left: its two cards' riders read the chosen card through the RevealedMatchingHandCardChoice continuation, and the exiling one needs an exile step over a hand card.
- `RevealTopCardIfOddMayCastFreeElseDrawEffect`: the free cast of a revealed library card exists only as continuations that park mid-step and also move the rest (top-card may-play, search pick, revealed dump): moving them onto the step model is outside this plan (Yennett).
- `RevealTopCardMayCastForManaOrPutLandOntoBattlefieldEffect`: the free cast of a revealed library card exists only as continuations that park mid-step and also move the rest (top-card may-play, search pick, revealed dump): moving them onto the step model is outside this plan; cast for {1} (Jadzi).
- `RevealTopCardOfTargetPlayerMayCastFreeEffect`: the free cast of a revealed library card exists only as continuations that park mid-step and also move the rest (top-card may-play, search pick, revealed dump): moving them onto the step model is outside this plan (Mirrored Depths).
- `RevealTopCardsOfTargetPlayerAndCastInstantOrSorceryEffect`: the free cast of a revealed library card exists only as continuations that park mid-step and also move the rest (top-card may-play, search pick, revealed dump): moving them onto the step model is outside this plan (Talent of the Telepath).
- `RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect`: it exiles, so the exile-until step is its first half; left: the search pick's cast-without-paying continuation with haste, sacrifice and the rest's fate, which parks mid-step (four cards).
- `RevealTopCardsOfTargetSpellControllerUntilInstantOrSorceryAndCastEffect`: the free cast of a revealed library card exists only as continuations that park mid-step and also move the rest (top-card may-play, search pick, revealed dump): moving them onto the step model is outside this plan (Spellshift).
- `RevealUntilNonlandCardWithManaValueAndCastEffect`: the free cast of a revealed library card exists only as continuations that park mid-step and also move the rest (top-card may-play, search pick, revealed dump): moving them onto the step model is outside this plan (Treasure Keeper).
- `SurveilThenEffect`: surveil is not on step 7's earlier-step list (it already splices SurveilEffect + continuation; add surveil to the list to split).
- `TapSourceThenEffect`: first half (tap) is not on step 7's earlier-step list and has no StepKind.
- `TapTargetThenEffect`: first half (tap) is not on step 7's earlier-step list and has no StepKind.
- `TargetPlayerDiscardsAndDestroysCreaturesOfChosenTypeEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `TargetPlayerDiscardsAnyNumberThenDrawsThatManyEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `TargetPlayerDiscardsHandThenDrawsThatManyEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `TargetPlayerDiscardsHandThenSeeksEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `TargetPlayerDiscardsReturnSelfIfCardTypeEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `TargetPlayerDiscardsThenControllerDrawsShortfallEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `TargetPlayerDiscardsThenDrawsThatManyEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `TargetPlayerDiscardsThenGainLifeForEachCardTypeEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `TargetPlayerDiscardsThenPutsLandOntoBattlefieldEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `TargetPlayerDiscardsUpToThenDrawsThatManyEffect`: fused: a discard prompt plus another instruction (then / or / unless / and); step 8.
- `TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffect`: fused: sacrifice then tokens; step 7/8.
- `TargetPlayerSacrificesCreatureOrControllerCreatesTokenEffect`: fused: sacrifice or another instruction; step 8.
- `TargetPlayerSacrificesCreatureOrCreatesTokenEffect`: fused: sacrifice or another instruction; step 8.
- `TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffect`: fused: sacrifice then act on the sacrificed creature; step 7/8.
- `TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffect`: fused: sacrifice then act on the sacrificed creature; step 7/8.
- `TargetPlayerSacrificesCreatureThenDrawsPowerEffect`: fused: sacrifice then draw its power; step 7/8.
- `TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect`: fused: sacrifice then conjure; step 7/8.
- `TargetPlayerSacrificesPermanentThenDealsManaValueDamageEffect`: fused: sacrifice then damage by its mana value; step 7/8.

#### Each player chooses, then all act (blocked-player-loop) (90)

Each player chooses in its own continuation; unblocked by a generic "each player chooses, then all act" loop (CR 101.4, 608.2e).

- `AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect`: each player is offered the exile in turn.
- `DestroyAllCreaturesExceptChosenTypesEffect`: Harsh Mercy: each player chooses a creature type in APNAP order, then one wipe; ChooseCreatureTypeAtResolutionEffect has the controller as its only chooser and keeps one type.
- `DisorientingChoiceEffect`: controllers now decide in APNAP order and the chosen permanents are exiled together after the last answer (ruling, CR 101.4); the handler keeps its own loop.
- `EachOpponentBlightsEffect`: each opponent chooses and puts its counters before the next chooses (High Perfect Morcant); BlightEffect now takes one player, not each(...).
- `EachOpponentChoosesCreatureCardFromControllerGraveyardToBattlefieldEffect`: each opponent in turn chooses from the controller's graveyard into one held batch (Rejoin the Fight).
- `EachOpponentChoosesCreatureCardFromTheirGraveyardToBattlefieldEffect`: the graveyard-return queue: each opponent chooses, then one batch enters (Dredge the Mire).
- `EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffect`: a choice per opponent, then one of the chosen is copied with their totals (Benthic Anomaly).
- `EachOpponentChoosesCreatureToExileWithSourceEffect`: chosen creatures accumulate in the choice context and are exiled together in one leave batch after the last opponent chooses (CR 101.4).
- `EachOpponentChoosesCreatureToTapAndGoadEffect`: each opponent chooses, then each is tapped and goaded (Fell Beast's Shriek).
- `EachOpponentChoosesCreatureYouGainControlEffect`: each opponent chooses, then control of all is gained (Riches).
- `EachOpponentChoosesGreatestManaValueCreatureToReturnToHandEffect`: each opponent breaks their tie, then one bounce batch (Summon: Valefor).
- `EachOpponentChoosesGreatestPowerCreatureToDestroyEffect`: the controller breaks each opponent's tie in turn, then one destroy batch (Highcliff Felidar).
- `EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamageEffect`: choices accumulate by opponent and the creatures are exiled together, powers read just before (ruling, CR 101.4).
- `EachOpponentChoosesMasterOfCeremoniesEffect`: each opponent chooses, then the rewards are counted over the choices (Master of Ceremonies).
- `EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffect`: each opponent chooses in turn in its own continuation (Gilded Ambusher).
- `EachOpponentMayDiscardOrSacrificePermanentEffect`: one discard-or-sacrifice-or-damage offer per opponent through queued mays (Zoyowa, Osseous Sticktwister).
- `EachOpponentMayInvestigateEffect`: one may per opponent, counted for the controller's investigations (Wernog).
- `EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffect`: one sacrifice-or-lose-and-draw offer per opponent through queued mays (Braids).
- `EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect`: one sacrifice-or-lose-life offer per opponent through queued mays (Fandaniel).
- `EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect`: each opponent is asked before seeing the library and may search and find nothing, then the next; the search queue only knows a searcher by a card taken (CR 701.23a; Boldwyr Heavyweights).
- `EachOpponentOfTriggeringPlayerMayDrawCardEffect`: asks each opponent its own may choice; its MayEffect now wraps DrawCardEffect(targetPlayerDeclaredElsewhere()).
- `EachOpponentReturnsGreatestManaValueNonlandPermanentThenDiscardsEffect`: a tie choice and a discard per opponent in its rerun loop (Dispersal).
- `EachOpponentRevealsHandAndExilesCreatureCardEffect`: inserts one revealed-hand choice per opponent (Valki).
- `EachOtherPlayerMayReturnCardsFromGraveyardToBattlefieldEffect`: one optional choice per other player on the graveyard-return queue (Enslaved Horror).
- `EachPlayerChoosesColorThenExileOtherPermanentsEffect`: each player chooses a color, then one exile by each controller's color (Selective Obliteration).
- `EachPlayerChoosesCreaturePutsVowCounterThenSacrificesRestEffect`: each player keeps one, then the counters and one sacrifice batch (Promise of Loyalty).
- `EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffect`: each player chooses, then all sacrifice together.
- `EachPlayerChoosesCreatureToDestroyEffect`: each player chooses any creature, then one destroy batch (The Horus Heresy).
- `EachPlayerChoosesCreatureToLeftThenCreatesMenaceCopyEffect`: each player chooses from the player to their left, then the copies (Caught in a Parallel Universe).
- `EachPlayerChoosesDifferentOpponentPermanentThenDestroyRestEffect`: each player keeps a different one, then the rest are destroyed (Fortunate Few).
- `EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandEffect`: a choice per type and player, then all return (Planar Overlay).
- `EachPlayerChoosesLandOfEachBasicTypeThenSacrificeRestEffect`: a choice per type and player, then the rest are sacrificed (Global Ruin).
- `EachPlayerChoosesLandsThenDestroyRestEffect`: each player keeps lands, then every other permanent is destroyed together (Urza's Sylex).
- `EachPlayerChoosesLandsThenSacrificeRestEffect`: each player keeps six lands, then all sacrifice the rest together (Planetary Annihilation).
- `EachPlayerChoosesNonlandPermanentAndPutCounterEffect`: each player chooses among all nonland permanents, then all the counters (Eye of Doom).
- `EachPlayerChoosesNonlandPermanentThenReturnRestEffect`: each player keeps one, then the rest return (Consuming Tide).
- `EachPlayerChoosesOneCardOfEachColorThenDiscardsRestEffect`: a choice per color and player from revealed hands, then the discards (Noxious Vapors).
- `EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyEffect`: each player chooses one or two, then each copies and counts (Human-Time Lord Meta-Crisis).
- `EachPlayerChoosesOpponentPermanentToDestroyEffect`: each player chooses, then one destroy batch (Sadistic Shell Game, Druid of Purification).
- `EachPlayerChoosesOpponentPermanentToExileEffect`: each player chooses up to one, then all exiled (The Legend of Yangchen).
- `EachPlayerChoosesOwnPermanentsToExileUntilSourceLeavesEffect`: each player chooses, then all exiled until the source leaves (Foreboding Steamboat).
- `EachPlayerChoosesPartyThenSacrificesRestEffect`: a party choice per role and player, then one sacrifice batch (Stick Together).
- `EachPlayerChoosesPermanentsThenPhaseOutRestEffect`: each player from the controller on keeps permanents, then the rest phase out together (Disciple of Caelus Nin).
- `EachPlayerChoosesTokenEffect`: each player chooses a token, then all are created (The Pit).
- `EachPlayerDiscardsDownToFewestEffect`: per-player amount in its own APNAP loop; DiscardEffect evaluates one amount.
- `EachPlayerDiscardsFractionOfHandRoundedDownEffect`: per-player amount in its own APNAP loop; DiscardEffect evaluates one amount.
- `EachPlayerDiscardsFractionOfHandRoundedUpEffect`: per-player amount in its own APNAP loop; DiscardEffect evaluates one amount.
- `EachPlayerDiscardsNonlandCardEffect`: card filter on each player's chosen discard in its own loop; DiscardEffect has no discard filter.
- `EachPlayerExilesCardFromGraveyardThenEffect`: each player chooses in its own rerun loop, all exiled together, then one reflexive ability counts the nonlands (Augusta).
- `EachPlayerExilesPermanentsOrCardsFromHandEffect`: each player picks among their permanents and hand, then all exiled (Nicol Bolas, Dragon-God; Descent into Madness).
- `EachPlayerFlipsCoinAndTailsSacrificesCreatureEffect`: each tails player chooses, then all sacrifice together (Goblin Assassin).
- `EachPlayerKeepsCardsShufflesRestIntoLibraryEffect`: each player keeps cards through KeepCardsInHandSupport's queue (Worldpurge).
- `EachPlayerMayDiscardHandThenDrawEffect`: each player chooses, then the accepted players act.
- `EachPlayerMayDiscardOneThenApplyEffectsEffect`: each may discard, the discards deferred to one event (Mog, Moogle Warrior).
- `EachPlayerMayDiscardOrLoseLifeEffect`: its own choose-then-act loop with deferred discards; takes each opponent once the opponents record moves onto it.
- `EachPlayerMayDiscardThenSearchBasicLandToHandEffect`: each may discard, the discards deferred, then who did may search (Borderland Explorer).
- `EachPlayerMayDiscardUpToThenTakeDamageEffect`: each chooses how many and which, discarded together, then damage (Mind Bomb).
- `EachPlayerMayDrawCardAndRestrictAttackingEffect`: one may-draw per player, the rider on who drew (The Second Doctor).
- `EachPlayerMayDrawCardThenGainLifeEffect`: one may-draw per player, then who drew gains (Kwain).
- `EachPlayerMayExileGraveyardCardsThenLoseLifeEffect`: each chooses from their graveyard, all exiled together, then the losses (Grave Consequences).
- `EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect`: each player chooses in APNAP order, then a per-player restriction.
- `EachPlayerMayPutLandFromHandThenOpponentsDrawEffect`: each player chooses secretly, then the lands enter together.
- `EachPlayerMayPutLandsFromHandToBattlefieldEffect`: each player chooses lands through PutLandsFromHandSupport's queue, which enter together (The Great Aurora).
- `EachPlayerMayReturnCardFromGraveyardToHandEffect`: one may per player, each with its own choice (Minamo).
- `EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect`: each player chooses, then the accepted players act.
- `EachPlayerNameCardRevealTopEffect`: every player names, then all reveal (Conundrum Sphinx); a miss also goes to the bottom, not the graveyard.
- `EachPlayerPaysAnyLifeForTokensEffect`: rounds of payments repeat until nobody pays, then each gets tokens (Plague of Vermin).
- `EachPlayerPutsCardFromHandOnTopOfLibraryEffect`: each player chooses a card in APNAP order through its own loop; the survivor asks one player.
- `EachPlayerReturnsCardsFromGraveyardToHandEffect`: the graveyard-return queue: each player chooses, then all return together (Ill-Gotten Gains).
- `EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardEffect`: each player chooses a type, then every chosen type returns (Patriarch's Bidding).
- `EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardToHandEffect`: each player chooses a type, then the graveyard-return queue (Grave Sifter).
- `EachPlayerReturnsCreatureToHandEffect`: each player chooses, then all return as one event (Curfew).
- `EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect`: each player reveals any number in turn, then tokens for each count (Kamahl's Summons).
- `EachPlayerSacrificesDownToCountEffect`: per-player amount (all but N); own each-player loop.
- `EachPlayerSacrificesDownToFewestEffect`: per-player amount (down to the fewest); own each-player loop.
- `EachPlayerSacrificesFractionRoundedDownEffect`: per-player amount (a fraction of what they control); own each-player loop.
- `EachPlayerSacrificesFractionRoundedUpEffect`: per-player amount (a fraction of what they control); own each-player loop.
- `EumidianWastewakerEffect`: Eumidian Wastewaker: 'you and defending player each discard a card or sacrifice a permanent' is each player choosing, then all acting (CR 101.4); the handler acts at each answer.
- `ExplosionOfRichesEffect`: Explosion of Riches: 'each other player may draw a card' is each asked in turn, then all draw (CR 101.4); no MayEffect over several players.
- `MakeAnExampleEffect`: all opponents separate (APNAP), then the controller chooses every pile, then one sacrifice batch (CR 608.2e); PendingMakeAnExample carries the separations and chosen creatures.
- `ManifoldInsightsEffect`: Manifold Insights: reveal the top ten, then starting with the next opponent in turn order each opponent chooses a different nonland card from among them, the chosen into your hand and the rest on the bottom in a random order: ChooseCardEffect takes one ….
- `MorphicTideAuraEffect`: no card (engine-built by MorphicTideEffectHandler): the Aura-host continuation of Morphic Tide / Glimpse of Tomorrow; goes with MorphicTideEffect.
- `MorphicTideBottomCardsEffect`: no card (engine-built by MorphicTideEffectHandler): the bottom-of-library continuation of Morphic Tide / Glimpse of Tomorrow; goes with MorphicTideEffect.
- `MorphicTideEffect`: Morphic Tide, Glimpse of Tomorrow (already one record): every player at once shuffles their owned permanents in, reveals that many and puts the revealed cards in type batches that enter together.
- `NaturalBalanceEffect`: Natural Balance: land counts read once, then each player with six or more lands chooses five and sacrifices the rest and each with four or fewer may search for up to five minus that many basic lands, in APNAP order through BasicLandSearchQueueSupport and the ….
- `OrderOfSuccessionEffect`: Order of Succession: starting with you, each player in turn chooses a creature controlled by the next player in the chosen direction, then each gains control of the creature they chose: one choice per player over a different player's creatures before anyone ….
- `RebirthAnteEffect`: Rebirth: each player may ante the top card of their library, then each who did has 20 life: AnteTopCardEffect(eachPlayer()) + a set-life step over the players who anted once the choose-then-act loop exists.
- `SeizeTheSpotlightEffect`: Seize the Spotlight: each opponent chooses fame or fortune in APNAP order, then you steal a creature per fame player and draw plus Treasure per fortune player: the choose-then-act loop (its own, correct).
- `ShuffleTargetPermanentsThenEachControllerMayCastEffect`: Guff Rewrites History: each player who controlled a shuffled permanent may cast the nonland card they exiled for free: a resolution cast offered to several players in turn, which no step does.
- `TargetPlayerExilesFromHandEffect`: each has its own per-player choose-then-exile loop and riders (play permission, return on leave, face down).

#### A subject or step result that does not exist yet (74)

- `ActivePlayerReturnsCardFromGraveyardToHandEffect`: declares the player target the trigger restricts, but the recorded active player returns: no subject for that player, and another return path (Oath of Ghouls).
- `AnimateChosenOwnPermanentEffect`: Turbocharged Escape's 'it perpetually becomes an artifact creature' needs a card subject naming a chosen permanent's card (cardsThisWay(CHOOSE) names chosen cards only); one user, no ledger row waits for that subject.
- `AttachAllAurasToAnotherPermanentEffect`: left (Glamer Spinners): folded as choose + attach over attachmentsOf(target(), Aura), no effect declares its 'target permanent' (attachmentsOf / controllerOf(target()) don't); needs a target-declaring step or subject.
- `AttachCurseToOpponentAndDrawEffect`: left (Lynde): two CHOOSE steps in one resolution (a Curse attached to you, then an opponent); the step results keep one outcome per kind, so the attach can't read both; the 'if you do' would be HappenedThisWay(ATTACH).
- `AttachTargetAuraToAnotherPermanentWithSameControllerEffect`: left (Simic Guildmage): no subject names 'the permanent target Aura is attached to' (its controller picks the candidates), and its target validator is keyed to the record.
- `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect`: the until-end-of-turn copy head: only the source becomes the copy; the target-to-target sibling needs a subject for the copier and removed supertypes (step 10 copy verb).
- `BecomeCopyOfTargetPermanentUntilEndOfTurnEffect`: two targets (copier and copied) and removed supertypes; the head has neither a copier subject nor that rider (step 10 copy verb).
- `BoostCreaturesInAttackingBandsEffect`: Chatzuk, Mighty Guitarist (MB2 playtest, class Oracle): the band is triggeringAttackers() still attacking in a band, but '+1/+1 for each creature in that band' counts a subject: subjects inside DynamicAmount are outside this plan.
- `BoostOpponentCreaturesByPoisonCountersEffect`: needs amounts read against each boosted creature (its controller's poison counters); BoostTargetCreatureEffect reads amounts against the source and choosing the amount source per subject is step 10's boost merge (5i hand-off).
- `BoostPairedCreatureOfTargetEffect`: target plus its soulbond partner: needs a paired-with subject (Joint Assault, one card).
- `ChoosePlayerOnEnterEffect`: read by AsEntersInteractionService only, with a two-player count the opponent sibling lacks; a candidate subject needs the opponent readers unified first.
- `ConjureCardIntoGraveyardThenDoubleAndShuffleEffect`: fixed in place under the owner's 'it' decision (CR 400.7, 400.7e): the dying Slime is the card in its graveyard stay (sourceObjectCard), doubled and shuffled only while still there; each card's own P/T doubled (CR 701.10b; a Clone's 0/0 card gains nothing).
- `CopyCardsExiledWithSourceAndMayCastCopiesEffect`: the subject exists (exiledWithSource()); left: the copy step CopyImprintedCardAndMayCastCopyEffect has no paid cast (normal, {1}, {3}) and a head of five cards needs it.
- `CopyDrawnInstantOrSorceryAndMayCastCopyEffect`: God-Eternal Kefnet: the copy's {2} reduction would be a survivor option only Kefnet sets (no other card copies with a reduction), and the copy is created in and cast from the hand (ruling; CR 707.12) where the survivor parks and casts from exile.
- `CopyEnchantedInstantAndMayCastCopyEffect`: Spellweaver Volute: needs a card subject for the card an Aura enchants as the Aura last existed (ruling), a copy created in and cast from the graveyard (ruling.
- `CounterlashEffect`: MayCastAnySpellFromHandWithoutPayingManaCostEffect needs a 'shares a card type with the target spell' filter read after the spell left the stack (it is countered first.
- `CreateTokenCopyOfSourceEffect`: a defect run, no merge; the copies are offered Esix's and Mirrormind Crown's first-token-creation replacement through CreateTokenCopyOfTargetPermanentEffectHandler.replaceFirstTokenCreation (CR 701.7a.
- `CreateTokenForImprintedCardOwnerEffect`: no player subject for the owner of the source's linked exiled card; two classes with the per-owner sibling (Severance Priest).
- `CreateTokensAndAttachEquipmentEffect`: the token step records CREATE_TOKEN, but 'attach an Equipment you control to it / each' has no primitive over createdThisResolution() (AttachOneOfControlledEquipment* are blocked on a picked subject).
- `CreateTokensForExiledCardsWithSourceEffect`: no player subject for the owners of the cards exiled with the source; a token per owner sized by that owner's cards (Skyclave Apparition).
- `DealDamageToRandomOpponentCreatureEffect`: a random pick is a choice, not a subject (as DealDamageToRandomOpponentEffect).
- `DealDamageToTargetCreaturesThenCreateTokensEffect`: needs a DynamicAmount counting the permanents a step result names (one token per creature dealt damage) and a multi-target-group damage primitive; Shellshock.
- `DefendingPlayerChoosesCreatureToBlockEffect`: Crashing Boars: ChoosePermanentsEffect.exactly(1, controlledBy(defendingPlayer(), untapped creature)).chosenBy(defendingPlayer()) fits, but MustBlockSourceEffect names its blocker only as its target (no blocker PermanentSubject.
- `DestroyGrantingPermanentIfNoCountersEffect`: Heliod's Punishment: 'then if it has no task counters on it' is a condition over the granting Aura, and Conditions take no subject (not in this plan); the grant's activation binding would also have to reach into a ConditionalEffect.
- `DestroyOtherCreaturesSharingColorWithEnteringCreatureEffect`: otherCreaturesSharing(triggering(), COLOR) with cannotBeRegenerated, once the subject reads a gone named permanent's last-known colors (Spreading Plague).
- `DestroyOtherPermanentsWithEnteringNameEffect`: otherPermanentsSharing(triggering(), NAME, any) with cannotBeRegenerated would express it, but the subject names nothing once the entering permanent left and the record reads its last-known name (Eye of Singularity).
- `DestroyTargetCreatureThenAmassGoblinsEffect`: amass by 'that creature's controller' is a bespoke continuation (CompleteAmassGoblinsEffect) with no player subject (Azog).
- `DestroyTargetCreatureThenCreateTokenEqualToPowerToughnessEffect`: 'if that creature dies this way' (Kalitas): no DIED step result (DESTROY also counts a destruction a replacement exiled).
- `DestroyTargetEquipmentAndDamageAttachedCreatureEffect`: 'the creature it was attached to' after the destroy (Unforge): no subject reads a departed target's attachedTo.
- `DestroyTargetPermanentThenEffect`: 75 of 89 cards split (runs 2-3); 14 left: manifest dread / token copy / exile-top riders with no player subject (Unwanted Remake, Dwarven Confluencer, Transforming Flourish), dies-this-way (Cinder Cloud, Kaervek's Purge, Tuktuk Scrapper), dealt-damage LKI ….
- `DestroyTargetPermanentThenPerpetuallyBoostEffect`: the hidden-zone logging decision (2026-11-23) settles its log, but Territorial Strike still waits on a part: the boost goes to the beheld object (StackEntry.beholdCardId, a permanent or a card in hand), which no subject names and which only this handler ….
- `DiscardCardThenEffect`: 41 of its 70 cards split (the same-resolution 'if you do' / 'then' ones: DiscardEffect[.matching] + ConditionalEffect(HappenedThisWay.discarded() | toACard(DISCARD, kind))).
- `DubiousChallengeEffect`: left. No step looks at the top N cards of a library and exiles up to M matching cards from among them, the rest staying in the library, recording EXILE: LookAtTopCardsEffect has no stay-in-library rest and records no EXILE.
- `EachControlledPermanentBecomesCopyOfTargetNonAuraPermanentEffect`: a set of copiers: the copier subject of the copy verb, which step 10 decides with the human sign-off (Mirrorform).
- `EachPlayerShufflesOwnedCreaturesIntoLibraryThenRevealsCreatureEffect`: the shuffle step takes no permanent filter and records no nontoken per player for "each player who shuffled a nontoken creature this way"; one card (Collision of Realms).
- `EnchantedCreatureFightsTargetCreatureEffect`: goes through FightSupport, the source sibling does not: one path, then a fighter subject.
- `ExileOneOfCombatOpponentsAndTrackWithSourceEffect`: ExileChosenPermanentsEffect chooses among permanents you control; no subject names the creatures that blocked or were blocked by the trigger's creature; one card (Godsend).
- `ExileTargetCardFromGraveyardWithDiscoveryCounterMayPlayThisTurnEffect`: the permission is one play among every card with a discovery counter: no subject for those and no permission group on the step (Lara Croft).
- `ExileTokensWithSameNameAsLeavingCreatureEffect`: no subject for tokens sharing a departed permanent's last-known name: otherPermanentsSharing needs it present (Dual Nature).
- `ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect`: no subject for tokens created with the source, which except(..., createdThisResolution()) would narrow; one card (Faerie Artisans).
- `EyeOfTheStormExileAndCopyEffect`: Eye of the Storm: 'that player copies ... may cast' casts for the triggering spell's caster, a caster subject on CopyImprintedCardAndMayCastCopyEffect only Eye would set, and 'exile it' is a non-target exile of the triggering spell with the source ….
- `FlipCoinForEachBlockingCreatureEffect`: not folded onto FlipCoinWinEffect + forEach: (1) whether 'for each blocking creature, flip a coin' is one set of flips (Edgar, King of Figaro's ruling) is unsettled and the three per-object records disagree (Rakdos flips at once, this and Mutalith one by ….
- `FlipCoinForEachMatchingPermanentDestroyOnLossEffect`: not folded: Rakdos's coins are one set of flips and the tails creatures are destroyed together (rulings); a FlipCoinWinEffect forEach would need the flip-event grouping decided (Edgar's ruling, see Fighting Chance's row) and a per-object FLIP step result ….
- `FlipCoinForEachOpponentEffect`: not folded: same flip-grouping question as Fighting Chance and Rakdos (Edgar's ruling), and 'that player' of each lost flip needs a per-flip player binding (today the handler swaps the entry's target for triggeringPlayer()).
- `GainActivatedAndTriggeredAbilitiesOfExiledCardsEffect`: a 'triggered abilities too' option on GainActivatedAbilitiesOfExiledCardsEffect would have Idris as its only user, since Koh (its other candidate) stays; fold both when a last-chosen card subject has a second user.
- `GrantControllerCreatureSpellsCantBeCounteredThisTurnEffect`: differs in spell filter, not subject: a CardPredicate would be one card's parameter; settle with the static can't-be-countered step-9 rows.
- `GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect`: its floating copy must keep the target player (a player subject bound at resolution, CR 611.2c; no PlayerSubject variant holds a fixed player) and 'spend mana as though it were mana of any color to cast spells this way', which only it sets.
- `GrantControllerSpellsCantBeCounteredThisTurnEffect`: differs in spell filter, not subject: a CardPredicate would be one card's parameter; settle with the static can't-be-countered step-9 rows.
- `GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect`: left: no subject or predicate for 'creatures you control blocking that creature' over a set; one card (Tidal Flats).
- `MayCastExiledCreatureCardsWithHatchingCountersEffect`: its hatching cards are exiled face down and the owner may look at them (NEO ruling); exiledCards' filter judges a face-down card without characteristics (CR 406.3a), so 'creature card with a hatching counter' needs a card subject over face-down cards the ….
- `OpponentChoosesOneOfTopTwoGraveyardCardsEffect`: Phyrexian Grimoire: no 'top two cards of your graveyard' card subject (TopOfGraveyard names one card; a count would be set by this card alone) and its harmful target opponent only chooses.
- `OpponentChoosesPermanentToExileUntilSourceLeavesEffect`: Wormfang Crab: ChoosePlayerEffect.opponent() + ChoosePermanentsEffect.exactly(1, controlledBy(you(), other)).chosenBy(playersThisWay(CHOOSE)) fits, but no exile step links a chosen (non-target) permanent to its source for ReturnAllCardsExiledWithSourceEffect: ….
- `PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect`: the handler now records the changes in the perpetual stores (types and Treefolk through PerpetuallyGrantCardCharacteristicsEffectHandler.grant, base P/T, keywords) instead of exchanging cards, so a copy copies the printed Forest (CR 707.2).
- `PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect`: 'becomes an artifact creature' keeps the card's other types (CR 205.1b, not 205.1a as fold run 12e wrote); the handler now adds ARTIFACT and CREATURE in perpetualCardTypes instead of writing the card's printed types, so it waits only for a card-subject form ….
- `PerpetualReduceRandomGreatestManaValueCreatureCardCostEffect`: Fuel Tank Feaster: 'a random creature card with the greatest mana value among creature cards in your hand' has no card predicate for the greatest mana value among the cards a subject names; one user.
- `PhaseOutSelfAndCombatOpponentEffect`: source and combat opponent (Dream Fighter): folds into PhaseOutEffect(allOf(source(), ...)) once the block trigger records the opponent as a subject can read it; not checked here.
- `PlayerChoosesUpToPermanentsThenSacrificesRestEffect`: Archfiend of Depravity: no verb sacrifices the set a subject names (SacrificeSelfEffect takes one permanent, SacrificeEachMatchingPermanentEffect a filter that reads no step result), so except(controlledBy(that player, creature), thisWay(CHOOSE)) has no ….
- `PlayersLoseLifeEffect`: engine-internal step with players and amounts fixed; not a subject.
- `PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect`: left: forEach fits but PreventDamageEffect takes scopes, not subjects ('prevent all combat damage that would be dealt by that creature this turn' over thisWay(CHOOSE)); AssignNoCombatDamageEffect differs (unpreventable damage); a one-card subject (Heroism).
- `PutCardToBattlefieldThenEffect`: the hand subject and the put step's RETURN exist; left: eight cards whose then-effects read the chosen card's id through the carrier entry, and attacking entry, which the put step lacks.
- `PutChosenCardFromHandOntoBattlefieldEffect`: payload of the spellbook and conjure follow-ups: their chosen object is no step result a CardSubject reads; two cards.
- `RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffect`: recipient is a player here and a permanent in its sibling: no subject names both.
- `RedirectTargetCreatureNextDamageFromChosenSourceToSelfEffect`: recipient is a player here and a permanent in its sibling: no subject names both.
- `ReturnCreaturesBlockingOrBlockedByTargetEffect`: no subject or predicate names what blocks or is blocked by the target; one card (Trial).
- `ReturnedPermanentFightsTargetCreatureEffect`: left: 'it fights' needs the returned permanent as the reflexive's fighter; no fight verb takes a fighter subject and QueueReflexiveAbilityEffect has no about-returned form (aboutPickedPermanent can't be reused: 7 handlers read picks).
- `ReturnTargetPermanentToHandThenEffect`: 11 of 12 cards split (runs 2-3); left: Ashiok (TargetPlayerExilesFromHandEffect has no subject).
- `RevealTopCardDealManaValueDamageEffect`: one damage event to a player and each creature they control: the mass damage takes no player subject; one card (Cerebral Eruption).
- `RevealUntilCreatureToBattlefieldRestToLibraryThenDemonPowerDamageEffect`: damage dealt by the returned permanent to each opponent has no subject; one card (Aspiring Champion).
- `SacrificeEnchantedCreatureOnLeaveEffect`: reads the departed Aura's chosenPermanentId when it never attached (Animate Dead family); no subject reads it.
- `SearchLibraryForInstantOrSorcerySharingSourceColorAndMayCastEffect`: Kasmina needs a colour-sharing card predicate over a permanent subject; it would have one user (CardSharesColorWithControlledPermanentPredicate's Circadian Struggle-style users are unbound and static-context); fold when a second card needs it.
- `SearchTargetPermanentControllerLibraryForSameNameToBattlefieldEffect`: Mimeofacture searches the library of the target permanent's controller; SearchTargetLibraryEffect searches only the target player's library, and a library-owner subject would be set by this card alone (guard rail).
- `SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffect`: its seek is now SeekLibraryEffect's (Fixed(1), filter, BATTLEFIELD, X bound) through SeekLibraryEffectHandler, so the card passes through the hand (Grafdigger's Cage no longer stops it) and enters with its enter abilities.
- `TapAndPreventCombatDamageByTargetAndBlockersEffect`: Feint: needs a creatures-blocking-[subject] PermanentSubject and a prevent-all-combat-damage-dealt-by-[subject]-this-turn record.
- `TurnSourceFaceUpOrExileAndMayCastEffect`: Etrata, Deadly Fugitive (granted ability): 'If you can't [turn it face up], exile it, then you may cast it' needs a turn-face-up step result (no StepKind records one) and the one turn-face-up path (this calls GameService's, Showstopping Surprise ….

#### A parameter only one card would set (68)

Folding would add a component only this card sets (the guard rail "never add a parameter only one card sets"); fold when a second user appears.

- `AddOnePlusOneCountersToModularAbilityEffect`: Zabaz, the Glimmerwasp: a placement-source condition (only counters a modular triggered ability puts) threaded through the counter-placement paths for this one card.
- `AjaniUltimateEffect`: LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect ties "reveals" (public log and prompt) to its bottom-random rest (Gishath); Ajani, Outland Chaperone only looks and shuffles; a look-vs-reveal option would be set by this card alone.
- `AllowPlayFromAnyLibraryTopEffect`: a library-owner PlayerSubject on AllowCastFromTopOfLibraryEffect / PlayLandsFromTopOfLibraryEffect would be set by this card alone (eachPlayer(), read from a face-up plane) while Xanathar's record stays; folds with it.
- `AnimateReturnedPermanentIfNotCreatureEffect`: Brilliance Unleashed's 'if it's an artifact creature card' judges the card, not the permanent: HappenedThisWay.toA(RETURN, filter) and the subjects judge the permanent as it is (a returned noncreature artifact under March of the Machines would not animate).
- `AttachSourceAuraToRandomOpponentEffect`: left (Maddening Hex): ChoosePlayerAtRandomEffect(except(eachOpponent(), enchantedPlayer())) has no filter for the opponents the Aura could enchant, which the handler narrows to; a player filter with one user.
- `AttachTargetEquipmentsToTargetCreatureThenEffect`: left (Thorin): the reflexive 'that creature deals damage equal to its power' is sourced at the host and fires only on a real attach; the attach verb's continuation is sourced at the entry's source and also counts an Equipment already attached.
- `BecomeCopyOfTargetCreatureWhileTargetTappedEffect`: Zygon Infiltrator: the copy itself is BecomeCopyOfSubjectEffect.of(target()), but its duration ('for as long as that creature remains tapped') is anchored on the copied target (beginCopy with the target as the duration's source).
- `BecomeTargetPermanentCopyOfTriggeringSpellUntilEndOfTurnEffect`: The Myriad Pools copies the triggering permanent spell (its copiable values on the stack, a face-down spell's included, else its card); no CardSubject names a spell on the stack and it is the one user; the copier half (.by(target())) exists now.
- `BoostByBushidoEffect`: a bushido-value amount of the amount source would be read only by Takeno (guard rail: no one-card parameter).
- `BoostSelfOrPutCounterIfExactlyThreeColorsEffect`: Paragon of Modernity: the ConditionalReplacementEffect shape fits, but no Condition reads the colours of mana spent to activate (ConditionContext carries only the Treasure flag, ColorsSpentToCast reads casts).
- `ChooseCardFromTargetHandToBattlefieldEffect`: Treacherous Urge split onto the steps and the targeted form went; left: Zara puts the card attacking, with a choice of whom (CR 508.4), and PutCardsOntoBattlefieldEffect has no entering attacking; one card.
- `ChooseNumberForCreatedPermanentEffect`: the token copy records no CREATE_TOKEN for "if it's a Saga": it can pause mid-step (Esix, one of several triggering) and end in a continuation; one card (Esper Terra).
- `CounterTriggeringSpellAndCreateTokensEffect`: CounterSpellEffect counters only the entry's target; this counters the triggering spell with the caster baked as the target; one card (Dovescape).
- `DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect`: Willie Lumpkin's attack ban lasts 'during their next turn', measured from the attacking player; CreaturesCantAttackControllerUntilNextTurnEffect's durations are measured from the resolving controller, so the fold needs a duration only this card sets.
- `DamageEachDestroyedPermanentControllerUnlessPaysEffect`: left: the objects departed (destroyed this way, last-known controller) and the unpaid damage is summed per player: a departed-object loop and a per-player amount for this one card (Stench of Evil).
- `DrawAndRandomDiscardWithSharedTypeCountersEffect`: Rowdy Crew (the draw is already DrawCardEffect(3)): DiscardEffect(2, you(), random) + PutCountersOnSourceEffect would split it, but 'if two cards that share a card type are discarded this way' is a DISCARD-step read only this card makes.
- `DrawIfLibraryLargerElseMillOpponentsEffect`: needs an 'otherwise' branch judged once on ConditionalEffect (the draw changes the library sizes, so a second NotCondition step judges them again: 6 vs 5 draws, then 5 vs 5 also mills; Oracle: 'Otherwise, each opponent mills five').
- `EachTargetPlayerDrawsCardsEqualToAttachedCountEffect`: no amount counts the permanents attached to the triggering player (only DealDamageToPlayersEffect reads that count, by its own component); one card (Curse of Surveillance).
- `EarthbendTargetLandThenFightEffect`: Earth Rumble: the reflexive 'up to one target creature you control fights target creature an opponent controls' needs two target groups (one optional) declared for an ability a resolution makes.
- `EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect`: Overwhelming Splendor: needs a static form of the player restriction (PlayerRestriction with a PlayerSubject judged as checked, enchantedPlayer() here) and an activating kind that excepts loyalty abilities.
- `ExileAllMatchingCardsFromLibraryWithSourceThenEffect`: no step exiles every matching card of a library; one card waits for it (Caldera Breaker).
- `ExileTargetCreatureAndSameNameControlledByItsControllerThenInvestigateEffect`: the same-name set exists (othersItsControllerControlsSharing); the exile step records no token flag for "each nontoken creature exiled this way" (a token-topped merged permanent leaves cards); one card (Declaration in Stone).
- `ExileTargetCreatureDrawPowerReturnAtControllerUpkeepDiscardToughnessEffect`: the discard reads the toughness of the returned permanent (ruling): no amount over what a RETURN step returned; one card (Vanish into Memory).
- `ExileTargetNonlandCardFromGraveyardAndSearchSameNameEffect`: the same-name cause is built for exiles; this searches to a hand: SearchLibraryEffect has no "any number of cards with that name" (its remaining count is the amount given, so the prompt would miscount) and no card filter over the exiled card.
- `ExileTargetNonlandPermanentAndCardWithSourceEffect`: both targets are chosen as card ids by the battlefield-or-graveyard choice, which the permanent and graveyard exile steps don't read; one card (The Spot).
- `ExileTopCardOfEachLibraryAndGrantLesserManaValueFreePlayEffect`: no predicate comparing to one card of the exile step, the controller's (Triple Triad).
- `ExileTopCardOfEachLibraryFaceDownAndGrantFreePlayPermissionEffect`: a face-down, free permission over each library: the face-down library primitive has no free-cast rider, which only this card would set (Extract Power).
- `ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect`: run 11's stay premise was false (the link ends as the card leaves exile, tested); left: ExileTargetPermanentEffect(triggering()) can't link what it exiles to the source, one card.
- `ExileUntilCardPredicateMayPlayWhileSourceControlledEffect`: the random bottom step exists (PutCardsIntoLibraryEffect); left: AllowPlayCardsFromExileEffect has no while-the-source-remains binding; one card (The Day of the Doctor).
- `GainControlOfTargetAuraEffect`: Aura Graft: 'attach it to another permanent it can enchant' is an option only Aura Graft would set on the attach records (same-type and same-controller moves, which also differ in chooser and protection checks); one card.
- `GainControlOfTargetCreatureOfChosenPlayerEffect`: Preacher: GainControlOfTargetEffect.opponentChosenTarget exists only for a spell's cast-time choice; an activated ability's opponent-chosen target (the two target groups, TargetLegalityService's player-index check) needs that path on activation first; one card.
- `GoadSourceCreatureWhileControllerControlsItEffect`: Vislor Turlough's "goaded for as long as they control it": the duration follows the new controller's control of the source while the goader stays you; no Duration says that (SOURCE_CONTROLLED is the creator's), and one card only.
- `GrizzledHuntmasterEffect`: 'choose a creature card you own from outside the game' has no CardSubject (beginOutsideGameCardChoice's one user is this handler).
- `HeadGamesEffect`: Head Games: the split (PutCardsIntoLibraryEffect.onTop(cardsInHands(targetPlayer())) recording PUT_INTO_LIBRARY before its order prompt + SearchTargetLibraryEffect(AmountThisWay(PUT_INTO_LIBRARY), null, HAND, false)) needs an unrevealed HAND destination: ….
- `IncreaseCastCostForNextMatchingSpellEffect`: left: Stalwart Realmwarden's 'the next noncreature spell target opponent casts' needs a player float that ends on that player's matching cast (a new Duration read by DurationClock on the cast event); only this card would set it.
- `LandsOfChosenTypeBecomeChosenBasicTypeUntilEndOfTurnEffect`: Vision Charm's first pick is any land type (nonbasic ones too) and the set is the lands of that type; the survivor has no land-type choice step or chosen-land-type predicate, and only this card would set them.
- `LookAtTopCardsHandTopBottomEffect`: three-way: folding Expressive Iteration's HandBottomExileChoice into HandTopBottomChoice changes the client prompt (handBottomExile and handTopBottom are separate frontend flows); an effect-level fold alone would add a destination only one card sets.
- `MillControllerAndDealDamageByHighestManaValueEffect`: Heretic's Punishment: "the greatest mana value among the milled cards" is a maximum AmountThisWay has no stat for; the only user in the queue (Ill-Timed Explosion's GreatestDiscardedCardManaValue reads a discard follow-up, not a step), so a GREATEST stat ….
- `MirrorOfFateEffect`: Mirror of Fate: ChooseCardEffect.upTo(exiledCards(you(), filter), 7) + ExileControllerLibraryEffect() + PutCardsIntoLibraryEffect.onTop(cardsThisWay(CHOOSE)) would fold it, but 'face-up exiled cards you own' has no filter: InExile's filter sees a face-down ….
- `NihiloorEnterEffect`: Nihiloor: for each opponent, tap up to one untapped creature you control, and one reflexive ability per creature tapped this way, each naming that opponent and that creature.
- `PerpetuallyBoostTriggeringCardByPowerEffect`: fixed in place under the owner's 'it' decision (CR 400.7): the triggering spell's card through Subjects.cardsOf(Subjects.triggeringSpell()), so a countered spell is not boosted (Rothga).
- `PerpetuallyGainKeywordsOfTriggeringCreatureEffect`: fixed in place under the owner's 'it' decision (CR 400.7): the source permanent's own card through SubjectResolutionService.sourceObjectCard, nothing once the Pupa left the battlefield.
- `PerpetuallySetRandomLandInLibraryAsBasicIslandEffect`: Hydroponics Architect: no perpetual supertype store (basic) on PerpetuallyGrantCardCharacteristicsEffect; one user. It writes printed values into a runtime copy.
- `ProtectionFromRingBearersEffect`: no Ring-bearer PermanentPredicate (only the SourceIsRingBearer condition), one user; wait for the ring batch's Ring-store merge, then fold as ProtectionFromPermanentsMatchingEffect(a Ring-bearer predicate).
- `PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect`: the head offers face-down cards one by one where this chooses a pile: that rule moves to the head first; one card (Ruin Processor).
- `PutTargetCardsFromGraveyardOnBottomOfLibraryEffect`: targetCard names one target, this has up to four; one card (Bow of Nylea).
- `PutTargetCreatureCardFromGraveyardOnTopAndDealPowerDamageEffect`: PutCardsIntoLibraryEffect takes a targetCard; left: no amount of the moved card's power as it was in the graveyard; one card (Dead Reckoning).
- `PutTargetExiledCardOwnedByDamagedPlayerOnBottomOfOwnersLibraryAndGainLifeEffect`: targetExiledCard exists; left: an owner that is the damaged player, bound as the target is chosen; one card (Time Reaper).
- `PutTargetOnBottomThenRevealUntilSharedCardTypeToBattlefieldRestOnBottomRandomEffect`: the filter exists now (CardSharesCardTypeWithPermanentsPredicate, bound by the reveal-until step); left: PutTargetOnBottomOfLibraryEffect records no DepartureResult (last-known card types) for departedThisWay to read; one card (Reality Scramble).
- `PutTargetPlayerHandOnLibraryThenSearchThatManyToHandEffect`: SearchLibraryEffect has no exact count from a step for another player's library and hand; one card (Jester's Mask).
- `PutTimeCountersOnTargetExiledCardWithSuspendEffect`: targetExiledCard exists; left: a put-into-exile-from-your-graveyard-this-turn qualifier; one card (Soovril).
- `RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect`: left out of the Duration fold: its watched player is a permanent target's controller or the attacked player (Nuka-Nuke Launcher's defending player), which the 52-card survivor can't take (RulesEventPipeline makes the watched player the triggered entry's ….
- `ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect`: the face-down entry exists; left: the entry step records no permanent for a turn-face-up step to name; one card (Yarus).
- `ReturnOpponentCreaturesFromGraveyardAsFoodEffect`: no CardSubject for the cards put into graveyards from the battlefield this turn; one card (Supper for Spiders).
- `ReturnOpponentsCreaturesWithToughnessLessThanSacrificedEffect`: no predicate compares toughness with an amount (the exploited creature's); one card (Profaner of the Dead).
- `ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffect`: a resolution choice capped by the event power; the chosen-return head caps mana value only; one card (Moorland Rescuer).
- `ReturnPermanentSharingTypeWithEnteringPermanentToHandEffect`: no shared permanent type quality, and the entered permanent is read as it last existed once it left; one card (Cloudstone Curio).
- `ReturnTargetCreatureCardsFromGraveyardToBattlefieldFaceDownAsCybermenEffect`: the face-down entry exists; left: targetCard names one target and the put step declares none; one card (The Moonbase).
- `ReturnTargetPermanentToHandAndReduceControllerSpeedEffect`: no reduce-speed step; one card (Spikeshell Harrier).
- `RevealHandChooseCreatureGainLifeDiscardEffect`: "that creature card's toughness" of a chosen card: no amount reads the CHOOSE step's cards; one card (Talara's Bane).
- `RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect`: no card predicate "shares a creature type with the triggering permanent" as it last was; one card (Heirloom Blade).
- `RevealUntilTwoNonlandCardsWithManaValueAndCastOneEffect`: the exile-until step stops at one match, and the pick is the cast-one-put-other search destination; one card (Invasion of Alara).
- `SacrificeArtifactThenDealDividedDamageEffect`: Kuldotha Flamefiend: StackedDividedDamageTriggerService announces the division by this class (zero targets allowed); a divided step behind a sacrifice gate needs that service to read through MayEffect/SequenceEffect/ConditionalEffect, an ….
- `SacrificePermanentsOrLoseGameEffect`: Lich: "sacrifice that many ... If you can't" needs HappenedThisWay.atLeast with a DynamicAmount minimum (EventValue); the condition takes an int, one user.
- `SacrificeTriggeringPermanentThenConjureDuplicateEffect`: Prototype X-8: the duplicate goes onto the battlefield (ConjureDuplicateOntoBattlefieldEffect, not the CONJURE step) and 'perpetually becomes a Robot artifact' needs a perpetual card-type grant over a created permanent ….
- `SeekThreeLandsThenChooseEffect`: Song of Seasons: 'seek a Mountain card, a Forest card, and a non-Mountain, non-Forest land card' is one seek of three filters (one SEEK step); a one-card-per-filter option on SeekLibraryEffect would have this card only (Bounty of the Deep is not implemented).
- `SuspectChosenOtherCreatureEffect`: Frantic Scapegoat: 'if you do, this creature is no longer suspected' needs a verb that removes the suspected designation, which only this card has.
- `TargetCreatureDealsPowerDamageToEachTargetEffect`: Spinning Wheel Kick; the bite would need every recipient of its group, and the AI scores this record as creature damage (canDamageCreatures, TargetPower) where the bite's group form is not; one card.

#### Costs, "unless" and payments (51)

- `AllowPlayExiledCostCardThisTurnEffect`: Primordial Mist moved to AllowPlayCardsFromExileEffect.thisTurn(exiledAsCost()) and the this-turn mode deleted; Chitinous Crawler's undurationed 'you may play it' (CR 608.2g, a land too) needs a normal-cost play-now offer over a card subject.
- `AllureOfTheUnknownEffect`: 'That opponent may cast the exiled card without paying its mana cost' needs a cast-now step cast by another player (MayCastDiscardedCardWithoutPayingManaCostEffect offers only the controller.
- `BoostOwnCreaturesByManaSymbolEffect`: a chroma amount of the amount source's own mana cost would be read only by Light from Within.
- `CantAttackOrBlockUnlessTapEffect`: a non-mana combat cost (tap an untapped creature not declared this combat) through CombatTapCostEffect / CombatTapCostService and the AI; one user, a payment mechanic the mana survivor lacks; step 10's cost-combinator merge.
- `CantAttackUnlessReturnToHandEffect`: a non-mana attack cost (return N matching permanents) picked and paid by AttackReturnToHandCostService at declaration; the mana survivor has no payment path for it and only this card would set a return cost.
- `CopySpellForEachOtherCreatureWithManaEffect`: Feather, Radiant Arbiter, cost-shape: 'pay {2} for each of those creatures' is paid from the pool inside the record's own choice completion (no mana abilities, open defect).
- `EachCreatureControllerSacrificesPermanentUnlessPaysEffect`: left: each player chooses which of their creatures to pay for in one choice and sacrifices that many permanents together (rulings); not one yes/no per object.
- `EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect`: left: the object is chosen per player (ties) and the ransom is that creature's mana cost: a ransom read from the object, one card (Tariff).
- `EmblemGrantsFlashbackEffect`: not folded: Jaya's emblem as CastSpellsFromGraveyardEffect(instant or sorcery) needs an exile-instead option no other user of that record sets (guard rail), and emblem graveyard permissions are not read by ….
- `ExileCreatureCost`: a payment: the self cost is paid automatically by the ability, the creature cost is a chosen spell or ability cost; merge with the cost heads in step 10.
- `ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect`: 'cast the copy if able' with the order choice (Spelltwine) and the random single copy (Mysterious Stranger) are one card each on CopyImprintedCardAndMayCastCopyEffect; Bloodthirsty Adversary's X targets ride a pay-any-number reflexive.
- `ExileSacrificedCardsFromGraveyardEffect`: no card subject for the cards a cost sacrificed (Sword of the Ages).
- `ExileSelfAndBecomeForetoldEffect`: BecomeForetoldEffect exists now; folding it would take three parameters only this card sets (a face-down permanent exile, the owner as the foretelling player, the card's own foretell cost); one card (The Foretold Soldier).
- `ExileSelfCost`: a payment: the self cost is paid automatically by the ability, the creature cost is a chosen spell or ability cost; merge with the cost heads in step 10.
- `ExileTopCardMayCastOrDealDamageEffect`: no cast-now step that pays the cost and records whether it was cast (Chandra, Torch of Defiance; as Vaan).
- `ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffect`: no cast-now step that pays the cost and records whether it was cast (Vaan; as Chandra, Torch of Defiance).
- `GainControlOfTargetUntilRansomEffect`: Squidnapper: the ransom ability names the player control was taken from and the source id, known only at resolution; a static GrantActivatedAbilityEffect can't name them until PayRansomEffect and the activation check read the grant source; one card.
- `IncreaseSpellCostForTargetPlayerUntilEndOfTurnEffect`: left: Academy Loremaster's tax floats with the drawing player as its controller; as a GrantStaticEffectToPlayerEffect over that player the float's controller is Loremaster's controller, and the cost collector judges a floated modifier's scope against the ….
- `KillingWaveEffect`: left: each player chooses which creatures to pay X life for, the life paid and the creatures sacrificed at once (rulings): one life payment event, not one per object.
- `LandDropCost`: Boulder Jockey (playtest, judged from its class): the {D} land-drop additional cost; a cost head for step 10's cost-combinator merge with May's MayPayLandDropEffect (owner's list).
- `MayHaveThisLandEnterTappedForRadCountersEffect`: Mariposa Military Base: owner's decision 2026-11-23 (run 25): one of May's 11 cost combinators, step 10's cost-combinator merge; cost-shape.
- `MayPayLifeForColorlessManaUntilEndOfTurnEffect`: Channel: owner's decision 2026-11-23 (run 25): listed with May's 11 for step 10 (a timed special action, read by AbilityActivationService); cost-shape.
- `MayReturnPermanentToHandAndEnterWithCountersEffect`: Rescuer Sphinx: owner's decision 2026-11-23 (run 25): one of May's 11 cost combinators (an as-enters return), step 10; cost-shape.
- `OpponentChoosesPermanentToSacrificeEffect`: Demonic Hordes, cost-shape: the sacrifice is a ForcedCostOrElseEffect fallback run through DestructionSupport.resolveForcedCostElseEffects' per-class list on a carrier entry, where a ChoosePlayerEffect + ChoosePermanentsEffect.chosenBy(playersThisWay(CHOOSE)) ….
- `OpponentGainsControlOfSourceCreatureEffect`: "an opponent" is a choice at resolution (CR 608.2d) with no subject; the handler takes the first opponent by seat (right for two players) and DestructionSupport calls it as a forced-cost fallback.
- `OpponentMayGainControlOfCreatureYouControlEffect`: Infernal Denizen, cost-shape: a ForcedCostOrElseEffect fallback (DestructionSupport's per-class list returns once its may prompt starts).
- `PayEnergyThenDestroyTargetPermanentEffect`: the energy ransom exists (PayEnergyCost(TargetManaValue)); its 'when you do, destroy that permanent' is a reflexive ability about the declared target, which QueueReflexiveAbilityEffect would target again (Behemoth of Vault 0).
- `PayLifeOrPayManaCost`: Redirect Lightning: a choice between two costs (5 life or {2}) as an additional cost, with payment units for waived base costs.
- `PerpetuallyReduceCostForHandCardsSharingTargetSpellEffect`: Absorb Energy: 'that spell' is the target spell, read also when it can't be countered (CR 608.2h); CardSharesCardTypeWithCardsPredicate binds a CardSubject and cardsThisWay(COUNTER_SPELL) names nothing when the counter fails.
- `ProtectionRacketEffect`: Protection Racket: repeat a process for each opponent in turn order (reveal the top card, that opponent may pay life equal to its mana value, exile it, else into your hand): no for-each-player repeat over a step sequence; one card.
- `PutCardsFromGraveyardOnBottomOfLibraryCost`: a payment: activation and forced-cost paths of its own; cost merges are step 10's.
- `PutCounterOnSourceCost`: an activation payment with a P/T counter shape; its name sibling is a cumulative-upkeep choice; counter costs merge in step 10.
- `PutSelfOnBottomOfOwnersLibraryCost`: a payment consuming the source; cost merges are step 10's.
- `ReduceAuraCastCostForTargetingAttachedPermanentEffect`: left with targets-x (Strong Back): read in computeTargetBasedCostReduction's attached branch; waits for the target-cost reader redesign (see ReduceOpponentCostForTargetingControlledPermanentEffect).
- `ReduceEquipCostForTargetingAttachedPermanentEffect`: left with targets-x (Strong Back's equip half): an attached() option on ReduceActivatedAbilityCostForTargetingSourceEffect would have one user; with the spell-side targets-[X] record after the target-cost reader redesign.
- `ReduceOpponentCostForTargetingControlledPermanentEffect`: left with targets-x: the target-dependent reductions are read by separate ad-hoc readers (CastingCostService.getTargetingSpellCostModifier, computeTargetBasedCostReduction with first-category-wins early returns, one GameActionAvailabilityService playability ….
- `ReduceOwnCastCostIfTargetingEnchantedPlayerEffect`: left with targets-x (Infectious Curse): read in computeTargetBasedCostReduction's enchanted-player branch and its own instant/sorcery-only playability preview.
- `ReduceOwnCastCostIfTargetingGraveyardCardEffect`: left with targets-x (No One Left Behind): a graveyard-card target kind on ReduceOwnCastCostIfTargetingPermanentEffect is read by a different branch of computeTargetBasedCostReduction; waits for the target-cost reader redesign.
- `ReduceTargetedCostEffect`: left with targets-x (High Troller, spells and activated abilities): read by getTargetingSpellCostModifier and the activated-ability reducers; waits for the target-cost reader redesign (see ReduceOpponentCostForTargetingControlledPermanentEffect).
- `ReturnCreatureToHandCost`: a payment: the self cost is paid automatically by the ability, the creature cost is a chosen spell or ability cost; merge with the cost heads in step 10.
- `ReturnMultiplePermanentsToHandCost`: its 14 ransom cards moved to ReturnPermanentToHandCost.youControl(count, filter, description); left: 26 activation-cost cards, which need the activation-cost path to read ReturnPermanentToHandCost.count.
- `ReturnSelfToHandCost`: a payment: the self cost is paid automatically by the ability, the creature cost is a chosen spell or ability cost; merge with the cost heads in step 10.
- `ReturnTargetCreatureCardToHandAndPerpetuallyModifyEffect`: Veko's 'becomes a Spirit' replaces the creature types (CR 205.1a) through PerpetuallyGrantCardCharacteristicsEffect.becomingCreatureTypes; base P/T and the alternative cost go to the perpetual stores, the card is no longer exchanged (CR 707.2); one card.
- `ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect`: the cost step and cardsThisWay(RETURN) exist now; left: the spell-or-nonland-permanent return records no step and its sibling has another target form (Nightclub Bouncer, Talion).
- `SacrificeCreatureCost`: a payment: the self cost is paid automatically by the ability, the creature cost is a chosen spell or ability cost; merge with the cost heads in step 10.
- `SacrificeGrantingPermanentAndControllerDrawsEffect`: a granted activated ability binds no granter() at activation, and controllerOf(granter()) has no last-known controller once it is sacrificed; Hold for Ransom.
- `SacrificeSelfCost`: a payment: the self cost is paid automatically by the ability, the creature cost is a chosen spell or ability cost; merge with the cost heads in step 10.
- `SeekInstantOrSorceryAndMayCastFreeEffect`: Karlach's blue face: 'until end of turn, you may cast that card without paying its mana cost' needs GrantFreeCastForNextMatchingSpellEffect bound to cardsThisWay(SEEK) (a card-subject binder in its handler) that only this card would set.
- `SharedColorDiscardAlternativeCostEffect`: Dream Halls' discard is a non-mana alternative cost with a choice the cast request carries (sharedColorDiscardHandCardIndex: PlayCardRequest, SpellCastingService, GameActionAvailabilityService, the projection, AiDecisionEngine, the frontend).
- `TransmuteArtifactSearchEffect`: cost-shape; would be SearchLibraryEffect(artifact, REVEAL_ONLY) + PutCardsOntoBattlefieldEffect(cardsThisWay(SEARCH, mana value <= the sacrificed artifact's)) + MayPayManaEffect.dynamic(difference) over the rest, but no card filter compares a mana value to ….
- `WebSlingingEffect`: Amazing Spider-Man's text is a keyword grant (each legendary coloured spell you cast has web-slinging {G}{W}{U}, CR 702.188a), not a plain alternative cost: a cast using it is a web-slinging cast (Scarlet Spider's WebSlingingReturnedCreatureManaValue reads ….

#### A delayed trigger shape (25)

Unblocked by putting the old delayed mechanisms onto `RegisterDelayedStepTriggerEffect` / `RegisterDelayedEventTriggerEffect`.

- `ChosenNameSpellCastTriggerEffect`: the triage's RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(CardHasSourceChosenNamePredicate) reads the Saga's chosen name at cast time and finds none once Medomai's Prophecy left.
- `ConjureDuplicateOfTriggeringCardIntoHandEffect`: the card axis: copies the triggering card's runtime card with a delayed discard, unlike the printing lookup of its sibling.
- `CreateTokenAndLinkToSourceEffect`: the token step records CREATE_TOKEN, but the source link and the per-token delayed triggers have no step over createdThisResolution().
- `DealDamageToDamageSourceCreatureOrSpellControllerEffect`: Tephraderm: DamageTrigger's damageSourceFilter is judged only by a delayed ability's collector and has no spell-source form, so the card's two abilities (a creature source, a spell source) can't be two dealtDamage() triggers yet.
- `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect`: 'target card chosen at random' plus a next-upkeep cast delayed trigger bound to the exiled card (Power Pack).
- `ExileSelfAtEndStepEffect`: delayed (DelayedPermanentActions) and has the legacy source-to-targetId fallback; no target sibling on the same mechanism (as 5a's DestroySelfAtEndStepEffect).
- `NecropotenceSetAsideTopCardEffect`: Necropotence: the steps exist (ExileTopCardOfOwnLibraryEffect.of(you()).exiledFaceDown() + RegisterDelayedStepTriggerEffect.aboutCardAtNext(your end step, cardsThisWay(EXILE), PutCardsIntoHandEffect)), but neither is a CardDrawingEffect, which ….
- `PhaseOutAttackingCreatureAtEndOfCombatEffect`: delayed end-of-combat phase-out via TemporaryGlobalTriggeredAbility (payload now PhaseOutEffect(named(id))); waits for the delayed-mechanism unification.
- `PutAllCardsExiledWithSourceIntoOwnersHandsAndDiscardAtNextTurnEndStepEffect`: no discard over a CardSubject for a delayed ability about cards put into a hand; one card (Kardum).
- `PutCommanderFromCommandZoneOntoBattlefieldEffect`: the command-zone subject exists; left: a delayed ability about what the put step put and a return-to-command-zone effect; one card (Hellkite Courser).
- `PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect`: one choice over two zones and a delayed return attached to what the put step put; one card (Next of Kin).
- `PutMinusOneCounterOnSourceAtEndOfCombatEffect`: an end-of-combat delayed ability about the source: waits for the delayed-mechanism unification noted in 5a, a redesign outside this plan.
- `PutSelfOnTopOfLibraryAtEndOfCombatEffect`: an end-of-combat DelayedPermanentAction: waits for the delayed-mechanism unification noted in 5a, a redesign outside this plan.
- `RegisterDrawCardsAtNextEndStepEffect`: registers a delayed draw; EndStepDrawRecipient is you()/triggeringPlayer(), convert with the delayed-trigger work.
- `RegisterDrawCardsAtNextUpkeepEffect`: registers a delayed draw; UpkeepDrawRecipient needs controllerOf(targetSpell()) and ownerOf(targetCards()).
- `ResolveDelayedPermanentActionEffect`: no card (engine-built payload of DelayedPermanentActions' delayed sacrifice, destroy and exile): goes with the delayed-mechanism unification onto RegisterDelayed* (5a's blocked rows).
- `ResolvePendingExileReturnEffect`: no card (engine-built payload of PendingExileReturns, the exile-until returns; its exile-entry version is the stay check): goes with the delayed-mechanism unification onto RegisterDelayed*.
- `ReturnCombatOpponentToHandAtEndOfCombatEffect`: schedules a TemporaryGlobalTriggeredAbility with a baked ReturnToHandEffect.target(), not DelayedPermanentActions; fold after the delayed-mechanism unification noted in 5a.
- `ReturnEnchantedCreatureAndSelfToHandAtEndOfCombatEffect`: one delayed trigger returning Aura and host simultaneously (TemporaryGlobalTriggeredAbility); scheduling each through DelayedPermanentActions would split the event.
- `SacrificeAtEndOfCombatEffect`: delayed end-of-combat ability with a damage rider.
- `SacrificeCreatedPermanentsAtNextTurnEndStepEffect`: Kav Landseeker's 'at the beginning of the end step on your next turn, sacrifice that token' (its ruling: every token the replacement adds too) needs a sacrifice over a set of created permanents in a delayed ability waiting for a later turn: ….
- `SacrificeOwnCreaturesAtEndStepEffect`: captures a set into one DelayedPermanentActions trigger; its siblings schedule one TemporaryGlobalTriggeredAbility each (5j): one scheduling path first.
- `SacrificeSelfAtEndStepEffect`: delayed: TemporaryGlobalTriggeredAbility, not DelayedPermanentActions (see 5a).
- `SacrificeSelfAtNextCleanupEffect`: delayed cleanup-step sacrifice.
- `SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffect`: the delayed trigger's 'that Vehicle' rides as the entry's targetId; SacrificeSelfEffect's target() would declare a target; Goblin Crash Pilot.

#### Next-draw replacement payloads (DrawService) (24)

The payload is read inline by `DrawService`; unblocked by a "this draw was replaced" record there.

- `AbundanceDrawReplacementEffect`: Abundance: an optional static draw replacement (land or nonland, reveal until, rest on the bottom) whose payload DrawService runs inline; a payload-carrying draw replacement is run 32's DrawService replacement-pass redesign.
- `ArchmageAscensionDrawReplacementEffect`: Archmage Ascension: an optional draw replacement (search for any card) active at six quest counters, whose payload DrawService runs inline (DrawReplacementKind.ARCHMAGE_ASCENSION).
- `BreathstealersCryptDrawReplacementEffect`: Breathstealer's Crypt: every player's draw is followed by a reveal and a pay-3-life-or-discard for a creature, run inline by DrawService and BreathstealersCryptDrawReplacementHandler.
- `ChainsOfMephistophelesDrawReplacementEffect`: Chains of Mephistopheles, Magus of the Chains: a draw replacement (discard then draw, else mill) on every draw but the first of a draw step, run inline by DrawService with its per-player application count; run 32's DrawService replacement-pass redesign.
- `DrawPerSourceCounterThenDamageEffect`: 'the number of cards they drew this way' is AmountThisWay(DRAW), but DrawCardEffectHandler records no cards when a draw is replaced by more draws (Alhammarret's Archive), so the damage would be 0 where the old cardsDrawnThisTurn count deals it.
- `EachOpponentDrawsCardEffect`: 4 card files moved to DrawCardEffect(1, eachOpponent()); Struggle for Project Purity stays: its "for each card drawn this way" counts a replacement's extra cards here, the survivor records none for a replaced draw (the owner's replaced-draw question, D14 ….
- `EachPlayerReturnsPermanentToHandEffect`: record unused but its handler's beginReplacement/completeChoice serve Words of Wind via DrawService, MultiPermanentChoiceHandlerService.
- `IslandSanctuaryEffect`: Island Sanctuary: a draw-step 'instead you may skip that draw. If you do, [payload]' marker dispatched by DrawReplacementKind in MayMiscHandlerService (the payload: a floated CreaturesCantAttackControllerUnlessPredicateEffect, flying or islandwalk, ….
- `ObstinateFamiliarDrawReplacementEffect`: Obstinate Familiar: 'if you would draw a card, you may skip that draw instead', a MaySkipDrawReplacementEffect marker dispatched by DrawReplacementKind in MayMiscHandlerService.
- `ParallelThoughtsDrawReplacementEffect`: Parallel Thoughts: 'you may instead put the top card of the face-down pile exiled with it into your hand' as a MaySkipDrawReplacementEffect marker dispatched by DrawReplacementKind (DrawService.resolveDrawFromExiledPile).
- `RegisterNextDrawCreateBearReplacementEffect`: next-draw replacement payload read by DrawService (Words of Wilding); same DrawService redesign as Mangara's Tome's row.
- `RegisterNextDrawDamageReplacementEffect`: next-draw replacement payload read by DrawService (Words of War), with its target and last-known source; same DrawService redesign.
- `RegisterNextDrawDiscardOpponentsReplacementEffect`: next-draw replacement payload read by DrawService (Words of Waste), prompts inside the draw; same DrawService redesign.
- `RegisterNextDrawExileTopCardMayPlayThisTurnEffect`: next-draw replacement payload read by DrawService (Urabrask, Heretic Praetor); same DrawService redesign.
- `RegisterNextDrawFromExiledPileReplacementEffect`: a next-draw replacement payload DrawService applies inline (Mangara's Tome); a generic 'instead of your next draw, [effects]' needs effects and their prompts run inside a draw: a DrawService replacement-pass redesign (step model), not this plan.
- `RegisterNextDrawFromOutsideGameReplacementEffect`: next-draw replacement payload read by DrawService (Ring of Ma'ruf), prompts inside the draw; same DrawService redesign.
- `RegisterNextDrawGainLifeReplacementEffect`: next-draw replacement payload read by DrawService (Words of Worship); same DrawService redesign.
- `RegisterNextDrawLookAtTopReplacementEffect`: next-draw replacement payload read by DrawService (Aladdin's Lamp), with its X; same DrawService redesign.
- `RegisterNextDrawReturnPermanentsReplacementEffect`: next-draw replacement payload read by DrawService (Words of Wind), each player's choice inside the draw; same DrawService redesign.
- `RevealDrawnCardAndBoostEquippedCreatureByManaValueEffect`: the reveal is of the card of an unreplaced draw (ruling 2022-06-10), which the DRAW step doesn't record: draw-replacement provenance in DrawService is outside this plan (Pact Weapon).
- `RevealDrawnCardAndRemoveLoyaltyCountersEffect`: the reveal is of the card of an unreplaced draw (ruling 2024-11-08), which the DRAW step doesn't record: draw-replacement provenance in DrawService is outside this plan (Jace, Mirror Mage).
- `SharedFateDrawReplacementEffect`: Shared Fate: a static draw replacement whose payload (exile the top card of an opponent's library face down, tracked per exiler) DrawService runs inline.
- `UnpredictableCycloneDrawReplacementEffect`: Unpredictable Cyclone: a draw replacement whose payload (exile until a shared card type, free cast, the rest to the bottom at random) DrawService runs inline: run 32's DrawService replacement-pass redesign, as Abundance and Shared Fate (Seen, not changed: its ….
- `ZursWeirdingDrawReplacementEffect`: Zur's Weirding: a draw replacement with a pay-life offer that DrawService runs inline (ReplaceSingleDrawEffect's ZURS_WEIRDING kind): run 32's DrawService replacement-pass redesign (Seen, not changed: only getOpponentId may pay, where the text says any other ….

#### Planar deck cards (13)

Unblocked by a planar-deck card subject with its reveal, bottom-order and planeswalk steps.

- `AllowPlayCardsExiledWithPlanarSourceEffect`: The Matrix of Time: plays cards linked to a face-up plane, which run 5's blocked ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect makes (the exile links only to a source permanent); waits with it for a link to the planar object.
- `AllPermanentsUntapDuringEachPlayersUntapStepEffect`: Horizon Boughs: the survivor's collector reads permanents and emblems only, its subject check allows you-sets, attached and source, and its set branch skips a source controlled by the active player.
- `DealDamageToAnyTargetThenDrawIfExcessDamageEffect`: excess damage to any creature dealt damage this way (redirects included); the damage primitives' excess read is the target's, planeswalkers and battles too; Vikya.
- `DealDamageToTargetAndTheirCreaturesEffect`: 7 of 8 cards split (DealDamageToTargetPlayerOrPlaneswalkerEffect + toCreaturesOfTargetedPlayer, one event); kept for Heart of Bogardan, an else-effect DestructionSupport.resolveForcedCostElseEffects runs synchronously with no target.
- `DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect`: two target groups (player or planeswalker, up to N creatures that player controls) and a can't-block rider; needs a multi-target-group damage primitive like Shellshock.
- `ExileRandomInstantOrSorceryFromTargetHandMayCastFreeEffect`: the random hand reveal exists; left: no exile step over a revealed hand card and the next-end-step return "if you haven't cast it" (Planeswalker's Mischief).
- `ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect`: linked to the planar object: the library exile links only to a source permanent (The Matrix of Time).
- `RevealPlanarCardsUntilFivePlanesEffect`: verdict corrected from unique: waits with the five other planar-deck reveals for a planar-deck card subject (its zone, a reveal step and a bottom-in-any-order step over it); also a choice among them on top.
- `RevealPlanarCardsUntilPlaneAndPlaneswalkEffect`: verdict corrected from unique: waits with the five other planar-deck reveals for a planar-deck card subject (its zone, a reveal step and a bottom-in-any-order step over it); also a planeswalk that does not depart.
- `RevealPlanarCardsUntilPlaneAndTriggerChaosEffect`: verdict corrected from unique: waits with the five other planar-deck reveals for a planar-deck card subject (its zone, a reveal step and a bottom-in-any-order step over it); also a chaos-trigger step.
- `RevealPlanarCardsUntilTwoPlanesAndPlaneswalkEffect`: verdict corrected from unique: waits with the five other planar-deck reveals for a planar-deck card subject (its zone, a reveal step and a bottom-in-any-order step over it); also a double planeswalk.
- `RevealTopPlanarCardMayPutOnBottomEffect`: verdict corrected from unique: waits with the five other planar-deck reveals for a planar-deck card subject (its zone, a reveal step and a bottom step over it).
- `RevealTopPlanarCardsAndTriggerChaosEffect`: verdict corrected from unique: waits with the five other planar-deck reveals for a planar-deck card subject (its zone, a reveal step and a bottom-in-any-order step over it); also a chaos-trigger step.

#### Engine-built records (no card writes them) (10)

- `CastInvokeCalamityChosenSpellEffect`: engine-built half of Invoke Calamity's record (its per-selection router into the hand and graveyard free casts); goes with CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffect.
- `DrawCardForPlayerEffect`: engine-built with a fixed player id (Seize the Spotlight, Cirdan the Shipwright); not a subject.
- `DrawUpToNCardsEffect`: different verb (draw up to N, a choice); DrawUpToRecipient is engine-built only, OPPONENT needs a single-opponent read.
- `LoseLifeToOpponentsWhoCastNamedSpellThisTurnEffect`: engine-built filtered each-opponent; needs a filtered each subject.
- `NihiloorTapAndStealEffect`: no card (engine-built by NihiloorEnterEffectHandler): the per-opponent tap choice and reflexive steal of Nihiloor; goes with NihiloorEnterEffect.
- `SourceFightsTargetCreatureControlledByPlayerEffect`: engine-built for Strax's reflexive ability; folding it needs a reflexive 'that player' read from playersThisWay(CHOOSE) (a QueueReflexiveAbilityEffect option or a target predicate over carried step results) that only Strax would set.
- `SourcePermanentControllerLosesLifeEffect`: engine-built with a fixed player id; not a subject.
- `TransmuteArtifactCardDispositionEffect`: Transmute Artifact's engine-built mover; waits with TransmuteArtifactSearchEffect.
- `TransmuteArtifactSelectedCardEffect`: Transmute Artifact's engine-built comparison; waits with TransmuteArtifactSearchEffect.
- `UnattachEquipmentIfAttachedToControlledCreatureEffect`: left: engine-built by ChooseEquipmentToUnattachEffectHandler and RegisterControlLossUnattachTriggerEffectHandler about a remembered Equipment; the fold needs a Condition 'that Equipment is attached to a creature you control', which no other record would use.

#### Out-of-battlefield characteristics (9)

Unblocked by a redesign of the out-of-battlefield characteristic queries.

- `AllCardsAreColorlessEffect`: Mycosynth Lattice: cards outside the battlefield and spells are colorless too (the outside-battlefield colour query): run 18's all-zones characteristic grant, waiting for the out-of-battlefield query redesign outside this plan.
- `AllPermanentsGainChosenColorEffect`: Painter's Servant: cards outside the battlefield and spells gain the colour too: run 18's all-zones characteristic grant, waiting for the out-of-battlefield query redesign outside this plan.
- `BecomeCreatureOutsideBattlefieldEffect`: Grist, the Hunger Tide: a characteristic outside the battlefield read by GameQueryService and DeckValidationService (SelfOutsideBattlefieldCreatureEffect, one implementer).
- `ControlledNonlandPermanentsAreColorEffect`: Celestial Dawn: its colour also applies to spells you control and nonland cards you own outside the battlefield (GameQueryService's outside-battlefield colour query): run 18's all-zones characteristic grant, waiting for the out-of-battlefield query redesign ….
- `GrantCardTypeToOwnCardsEffect`: an all-zones characteristic grant is read by each characteristic's own out-of-battlefield query through its capability (card types, creature types, land types, supertypes): one record needs those queries redesigned, outside this plan.
- `GrantCardTypeToOwnNonlandPermanentsEffect`: an all-zones characteristic grant is read by each characteristic's own out-of-battlefield query through its capability (card types, creature types, land types, supertypes): one record needs those queries redesigned, outside this plan.
- `GrantSubtypeToOwnCreaturesInAllZonesEffect`: an all-zones characteristic grant is read by each characteristic's own out-of-battlefield query through its capability (card types, creature types, land types, supertypes): one record needs those queries redesigned, outside this plan (Roshan).
- `GrantSubtypeToOwnLandsAndLandCardsEffect`: an all-zones characteristic grant is read by each characteristic's own out-of-battlefield query through its capability (card types, creature types, land types, supertypes): one record needs those queries redesigned, outside this plan (Dune Chanter).
- `GrantSupertypeToOwnLandsAndLandCardsEffect`: an all-zones characteristic grant is read by each characteristic's own out-of-battlefield query through its capability (card types, creature types, land types, supertypes): one record needs those queries redesigned, outside this plan (Rootpath Purifier: ….

#### A token controller on CreateTokenEffect (5)

Unblocked by a `PlayerSubject` token controller on `CreateTokenEffect` with the Esix, investigate and attack paths agreeing.

- `ChooseAttackTargetsForCreatedTokensEffect`: built at runtime by its handler's createdTokensAttack, called by CreateTokenEffectHandler, CreateTokenForTriggeringPlayerEffectHandler, CreateTokensAttackingEffectHandler, PayXManaCreateXTokensEffectHandler, PermanentChoiceTriggerHandlerService.
- `CreateTokenForPlayerEffect`: token-controller axis: CreateTokenEffect needs a PlayerSubject token controller first; paths differ (Esix offer via resolveForController vs direct applyCreateToken, investigate event, attack choice.
- `CreateTokenForSourceControllerEffect`: token-controller axis: CreateTokenEffect needs a PlayerSubject token controller first; paths differ (Esix offer via resolveForController vs direct applyCreateToken, investigate event, attack choice.
- `CreateTokenForTargetPlayerEffect`: token-controller axis: CreateTokenEffect needs a PlayerSubject token controller first; paths differ (Esix offer via resolveForController vs direct applyCreateToken, investigate event, attack choice.
- `CreateTokenForTriggeringPlayerEffect`: token-controller axis: CreateTokenEffect needs a PlayerSubject token controller first; paths differ (Esix offer via resolveForController vs direct applyCreateToken, investigate event, attack choice.

#### The owner's gift opponent (2)

Waits on the owner (no gift opponent is recorded; "Waiting on the owner" below).

- `EachOtherPlayerDrawsCardEffect`: the 4 non-gift files moved (Words of Wisdom, Deadpool Trading Card, Master of the Feast to DrawCardEffect(1, eachOpponent()); Baleful Mastery to ChoosePlayerEffect.opponent() + DrawCardEffect(1, playersThisWay(CHOOSE)), its multiplayer ruling).
- `TargetOpponentCreatesTokenEffect`: declares no target and derives the opponent (two players): the target or promised opponent must reach the entry before it can be a subject; also fires the gift-given event.

#### Other (each note names the missing part) (197)

- `ActivePlayerExilesCardFromGraveyardEffect`: "if the player doesn't" also covers an accepted exile from an empty graveyard: needs the player graveyard choice to record an EXILE step (Web of Inertia).
- `ActivePlayerRevealsUntilCreatureToBattlefieldRestToGraveyardEffect`: the handler now enters the creature through the entry batch (as-enters choices, a card that can't enter from the library stays there); still blocked on the record declaring Oath of Druids' player target.
- `AnimateControlledEnchantmentsEffect`: its 'as long as you control five or more enchantments' threshold would be an option only Starfield of Nyx sets; the reuse path, ConditionalEffect(ControlsPermanentCount(5, enchantment), survivor), needs that condition board-stable (ConditionBoardStability is ….
- `AttachOneOfEquipmentToSamuraiEffect`: left (Gilgamesh): 'attach one of them to a Samurai you control' makes two choices in one resolution (a Samurai that could take any one of the picked Equipment, then one of the Equipment that fits it).
- `AttachTargetEquipmentToCreatedPermanentEffect`: left: LivingWeaponEffectHandler builds its source form for every living-weapon card (the triage's 'no maker' was wrong); folding Grip alone leaves the record.
- `AwardManaOfColorsInGraveyardEffect`: Corrupted Grafstone's colours of graveyard cards would be a ManaSpendRestriction value only it sets, and the survivor's land-replacement amount ignores CR 106.5 (no colour, no mana).
- `AwardManaOfSourceCardColorsEffect`: Wrath of Sod's manabond grant: the survivor's SOURCE_PERMANENT_COLORS (Katilda) has no potential-mana model of the source's current colours (the record's estimate facets and availableManaColors), and its land-replacement amount is 1 for a colourless source ….
- `BattleAngelsOfTyrEffect`: Battle Angels of Tyr: the split needs three Conditions on the damaged player, more cards in hand / lands / life than each other player (you included); subjects inside Conditions are outside this plan and each Condition would be its card's alone.
- `BecomeCopyOfChosenCreatureYouControlUntilEndOfTurnEffect`: Deepfathom Echo: ChoosePermanentsEffect keeps a lone chosen permanent as the source's chosen permanent, so a copy of a 'the chosen creature' card (Dauntless Bodyguard) would have a choice it never made; the choose step needs a no-keep form first.
- `BecomeCopyOfTargetCreatureEffect`: the optional copy runs through BecomeCopyOfTargetCreatureChoiceHandler (keeps its trigger, may keep color); the two siblings are mandatory with their own retention: a copy verb needs the "except" profile first (step 10).
- `BecomeEnchantmentEffect`: Opal Acrolith, Hidden Stag (already one record): it undoes BecomeCreatureEffect's card swap (setCard(getOriginalCard())); a layered SetCardTypesEffect({ENCHANTMENT}, source()) folds it only once BecomeCreatureEffect is a timestamped layer 4/7b floating ….
- `BoostAllCreaturesExceptChosenTypeEffect`: ChooseCreatureTypeAtResolutionEffect + BoostAllCreaturesEffect(-3, -3, Not(PermanentHasTypeChosenThisResolutionPredicate)) says it, but BoostAllCreaturesEffect is no BoardWipeEffect, so Crippling Fear would lose the sweepsBoard view SpellEvaluator's phase and ….
- `BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffect`: Haunted One's 'other creatures that share a creature type with it' uses the triggering creature's last-known types once it left (CR 608.2h); Subjects.otherPermanentsSharing reads live permanents only and would name nothing, a narrowing.
- `BoostByAttackCountEffect`: implements StaticCreatureBoostEffect, which five AI readers use; DynamicStaticBoostEffect has no such view, so the fold would change the AI's reading of Moraug and Rampaging Ursaguana.
- `BoostEquippedCreatureOfEnchantedEquipmentEffect`: Rune of Might, Rune of Speed: the Rune gives the Equipment the static ability (Oracle: it has Equipped creature gets ...), which ability loss removes (EnchantedEquipmentGrantSupport).
- `BoostNonHumanCreaturesByCreatureTypeCountEffect`: a creature-type-count amount of the amount source (changeling counts all, capped) would be read only by Diligent Zookeeper.
- `BoostOtherMulticoloredCreaturesByColorCountEffect`: a colour-count amount of the amount source would be read only by Knight of New Alara.
- `BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect`: needs per-boosted-creature amounts (step 10's boost merge, 5i) and a count of creatures controlled by the player that creature attacks, which only Jurin would read.
- `BoostTargetCreatureByTargetPlayerCountEffect`: Dark Salvation: the amount is read against another target group ('each Zombie that player controls'); same missing part as Orim's Thunder's blocked row (DestroyTargetPermanentThenDealManaValueDamageToTargetCreatureEffect), 2 classes.
- `CantBeBlockedByCreaturesWithPowerLessThanIslandCountEffect`: Kraken of the Straits: no predicate says 'power less than a count'; the at-most count predicates are not strict, and a new predicate only Kraken would use moves the special case.
- `CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffect`: Invoke Calamity casts from a hand and a graveyard under one total mana-value cap with an exile-instead replacement; the cast-now queue (ExileFreeCastQueueSupport) casts only from exile, and the hand and graveyard free casts are two other flows ….
- `ChooseCardFromGraveyardAndConjureDuplicateIntoHandEffect`: no conjure-a-duplicate step over a CardSubject (with Holographic Double's; two classes); its choice is secret, where ChooseCardEffect names a graveyard card in the log (Nashi, Illusion Gadgeteer).
- `ChooseCounterTypeOnControlledCreatureThenPutOnOtherCreaturesEffect`: no recorded choice of a kind of counter among a permanent's; two classes with Aven Courier's (Contractual Safeguard).
- `ChooseCounterTypeOnControlledPermanentThenPutOnTargetPermanentEffect`: no recorded choice of a kind of counter among a permanent's; two classes with Contractual Safeguard's (Aven Courier).
- `ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect`: ChooseCardEffect is its first half; no conjure-a-duplicate step over a CardSubject; two classes of the family with Nashi's (Holographic Double).
- `ChooseCreatureOnEnterEffect`: kept with EnchantedCreatureIsCopyOfChosenCreatureEffect (same static Aura path); its choice now drops a creature that can't be chosen (CR 608.2d).
- `ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect`: different verb (keep one of each type, sacrifice the rest); its recipient became PlayerSubject players (each only).
- `ChooseNameAndNumberRevealLibraryDamageEffect`: no "exactly the chosen number" condition between a count and the entry's number and no reveal of a whole library; two classes with Scrying Glass's reveal (Mindblaze).
- `ChooseOneForTargetCreatureEffect`: the suspected defect does not exist (its cards have one target group, so the permanent form's group binding isn't needed, and its no-target return covers the up-to-one case).
- `ChooseOneForTargetPermanentEffect`: see ChooseOneForTargetCreatureEffect (per-mode AI polarity, the no-target prompt).
- `ChooseOpponentOnEnterEffect`: four entry paths read it (auto-choice of a sole opponent, the chooser it records, the put-onto-battlefield support, cleanup hand size); ChoosePlayerOnEnterEffect is read by one.
- `ConjureDarigaazShivanChampionSpellbookEffect`: left. Every part but one exists (ConjureCardsEffect.atRandomFrom(13 printings, intoExile()), PutCounterOnExiledCardsEffect(cardsThisWay(CONJURE), EGG, 3), withAbilities(the exile upkeep ability)).
- `ConjureDuplicateOfSourceCardIntoHandEffect`: the card axis: no source-card CardSubject, and it conjures the catalog printing with removed keywords.
- `ConjureDuplicateOfTargetExiledCardIntoHandEffect`: left. Each face folds onto ConjureCardsEffect.duplicateOf(targetCardExiledWithSource(filter), intoHand()) + the perpetual steps over cardsThisWay(CONJURE), but not at a clean point: (1) the specialize trigger is one made ability whose steps would each need ….
- `CopyTargetSpellOrAbilityEffect`: copies a targeted stack entry of any of those kinds with its own retarget choice (Return the Favor): one copy path with the triggering sibling first.
- `CopyTriggeringSpellOrAbilityEffect`: dispatches to three copy paths by what triggered (spell, activated, triggered ability), from the trigger snapshot; the target sibling has a fourth: one copy path first (step 10).
- `CreatedTokensFightDifferentOpponentCreaturesEffect`: Ezuri's Predation: needs the one fight path with a fighter PermanentSubject (5m's blocker of SourceFightsTargetCreatureEffect and EnchantedCreatureFightsTargetCreatureEffect) plus a pairing the controller chooses between createdThisResolution() and the ….
- `CreateTokenAttachedToDefendingPlayerEffect`: host is a player (defending player), not a PermanentSubject; folds once the attach verb takes a player host.
- `CreateTokenCopyAndLinkToSourceEffect`: face-down copies are decided; the token copy with trackWithSource would express it, but its Esix replacement (a625b69fdc's survivor path) creates copies the source doesn't track, so Dance of Many's 'the token' would miss them (Faerie Artisans already has that ….
- `CreateTokenCopyOfSourceForEachOtherPlayerEffect`: token-controller axis of CreateTokenCopyOfTargetPermanentEffect (its createForTargetController flag is controllerOf(target())); the source copies belong with CreateTokenCopyOfSourceEffect (step 10).
- `CreateTokenCopyOfSourceForTargetPlayerEffect`: token-controller axis of CreateTokenCopyOfTargetPermanentEffect (its createForTargetController flag is controllerOf(target())); the source copies belong with CreateTokenCopyOfSourceEffect (step 10).
- `CreaturesMustAttackTargetPlayerUntilNextTurnEffect`: no card: the floating requirement payload two handlers make and AttackLegalityService reads.
- `DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect`: the can't-gain-life-for-the-rest-of-the-game records (PlayersCantGainLifeRestOfGameEffect, TargetPlayerCantGainLifeRestOfGameEffect) take no PlayerSubject; fold them first, then playersThisWay(DEAL_DAMAGE); Screaming Nemesis.
- `DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectIfCreatureEffect`: the Perpetually* grants take no PermanentSubject; Melt Through.
- `DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectsIfCreatureDamagedEffect`: the Perpetually* grants take no PermanentSubject (would read thisWay(DEAL_DAMAGE, creature)); Switchgrass Grazer.
- `DealDamageToCombatOpponentControllerEffect`: declares a harmful permanent target over a baked non-targeting entry; the survivor's controllerOf(target()) declares a benign one.
- `DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffect`: the creature damage is evaluated per controller; DealDamageToPermanentsTargetControlsEffect evaluates one amount; Incite Rebellion.
- `DealDamageToEnchantedPermanentControllerEffect`: its last-known chain (Aura snapshot, then recorded host controller, then baked targetId) and its enchanted-permanent-as-source swap differ from controllerOf(attached()) + dealtByTriggeringPermanent(); fold with tests for the Aura-gone cases.
- `DealDamageToTargetPlayerControlsCreaturesThenPerpetuallyBoostEffect`: PerpetuallyBoostTargetCreatureEffect takes no PermanentSubject; Chaos Balor.
- `DestroySelfAtEndStepEffect`: see DestroyTargetPermanentAtEndStepEffect; also has the legacy source-to-targetId fallback.
- `DestroySourceThenDealDamageToControllerEffect`: Voodoo Doll; tried SequenceEffect.of(DestroyTargetPermanentEffect(source()), DealDamageToPlayersEffect(CountersOnSource(PIN), you())): after the destroy the amount reads the entry's source snapshot taken when the ability triggered, so pin counters added in ….
- `DestroyTargetArtifactDealManaValueDamageToSourceEffect`: 'that artifact deals damage … to this creature' (Goblin Tinkerer): no damage step takes a departed target as the damage source.
- `DestroyTargetCreatureAndCreateTokenCopiesEffect`: Saw in Half's 'except their power is half that creature's power and their toughness is half its toughness, round up' is a copy exception (copiable values, CR 707.9b) from its last-known P/T.
- `DestroyTargetCreaturesThenPutCountersOnSourceIfOwnDestroyedEffect`: 'if a creature you controlled was destroyed this way' (Demonic Junker) needs a condition on the DESTROY outcome's players; toA(DESTROY, controlled by you) can't read a departed snapshot's controller.
- `DestroyTargetNonblackCreatureAndGainControlOfAttachedEquipmentEffect`: 'all Equipment that was attached to it' after the destroy (Murderous Spoils): attachmentsOf reads live hosts only.
- `DestroyTargetPermanentThenDealManaValueDamageToTargetCreatureEffect`: Orim's Thunder's kicked damage reads 'that permanent's' mana value from the other target group; TargetManaValue reads the damage effect's own target.
- `DestroyUpToOneNonbasicLandPerPlayerThenSearchEffect`: Krenko's Buzzcrusher: 'for each player, destroy up to one' needs ChoosePermanentsEffect.oneForEach with no minimum and a multi-choice prompt that can be declined (oneForEach is exactly one, a lone candidate taken unasked).
- `DestroyUpToOneTargetPermanentEffect`: up to one target: OptionalTargetEffect is a marker interface, so the survivor can't absorb it per instance.
- `DistantMemoriesEffect`: 'any opponent may have you put that card into your hand' asks each opponent in turn order until one does (ruling); no MayEffect chooser or loop asks several players in turn; today only the first other seat is asked (defect queued).
- `DoubleCombatDamageToCreaturesThisTurnEffect`: left: 'if a creature would deal combat damage to a creature' is every controller's creatures as sources and creature recipients only, a source set and a scope no source doubler has (each set by Blind Fury alone).
- `DoubleDamageFromTargetPermanentThisTurnEffect`: left: Overblaze's doubling is a resolved spell's effect on the permanent, not an ability it gains (a layer-6 grant of DoubleDamageFromCreaturesEffect(source()) would end under Humility).
- `DrainLifeFromDyingCreatureOwnerEffect`: is ownerOf(triggering()) plus a fixed gain (gains the amount even when no life is lost) and a graveyard-owner lookup; not LoseLifeEffect's drain.
- `DrawCardIfEventValueAtLeastEffect`: ConditionalEffect(EventValueAtLeast(n), DrawCardEffect()) says it, but hides the CardDrawingEffect that BoardEvaluator.activatedAbilityThreat and HardAiDecisionEngine.isGoodTimingForAbility read on Black Panther's activated ability.
- `EachDestroyedPermanentControllerGainsLifeEqualToManaValueEffect`: runs 17-18: Seeds of Innocence. AmountThisWay(DESTROY, MANA_VALUE).includingUnremoved().controlledByYou() would name the set, but the record gains life once per artifact (one life-gain event each.
- `EachPlayerChoosesCardFromGraveyardToBattlefieldEffect`: the each-graveyard batch (PendingGraveyardReturnBatch) has no exile-if-it-leaves rider for the target-player sibling; differs in riders, not only in whose graveyard.
- `EachPlayerChoosesCreatureThenSacrificesRestEffect`: handler's completeChoice called by MultiPermanentChoiceHandlerService; the whole flow (choice context, completion) looks dead.
- `EachPlayerExilesTopCardMayPlayUntilNextEndStepEffect`: the permission is each owner's and ends at the controller's next end step: AllowPlayCardsFromExileEffect ends a duration on its holder's clock; two cards (Rocco, Memory Vessel).
- `EachPlayerExilesTopCardsMayPlayUntilNextTurnEffect`: as Rocco's: each owner plays until the controller's next turn; two cards (Memory Vessel).
- `EachPlayerTakesDamageUnlessPaysEffect`: left with Stench of Evil's record, its only builder.
- `EachTargetPlayerGainsLifeEffect`: amount evaluated once in the caster's context (Reward the Faithful reads your permanents) where a target-player GainLifeEffect re-bases it per target; and the AI scores it as own gain.
- `EcologicalAppreciationEffect`: Ecological Appreciation: needs a library-and-graveyard search step recording SEARCH (different names, mana value X or less; Doomsday holds the only other such pool).
- `EnchantedCreatureDealsDamageToTargetCreatureEffect`: fixed amount from the Aura's host (own departed-snapshot chain); the power survivor has no amount component.
- `EnchantedCreatureDealsPowerDamageToAnyTargetEffect`: any-target recipient (see TargetCreatureDealsPowerDamageToAnyTargetEffect) with its own triggering-or-attached last-known chain.
- `EnchantedCreatureIsCopyOfChosenCreatureEffect`: Metamorphic Alteration's 'enchanted creature is a copy of the chosen creature' is a static ability kept by AuraCopyService while attached, not a resolution.
- `EpicExperimentEffect`: 'put all cards exiled this way that weren't cast into your graveyard' is a remainder option only Epic Experiment would set on ExileTopCardsAndMayCastSpellsEffect, which records no EXILE step a later put step could read.
- `ErestorOfTheCouncilEffect`: reads the triggering vote (StackEntry.getTriggeringVotingResult, the 'players finish voting' event), not a VOTE step of its own entry.
- `ExileAllButBottomCardsOfEachLibraryFaceDownEffect`: the library exile has no face-down option and evaluates one count for every player, not each library size less six (Doomsday Excruciator).
- `ExileAnyNumberOfOwnGraveyardCardsThenEffect`: no any-number choice on the own-graveyard exile step (Specter of Mortality; as Winter).
- `ExileDyingCreatureAndCreateSpiritTokenCopyEffect`: the token's link to the exiled card and a copy of the creature as it last existed (Hofri Ghostforge; as Timothar).
- `ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect`: 'exile it if it's an instant or sorcery card' needs a filter on the triggering-card exile (Cryptic Pursuit).
- `ExileMatchingCardsFromTargetPlayerHandEffect`: a filter on ExileTargetPlayerHandEffect would be set only by Thought Distortion (guard rail).
- `ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffect`: a reflexive target among the cards exiled this way: no target filter over cardsThisWay (Espers to Magicite).
- `ExileRandomInstantOrSorceryFromGraveyardAndMayCastFreeEffect`: the random step exists; the cast-now step (MayCastDiscardedCardWithoutPayingManaCostEffect) has no exile-instead-of-graveyard rider, which only Storm of Memories would set.
- `ExileSacrificedPermanentWithSourceEffect`: ExileTriggeringCardFromGraveyardEffect would express it with a source link and the sacrificed snapshot's card; the token rider is a ConditionalEffect (Prowling Geistcatcher, Bag of Devouring).
- `ExileSourceAndCreaturesAndPlaneswalkersWithManaValueAtMostSourceCountersEffect`: graveyard cards exiled with the permanents; no graveyard-set exile primitive; Sphere of Annihilation.
- `ExileSourceAndTopCardChooseOneMayPlayUntilNextTurnEffect`: a choice group on the permission over two exile steps' cards (Flameskull; as Lara Croft).
- `ExileSourceCardFromGraveyardAndCollectEvidenceEffect`: collect evidence records no step for "if you do" and its feasibility is checked before the exile (Lamplight Phoenix).
- `ExileTargetCreatureAndReturnAtNextEndStepEffect`: harmful creature spec and RemovalEffect where FlickerEffect's target is benign permanent: decide which is right before folding into FlickerEffect.exileTargetReturnAtEndStep.
- `ExileTargetCreatureCardFromGraveyardDealPowerDamageToSourceEffect`: the exiled card is the damage source: no damage step whose source is a CardSubject (Selfless Exorcist).
- `ExileTargetCreatureCardFromGraveyardPutPowerCountersEffect`: the any-graveyard target path reads every target of the entry: no target-group component for the fused spell (Flesh // Blood).
- `ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect`: Luminate Primordial only: each target's controller gains that creature's power (a per-object amount); run 9 split Swords to Plowshares, Avenger en-Dal, Captain Marvel as ExileTargetPermanentEffect + GainLifeEffect(TargetPower | TargetToughness, ….
- `ExileTargetGraveyardCardsAndSeparateIntoPilesEffect`: no separate-into-piles step over cardsThisWay(EXILE).
- `ExileTargetNonlandPermanentIfManaValueAtMostConvergeEffect`: ConditionalEffect(TargetPermanentManaValueAtMostColorsSpent, exile) expresses it, but the AI's removal scoring does not look inside a ConditionalEffect (Prismatic Ending).
- `ExileTargetPermanentAndCreateTokenCopyEffect`: token copy of a departed target needs CreateTokenCopyOfTargetPermanentEffect to read the exiled card's last-known copiable values; Dedicated Dollmaker, Kaya, Nemesis Trap.
- `ExileTargetPermanentAndGrantControllerCastPermissionUntilNextTurnEffect`: needs a cast-permission grant on the cards exiled this way; Hurl Through Hell.
- `ExileTargetPermanentAndTrackWithSourceAndAllowPlayEffect`: needs the source link on ExileTargetPermanentEffect the step-8 closing summary names, then AllowPlayCardsFromExileEffect.whileExiled(...).playableByOwner() (Prowl, Stoic Strategist).
- `ExileTargetPermanentThenEffect`: 20 of 25 cards split (runs 2-3); 5 left: seek (Fragment Reality, Obscura Polymorphist), incubate (Excise the Imperfect), counters LKI (Flay Essence), dealt-damage LKI (Sold Out).
- `ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect`: SearchLibraryEffect has no "that player may search for that many" that may search at zero and skips the shuffle when declined; with Winds of Abandon two cards (Settle the Wreckage).
- `ExileTargetPlayerGraveyardCardsEffect`: X cards targeted in one target player's graveyard plus per-card life riders; no exile step targets 'cards from target player's graveyard' (Suffer the Past).
- `ExileTopCardOfTargetOpponentLibraryWithArtifactThresholdEffect`: the cast-now offer is capped per face of an Adventurer card beside a timed permission: no such option on the cast-now step (Rashmi and Ragavan).
- `ExileTopCardsAndSuspendNonlandsWithManaValueEffect`: PutTimeCountersAndGainSuspendEffect takes a fixed count; the counters equal each card's mana value (The Parting of the Ways).
- `ExileTopCardsMayPlayThisTurnAndPutTimeCountersOnSuspendedCardsEffect`: time counters only on exiled cards that already have suspend: no such option on the suspend step (Ecstatic Beauty).
- `ExileTopCardsOfTargetOpponentCreateTokenPerChosenColorEffect`: no choose-a-color step before the exile and no card predicate reading that choice (Oona).
- `ExileTopCardsUntilPermanentCountToBattlefieldEffect`: exile until N permanent cards, then put them onto the battlefield: no counting until-step and no battlefield step over a CardSubject (Auspicious Starrix).
- `ExileTopCardsWithLukkaPermissionEffect`: with LukkaExileCastPermissionEffect (Lukka, Coppercoat Outcast): mayOnlyCast(creature) would stop casting an exiled adventurer card as its Adventure (ruling 2020-04-17).
- `ExileTopUntilNonlandMayCastWithoutPayingManaCostEffect`: the rider of DestroyTargetPermanentThenEffect run as the destroyed permanent's controller: the cast-now step offers the card to the resolving controller only (Transforming Flourish).
- `ExileTopUntilNonlandOfDamagedPlayerMayCastForLifeThisTurnEffect`: the exile-until step exists; the cast-now-for-life option exists only on the library primitive (Bismuth Mindrender).
- `ExileUpToCombatDamageCardsFromGraveyardEffect`: ExileGraveyardCardsEffect's target count is fixed; the trigger's damage sets this one (Froghemoth).
- `FatesealEffect`: ScryEffect lets the controller arrange only a targeted library (decisionMaker is read for targetPlayer() only, and an untargeted library is arranged by its owner), and its untargeted no-trigger log is clash's.
- `GainAbilitiesOfLastChosenExiledCardEffect`: needs a 'the last chosen card, still exiled with this' CardSubject (Permanent.lastChosenExiledCard) that only Koh would set, and its gained exile and return links rewritten together (CR 607.5); with Idris's triggered-abilities option once a second user appears.
- `GainControlOfAllPermanentsMatchingEffect`: its follow-ups read the permanents gained; no GAIN_CONTROL step kind.
- `GainControlOfAllPermanentsTargetPlayerControlsEffect`: its follow-ups read the permanents gained; no GAIN_CONTROL step kind.
- `GainControlOfAuraAttachedPermanentEffect`: Eriette, the Beguiler: GainControlOfTargetEffect would need a duration that tracks the triggering Aura's attachment, not the source's; its WHILE_ATTACHED should be the resolution form (Seen, not changed).
- `GainControlOfCreaturesOfChosenTypeIfMoreThanEachOtherPlayerEffect`: Peer Pressure: ControllerControlsMorePermanentsThanEachOtherPlayer has no permanent filter and APlayerControlsMoreCreaturesThanEachOtherPlayer asks about any player; a filter only Peer Pressure would set.
- `GainControlOfEnchantedPermanentEffect`: the record is the payload of the floating control effect (LayerClassifier, FloatingContinuousEffect, CreatureControlService read it): a fold changes the layer readers.
- `GainControlOfEnchantedTargetEffect`: the record is the payload of the floating control effect (LayerClassifier, FloatingContinuousEffect, CreatureControlService read it): a fold changes the layer readers.
- `GoadCreaturesUntilNextTurnEffect`: a floating rule over a predicate evaluated continuously, also requirement-only (Kardur); not the locked goad designation of the target head: step 10.
- `GrantAllCreatureTypesToOwnCreaturesEffect`: the static and until-end-of-turn changeling record; the Oko sibling never ends and grants types only.
- `GrantOffspringToWhiteCreatureCardInHandEffect`: no perpetual offspring grant over cardsThisWay(CHOOSE): recount with the perpetual verbs in the Perpetually run (Cottontail Caretaker).
- `GrantProtectionChoiceToControllerAndPermanentsUntilEndOfTurnEffect`: the color-choice protection step grants to permanents only: a player recipient through the choice's completion; two classes (Seht's Tiger, Faith's Shield).
- `GrantProtectionChoiceToControllerUntilEndOfTurnEffect`: the color-choice protection step grants to permanents only: a player recipient through the choice's completion; two classes (Seht's Tiger, Faith's Shield).
- `GrantSubtypeToTargetCreatureEffect`: not folded: needs an overriding component on GrantSubtypeUntilEndOfTurnEffect, the PERMANENT path's layer equivalence checked, Goatnapper's perpetual half split off, and a per-mode AI polarity rule (HARMFUL vs NEUTRAL today).
- `GraveEndeavorEffect`: left: 'return ... with a number of +1/+1 counters equal to that result' enters with the counters through ReturnCardFromGraveyardEffect's int plusOneCounterCount (carried by its choice prompt); a DynamicAmount count there would be set by it alone.
- `GuidedPassageEffect`: ChooseCardEffect.oneOfEach asks category by category, so a land creature taken as the creature can leave the land choice empty, where today's one prompt validates the whole set (GuidedPassageTest's land-creature cases).
- `HeistTargetLibraryEffect`: one keyword action with two prompts (HeistCardChoice here, LibrarySearch HEIST there): unify the prompt and completion first.
- `HeistTargetOpponentLibraryEffect`: one keyword action with two prompts (HeistCardChoice here, LibrarySearch HEIST there): unify the prompt and completion first.
- `HuntedByTheFamilyEffect`: Hunted by The Family: 'for each of them, that creature's controller faces a villainous choice' about that creature: TargetPlayerChoosesOneEffect.villainousChoiceFacedBy faces players, and no per-target loop binds the options to one target (kin of Kaboom!'s ….
- `LibraryOwner`: enum kept (CONTROLLER, ENCHANTED_PERMANENT_CONTROLLER) only for the TopCardOfLibraryType / TopCardOfLibraryMatchesPredicate conditions: subjects in conditions are a follow-up.
- `LookAtTopCardsHandBottomExileEffect`: as LookAtTopCardsHandTopBottomEffect: the prompt fold needs client work (game.component's handBottomExile flow).
- `LookAtTopCardsMayRevealMatchingToHandThenMayPutOntoBattlefieldEffect`: Break Out: the survivor's selected-card follow-up runs after the card is in the hand, and its battlefield-else-hand path offers only cards that may go onto the battlefield.
- `LukkaExileCastPermissionEffect`: ExileTopCardMayPlayThisTurnEffect.mayOnlyCast(creature) gives the permission to creature cards but also limits the cast to creature spells, while Lukka's ruling lets an adventurer card be cast as its Adventure.
- `ManaEchoesEffect`: AwardManaEffect(PermanentCount(creature + PermanentSharesCreatureTypeWithTriggeringPermanentPredicate)) can't say it: AmountEvaluationService.countPermanents' FilterContext carries no triggering permanent, and the predicate's last-known read is only the leave ….
- `ManifestDreadXTimesThenPutCountersEffect`: Valgavoth's Onslaught: RepeatEffect(X, ManifestDreadEffect.forController()) + counters over thisWay(MANIFEST) needs the repetitions to add to one MANIFEST step: ManifestDreadEffect's completion (LibraryChoiceHandlerService.resolveManifestChoice) records ….
- `MugEffect`: Locke, Treasure Hunter: the steps exist (MillEffect(1, eachPlayer()) recording MILL; ConditionalEffect(HappenedThisWay.toACard(MILL, land), CreateTokenEffect.ofTreasureToken(1))).
- `MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffect`: left: 'one of your opponents' is the opponents of the ability's controller (the float's controller), not the creature's controller's; DoubleDamageFromCreaturesEffect would need a multiplier and a caster's-opponents recipient scope, each set by it alone.
- `MustBlockTargetCreatureEffect`: two positional targets (blocker and blocked); MustBlockSourceEffect is also its floating payload: merge in step 10.
- `OpponentChoosesCreatureTheyControlTokenCopyEffect`: handler's completeChoice called by PermanentChoiceBattlefieldHandlerService; the whole flow (choice context, completion) looks dead.
- `OpponentMayReturnExiledCardOrDrawEffect`: Distant Memories' payload; waits with DistantMemoriesEffect for an any-opponent-in-turn may.
- `OwnLandsGainAllBasicLandTypesUntilEndOfTurnEffect`: Energybending's skip of a type the land has of its own guards against the survivor's in-addition path granting an explicit mana ability beside the intrinsic one (CR 305.6).
- `PackHuntEffect`: Master of the Wild Hunt: 'tap all untapped Wolves you control; each Wolf tapped this way deals damage equal to its power to target creature.
- `PerpetuallyEnterTappedChosenCardEffect`: Boareskyr Tollkeeper's record is already a step after the CHOOSE (reads the chosen card); 'perpetually gains This permanent enters tapped' as an ability needs the entry path to read a perpetual static ability.
- `PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect`: Seething Skitter-Priest's permanent half: the permanent-set grant survivor (PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect over a set) grants once (Antique Collector), while each Skitter-Priest grant is its own instance (CR 113.2c).
- `PhyrexianPortalEffect`: RevealTopCardsAndSeparateEffect reveals the cards and keeps at most one pile face down; Phyrexian Portal's opponent only looks, and both piles are face down: an option only it would set.
- `PlayerCantCastSpellsAndAttackWithCreaturesEffect`: the floated payload of TargetOpponentChoosesPlayerForRestrictionEffect (Choose Your Champion), read by CastingPermissionService and AttackLegalityService; it goes with that record.
- `PreventAllDamageToAttachedCreatureEffect`: prevents damage to a player, its sibling to a permanent: different recipient types and pipelines.
- `PreventAllDamageToControllerEffect`: prevents damage to a player, its sibling to a permanent: different recipient types and pipelines.
- `PreventFixedDamagePerSourceToControllerEffect`: recipient is a player here and a permanent in its sibling, each read by its own damage pipeline.
- `PreventFixedDamagePerSourceToSelfEffect`: recipient is a player here and a permanent in its sibling, each read by its own damage pipeline.
- `ProtectionFromNonSubtypeCreaturesEffect`: the survivor judges battlefield permanents only, while 'protection from non-Spirit creatures' (Harbinger of Spring, Spare from Evil) also covers creature spells and cards on the stack (the record's card path, changeling read).
- `PutCageCounterOnExiledCardWithSourceEffect`: engine payload of the cage-counter exile: that choice records no EXILE for PutCounterOnExiledCardsEffect.
- `PutChosenCardFromHandIntoGraveyardEffect`: engine payload of the fused seek-three handler (its family's split); the graveyard step moves no hand card; one maker.
- `PutDiscardedCardOnTopOfLibraryThenBattlefieldEffect`: engine payload of a fused exile-discard-return handler; its discard is no step of the entry.
- `PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect`: also Binding Negotiation's decline payload, which needs a face-up qualifier on exiledCards(); two makers.
- `PutTopCardOfTargetLibraryOnBottomEffect`: engine may payload of the look-at-top handler, which records no card for a step to name.
- `RandomPlayerThenFightsTargetCreatureEffect`: Strax, Sontaran Nurse: the split needs the reflexive ability's target filter to read the carried playersThisWay(CHOOSE); that option has only this user (with SourceFightsTargetCreatureControlledByPlayerEffect).
- `ReboundCastFromExileEffect`: built at runtime by its handler's reboundAbility, called by StackResolutionService, ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffectHandler.
- `RedirectChosenColorSpellDamageToControllerEffect`: Harsh Judgment: a static redirection keyed by a source property (an instant or sorcery spell of the chosen colour), not by a source; SourceKeyedRedirectShield keys an object.
- `RedirectNextInstantOrSorceryDamageToControllerEffect`: Aegis of Honor: a floated one-shot redirection keyed by a source property (any instant or sorcery spell dealing damage as itself), not by a source; see Harsh Judgment's row: a source-filter key on SourceKeyedRedirectShield waits for a third card.
- `ResolveRiderForPlayerEffect`: Pain's Reward no longer uses it (split onto the BID step); it stays as DestroyTargetPermanentThenEffectHandler's rider step until that record's blocked cards (step 8) move.
- `ReturnDyingCreatureToBattlefieldEffect`: the same card by two paths: attach and steal tracking here, exile origin and a control effect there: one path first.
- `ReturnedPermanentReturnsTargetPermanentCardFromGraveyardEffect`: the made ability's source is now the spell (no source permanent; CR 603.7d), not the returned permanent. Still blocked for fold run 6c's reasons (Torpor Orb judged on the card, the mana-value bound). Jailbreak.
- `ReturnPermanentsOnCombatDamageToPlayerEffect`: Mistblade Shinobi is a targeted bounce now; left for Cephalid Constable: up to the damage many targets, declared through its own branch of the trigger queue (no event-value target count on ReturnToHandEffect).
- `ReturnSourceTransformedFromGraveyardAttachedToTargetPermanentEffect`: host is a player here and a permanent in its sibling, with their own target specs and rechecks.
- `ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect`: host is a player here and a permanent in its sibling, with their own target specs and rechecks.
- `ReturnTargetCreatureAndSameNameCardsFromGraveyardToBattlefieldEffect`: no CardSubject for the cards with the target card's name; its to-hand sibling moves through another path (Rat King, Verminister).
- `ReturnTargetCreatureAndSameNameCardsFromGraveyardToHandEffect`: no CardSubject for the cards with the target card's name; its battlefield sibling moves through another path (Echoing Return).
- `ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect`: the targeted reanimation head caps total mana value, not total power, which only this record would set (Nethroi, Reunion of the House).
- `ReturnTargetCreaturesUnlessControllersDrawEffect`: left: objects are targets, one draw per payer who chose it, all draws before the returns (its ruling; SourceControllerDrawsCost paid together draws once, Development's), and whether an opponent may choose 'you draw' when the caster can't draw isn't settled ….
- `ReturnTargetCreatureToHandAndCreateTokenCopyEffect`: the bounce + CreateTokenCopyOfTargetPermanentEffect would split, but the copy step copies the remembered card, not a face-down permanent's copiable values (CR 707.2) as this handler does; Supplant Form.
- `ReturnTargetPermanentsThenEffect`: 6 of 7 cards split (ReturnToHandEffect.target() + returned() gate / AmountThisWay(RETURN) / TargetManaValue / ungated 'Then').
- `RevealCardsChooseOneToCastEffect`: the target chooses which cards are looked at, then a free cast from that hand, which no step does (Extract Brain).
- `RevealCardsChooseOneToExileEffect`: the exile-from-hand continuation carries the permission for as long as you control the source and the any-mana rider (Taster of Wares).
- `RevealMatchingCardsFromTargetHandAndExileEffect`: the hand reveal and choice exist; left: exiling the chosen hand card with the draft by that player as its payload (Break Expectations).
- `RevealTopCardAndCounterTriggeringSpellIfMatchesEffect`: the REVEAL step exists now; left: CounterSpellEffect cannot name the triggering spell, and the match reads that spell as it last was (Counterbalance, Swindler's Scheme).
- `RevealTopCardOfOpponentLibraryAndBecomeCopyEffect`: the copy effects take no CardSubject (with the Nascent Metamorph record; two classes) (Shapeshifter's Marrow).
- `RevealTopCardsForCardTypesCastThisTurnEffect`: one choice for each card type found as it resolves: ChooseCardEffect's qualities are fixed on the card (with RevealTopCardsForVividEffect; two classes) (Hurkyl).
- `RevealTopCardsForVividEffect`: one choice for each color found as it resolves: ChooseCardEffect's qualities are fixed on the card (with Hurkyl's record; two classes) (Sanar).
- `RevealUntilNonlandBottomThenDealManaValueDamageEffect`: the reveal-until step and the library step exist (Erratic Explosion and Calibrated Blast would split); left: Kaboom!'s repeat of the whole process for each target in turn order, which keeps the record.
- `RiskyMoveEffect`: Risky Move: 'choose a creature you control and an opponent' is two CHOOSE steps (ChoosePermanentsEffect, ChoosePlayerEffect).
- `SacrificeEnchantedPermanentAndReattachSourceAuraEffect`: no 'attach the source Aura to a permanent that player controls' primitive after the sacrifice.
- `SacrificeOtherCreatureOrDamageEffect`: different verb (sacrifice or be dealt damage, asks); its recipient became a PlayerSubject player (you() or triggeringPlayer()).
- `SacrificePermanentAndReturnTargetCardsFromGraveyardEffect`: its cast-time graveyard targets are special-cased by class in SpellCastingService, TargetLegalityService and GameActionAvailabilityService; Victimize.
- `SacrificePermanentThenEffect`: run 7 split 111 of its 121 cards (SacrificePermanentsEffect + ConditionalEffect(sacrificed(), X | QueueReflexiveAbilityEffect(X))); kept for engine-internal uses (Forage, MayPayTapAndSacrificePermanent.
- `SacrificeSelfUnlessEscapedEffect`: conditional (not escaped) wrapper around SacrificeSelfEffect: a ConditionalEffect once an escaped condition exists (step 9).
- `SeekCardSharingCardTypeWithDiscardedCardsEffect`: Diviner of Fates: triggeringCards() names a discarded card only in its graveyard stay, so one exiled instead (Rest in Peace) or moved since gives no type, while 'shares a card type with one of the discarded cards' reads last-known types (CR 608.2h).
- `SourcePermanentDealsPowerDamageToUpToOneTargetCreatureEffect`: carries the OptionalTargetEffect marker, which the survivor can't implement per shape.
- `TargetLandDealsDamageToTriggeringCreatureEffect`: fixed amount from a target land to the triggering creature; the power survivor has no amount component.
- `TargetOpponentMayDrawCardEffect`: Phelddagrif and Tataru Taru moved to MayEffect(DrawCardEffect(1, targetPlayerDeclaredElsewhere()), TARGET_PLAYER) and the chosen-target form went.
- `TargetPlayerChoosesCardFromGraveyardToBattlefieldEffect`: enters tapped and exile-if-it-leaves through its own immediate choice; the each-graveyard batch of the sibling has no such rider.
- `TargetPlayerChoosesCardsFromHandThenMayCastOneEffect`: handler's handleCardSelection called by TargetPlayerChoosesCardsFromHandChoiceInteractionHandler; the whole flow (pending interaction, AI strategy, projection) looks dead.
- `TargetPlayerDiscardsByConvergeEffect`: EffectResolution.hasConvergeEffect uses the class as the marker that snapshots converge into xValue; fold needs a converge DynamicAmount.
- `TargetPlayerExilesCardFromGraveyardEffect`: lifeGain and track riders of the graveyard-choice completion; the active-player sibling's decline branch needs an EXILE step recorded by this choice first.
- `TargetPlayerLosesGameEffect`: targetPlayer() with combat-damage-baked targetId plus an engine-bound playerId; folds into ControllerLosesGameEffect with a per-card triggeringPlayer mapping.
- `TargetPlayerReturnsCardFromGraveyardToHandEffect`: the target player chooses through its own path; the Oath of Ghouls sibling uses GraveyardReturnSupport: one path first.
- `TurnaboutEffect`: Turnabout: both choices are made on resolution (its ruling), so not a cast-time ChooseOneEffect; ChooseOneAtResolutionEffect declares a permanent target and resolves with an illegal target, where Turnabout's lone player target must be declared as a player and ….
- `TurnSourceFaceDownEffect`: two turn-face-down paths (the source one ends a copy, the target one skips double-faced cards): unify first, as face up.
- `TurnTargetCreatureFaceDownEffect`: two turn-face-down paths (the source one ends a copy, the target one skips double-faced cards): unify first, as face up.

---

## 17. Waiting on the owner (effect cleanup)

**Status:** open · **Found:** 2026-11-26, closing the effect cleanup (step 11)

The effect cleanup's plan and queues were deleted when it closed (2026-11-26). Their last versions:
`git show 3b0d60d42d4:effect-cleanup-refactor.md` (plan, Standing knowledge, the owner's decisions, step 10's Findings) and
`git show 3b0d60d42d4:effect-cleanup-queues.md` (fold queue, defect queue). What the cleanup built for card and engine authors is in
`agent-docs/architecture/effect-building-blocks.md`; `scripts/effect-audit-ledger.csv` keeps every class's verdict and
`python3 -I scripts/effect-inventory.py` the inventory.

58 items: every open question of the owner's and every orchestrator decision logged for the owner to review (the date is
the run's). An answer that changes play becomes a defect run (entry 18); one that confirms a decision just removes its line.

The full text of each item is in the cleanup plan's last version, `git show 3b0d60d42d4:effect-cleanup-refactor.md`: the open
questions under "The owner's open questions", the decisions under "The owner's decisions" (grep the run name), the T1 question in
"Step 10, run T1"; the defect lines in `git show 3b0d60d42d4:effect-cleanup-queues.md`.

#### Open questions

- 2026-11-25, the gift opponent (deferred by the owner; CR 702.174a, `missing-features-to-do.md` entry 80): no gift opponent is recorded, so every gift handler gives the gift to `getOpponentId`, every other player or the spell's target (right with two players only); Cruelclaw's Heist, Consumed by Greed and Perch Protection never raise the gift-given event (CR 702.174c). Two ledger rows wait (`EachOtherPlayerDrawsCardEffect`, `TargetOpponentCreatesTokenEffect`).
- 2026-11-25, face-down board views (`missing-features-to-do.md` entry 81): a player can't tell their own face-down permanents apart on the board or in id-only prompts; needs a per-viewer permanent view and client work.
- 2026-11-25, Tribute's reduced placement: a halved tribute (an opponent's Halving Season) reads as unpaid; CR 702.104b says "as specified".
- 2026-11-25, "if you do" after a replaced draw (defect run D14, line 17): a draw replaced by drawing more (Alhammarret's Archive) records no cards, so `HappenedThisWay.drew()` and `AmountThisWay(DRAW)` read nothing; `EachOpponentDrawsCardEffect` waits for it.
- 2026-11-25, Nowhere to Run (D16, line 12): should its "hexproof doesn't apply" cover any player's spells and abilities (it is modelled as its controller's only)?
- 2026-11-25, set exchanges (D19, line 22): Reins of Power, Twist Allegiance and Perplexing Chimera don't check "can't be controlled by other players"; the set reading of CR 701.12a is unsettled.
- 2026-11-25, Reality Twist on unlisted land types and Boseiju's rider under a land-mana replacement (D21): the 2013 ruling is ambiguous; CR 106.6a for Boseiju.
- 2026-11-25, a seeded game random source: about 180 `ThreadLocalRandom` sites, so tests can't fix a random pick.
- 2026-11-26, T1: the tapped-and-attacking (and enters-with, exiled-later) token copies of a source under Esix, Mirrormind Crown and Moonlit Meditation (Splinter Twin, Nacatl War-Pride, Furnace-Blessed Conqueror, Mirage Phalanx, ...): do the substitutes keep the creating effect's instructions? Today they aren't offered; Esix's ruling "applies before anything that modifies how those tokens enter" and Jinnie Fay's ruling point to carrying them. Blocks the T merge.
- 2026-11-26, U8: Madame Null, Power Broker asks "pay 0 life" for a 0-power creature (pinned in `MadameNullPowerBrokerTest`), against orchestrator decision U3b Q4.
- 2026-11-26, U6b: Decoy Gambit offers "have you draw a card" when its caster can't draw; may an opponent choose it then (CR 608.2d, 121.3)?
- 2026-11-26, U7 Q3: a client chooser for an "[A] or [B]" additional cast cost (`AnyOfCost` as `addCastingCost`; CR 601.2b) is a missing feature: Bone Shards, Demand Answers, Minion Missile and Souls of the Lost can't be cast with their discard from the UI.
- 2026-11-25, U3b Q5: the special-action delayed payments (Quenchable Fire, the Asps, Sabertooth Cobra; CR 116.2c) raise no tax-paid event (matters only with Tax Taker).

#### Orchestrator decisions for the owner to review

- 2026-11-25, U3c Q1: the four "counter it" punisher cards (Brain Gorgers, Dash Hopes, Phantasmagorian, Temporal Extortion) use the plain loop, paid as each player chooses and countered after the last answer (CR 603.3).
- 2026-11-25, U3c Q2: Development draws at most one card per round, every opponent still asked (its 2006 rulings; CR 118.12a reading open).
- 2026-11-25, U3c Q3: Development's draw is the new `SourceControllerDrawsCost`.
- 2026-11-25, U3c Q4: the hard AI subtracts an estimate of a non-mana ransom and never pays life or damage at or above its life total.
- 2026-11-25, U3c Q5: the sacrifice-candidate guards (Sigarda, "can't be sacrificed", The Master's tokens) fixed in U3c, U2's ward included.
- 2026-11-25, U3b Q3: Spelltithe Enforcer asks only the opponent who cast the spell ("that player").
- 2026-11-25, U3b Q4: at an amount of 0, Tymna and Madame Null don't ask.
- 2026-11-25, U3b Q1, Q2, Q6, Q7: the per-payer branch and the Lurking Spinecrawler, Read the Runes and The Dalek Emperor fixes in U3b2; Decoy Gambit in U6; the `XOrY…Cost` fold in U7.
- 2026-11-26, U4 Q1: U4 split into U4a to U4d, the record and its handlers deleted at the end of U4d.
- 2026-11-26, U4 Q2: mandatory "do X. If you can't, Y" forms are steps plus a conditional, not `UnlessPaysEffect` (CR 118.12).
- 2026-11-26, U4 Q3: "that player" with no "target" (Tomb Blade, Pillar Tombs of Aku) is the event's player, not a declared target (CR 115.1).
- 2026-11-26, U4 Q4: Satya pays the token's mana value read as the delayed ability resolves, last-known once the token left (CR 608.2h), the payer still asked.
- 2026-11-26, U4 Q5: Fraying Line's exile is one event (CR 608.2c, 603.10a).
- 2026-11-26, U4 Q6: the hard AI keeps scoring a source-sacrifice branch as nothing (parity; a defect line asks for the source's value).
- 2026-11-26, U4 Q7: Balduvian Fallen moved in U4d; Flash in U4b, reduced by the full {2}.
- 2026-11-26, U4 Q8: "any player may pay" asks every player even after one paid (Icy Prison, Nakaya Shade, Rhystic Circle, Rhystic Shield; CR 101.4), an upkeep mana payer with no mana source isn't asked.
- 2026-11-26, U7 Q1, Q2: the client is unchanged: free-cast options keep today's order (discard first for Bone Shards, Demand Answers, Minion Missile) and "P pays {m} for C." stays where it is logged.
- 2026-11-26, U7 Q4: the mana alternative of an any-of cast cost is paid with the total cost (CR 601.2f).
- 2026-11-26, U7 Q5: Redirect Lightning stays on `PayLifeOrPayManaCost`.
- 2026-11-26, U7 Q6: the any-of chooser after targets is a defect line.
- 2026-11-26, U7 Q7: U7 split into U7a, U7b, U7c; the engine's free-cast chooser and the exile and library-top request fields in scope.
- 2026-11-26, Boost Q1: the survivor is `BoostEffect` (handler `BoostEffectHandler`), every call site on factories.
- 2026-11-26, Boost Q2: an explicit `AmountsRead {SOURCE, BOOSTED}` set by the factory.
- 2026-11-26, Boost Q3: a target or set boost refreshes the boosted creature's waiting abilities' snapshots too (CR 608.2h).
- 2026-11-26, Boost Q4: today's log lines kept.
- 2026-11-26, Boost Q5: `CreatureBoostEffect.targetedBoost` by `declaresTarget()`, AI only.
- 2026-11-26, Boost Q6 to Q8: parity, one defect line each.
- 2026-11-26, Boost Q9: split into B1 and B2.
- 2026-11-26, Damage P to R Q1: the player kinds leave `subject` null, so a misuse fails loudly.
- 2026-11-26, Damage P to R Q2: Curse of Thirst is `eachEvaluatedFor(PermanentCount(...), triggeringPlayer())`, pinned first.
- 2026-11-26, Damage P to R Q3: the player handler's prevention checks moved into the pools in defect run D26, failing-first (CR 616.1).
- 2026-11-26, Damage P to R Q4: Typhoon's record absorbed in P.
- 2026-11-26, Damage P to R Q5: the excess redirect waited for R (done in run X).
- 2026-11-26, Damage P to R Q6: Aegis of Honor's redirect before the player pool was a defect line (fixed in D26); the one-damage-event-per-effect rule is cited by no CR number.
- 2026-11-26, rest of step 10 Q1: run S's survivor is `CantAttackOrBlockUnlessEffect` with `attack` / `block` / `attackOrBlock`.
- 2026-11-26, rest of step 10 Q2: run D fixes every player's amount before anyone draws (CR 608.2h).
- 2026-11-26, rest of step 10 Q3: run D's AI own-draw rule (only the controller's draw is its card advantage).
- 2026-11-26, rest of step 10 Q4: R2's "this card" is `sourceObjectCard()` (the owner's "it" decision).
- 2026-11-26, rest of step 10 Q5: run T checked Esix's and Mirrormind Crown's rulings and kept the open case (the T1 question above).
- 2026-11-26, rest of step 10 Q6: `MakeAllCreaturesUnblockableEffect` stays out (CR 611.2c, Venser's ruling); `ExileGraveyardCardsEffect.eventValueFilter` stays; the survivor's log line wins.
- 2026-11-26, gated runs Q2 (U8): an X chosen while paying is made the entry's X (CR 107.3f).
- 2026-11-26, gated runs Q3 (U8): X = 0 declines, as before (no card observes the CR 118.12 difference).
- 2026-11-26, gated runs Q4 (U8): the strict question rule for "pay X" (asked unless the payer certainly can't pay X = 1); a rare extra prompt.
- 2026-11-26, gated runs Q5 (R2): Bogardan Phoenix's counter is put after it enters (its Oracle), not "enters with".
- 2026-11-26, gated runs Q6: the only player-facing changes beyond the rules are the survivors' log lines and U8's prompt text ("Pay {X}{R}?").

---

## 18. Rules defects seen during the effect cleanup

**Status:** open · **Found:** 2026-11-23 to 2026-11-26, the effect cleanup's defect queue

The effect cleanup's plan and queues were deleted when it closed (2026-11-26). Their last versions:
`git show 3b0d60d42d4:effect-cleanup-refactor.md` (plan, Standing knowledge, the owner's decisions, step 10's Findings) and
`git show 3b0d60d42d4:effect-cleanup-queues.md` (fold queue, defect queue). What the cleanup built for card and engine authors is in
`agent-docs/architecture/effect-building-blocks.md`; `scripts/effect-audit-ledger.csv` keeps every class's verdict and
`python3 -I scripts/effect-inventory.py` the inventory.

**Problem.** 252 open defect lines of the cleanup's defect queue (every line not struck through), one line each: card or class,
the rule, how sure ("of the code" = read in the code, "unverified" = not re-checked). The queue line has where it was seen and
more detail.

**Why it matters.** Rules accuracy first (AGENTS.md); each line is behaviour the engine gets wrong or can't show.

**Proposed direction.** One group per defect run, failing tests first, each fix with its Oracle text, ruling or CR rule.

**Prompt to start it:**

```
Do refactors-to-do.md entry 18, group "<group>": for each line, read its full queue line (git show
3b0d60d42d4:effect-cleanup-queues.md), confirm the defect with a failing test, then fix it. Read AGENTS.md and
agent-docs/ARCHITECTURE.md first; check Oracle text and rulings with the Scryfall MCP and every CR number with get_rule. A line
that turns out not to be a defect is removed with the reason in the report. Run only the touched test classes by FQCN.
Don't commit.
```

#### Face-down and lost-ability reads (CR 613.1f, 708.2a) (12)

- Kappa Cannoneer, Lockjaw, Slicer, Full Steam Ahead, Vines of Vastwood: "can't be blocked / sacrificed / the target" are granted abilities "loses all abilities" removes, but are rules of the game (CR 611.2c). Unverified.
- `cardsThisWay(kind, filter)`, the copy, cast and name / mana-value readers, the entry batch's "can't enter" filter read a face-down exiled card's hidden face (CR 406.3a). Unverified.
- Token copies of face-down permanents, other consumers of a face-down object's real characteristics, opaque card ids: not looked at. Unverified.
- Soulless Jailer: `canCastSpellFromZone` reads `card.hasType(CREATURE)`, not the spell (an Adventure, a modal back face). Unsure, low.
- Stoic Star-Captain and Zilortha model their source's rule as a granted ability, which "loses all abilities" on the creature removes (CR 613.1f). Fairly sure.
- `CastingPermissionService` spell-limit, Ethersworn Canonist, most-recent-spell and global land-restriction readers skip lost abilities but not face-down sources (CR 708.2a). Sure of the code.
- `DamagePreventionService` per-source preventions have no guard; the cleric, all-but-one and spell-token preventions ask only `hasLostAllAbilities`; `grantedAttachedPreventionSurvives` reads the Aura's printed statics (CR 708.2a, 613.1f). Sure of the code.
- `GameQueryService.getEffectiveCardColors`' off-battlefield scan (Celestial Dawn) asks only the floating flag (CR 613.1f). Sure of the code, low.
- `areCreatureDeathTriggersSuppressed`' simultaneous-death scan asks only the floating flag and skips no face-down source (CR 708.2a). Fairly sure, low.
- `DrawService` Alms Collector, Words-return and Quantum Riddler readers skip no face-down source (CR 708.2a). Sure of the code.
- The static-set filter reads in `GameQueryService.layeredQueryReads` (`matchesLeaf`) use printed keywords and types ("creatures with flying" in static locks other than the tap lock; Katabatic Winds' attack/block half unverified). Fairly sure.
- Painter's Servant doesn't colour cards that aren't on the battlefield, or spells (Oracle text). Sure.

#### Two-player assumptions (`getOpponentId`, first seat) (17)

- Notion Thief, Hullbreacher, Alms Collector (`DrawService`), Elesh Norn (`areOpponentPermanentETBTriggersSuppressed`) look at one opponent; Distant Memories asks the first opponent by seat and finds its controller as "the other player"; Infernal Denizen offers the first opponent. Sure.
- Shared Fate exiles from `getOpponentId` (the drawer chooses); Abstract Performance, Brilliant Ultimatum, Burning-Rune Demon, Captivating Glance's clash take the first other player. Sure.
- Death or Glory, Ecological Appreciation take the first other seat; Deliver Unto Evil, Fateseal use `getOpponentId` (the controller chooses, CR 700.2e's reading); Covenant of Minds takes the first non-opponent seat as controller; Triskaidekaphobia declares the first survivor the winner (CR 800.4a). Sure.
- Galactus forces the first opponent on a tie; Norn's Annex's tax and Nahiri's `MustAttackPlayerEffect` count use `getOpponentId`. Sure.
- Stand or Fall serves only `getOpponentId` ("each defending player") and floats `OnlyChosenCreaturesCanBlockEffect` on every player; Favored Enemy reads the first opponent's library. Sure.
- Hostile Negotiations (always the first other seat), Memories Returning, Murmurs from Beyond pick the opponent by seat or ask the controller. Sure.
- Zur's Weirding offers the payment only to `getOpponentId` ("any other player"); Verdant Mastery likewise. Sure.
- Threats Undetected, Turtles Forever: "an opponent chooses" takes the first opponent. Fairly sure.
- `TargetOpponentCreatesTokenEffect` derives "target opponent"; `OpponentGainsControlOfSourceCreatureEffect` picks by seat. Unverified.
- `MayEffect`'s `DEFENDING_PLAYER` doesn't read the recorded defending player (CR 508.5; Harbor Guardian's kind); Soldevi Heretic derives "target opponent" and targets nobody. Unverified.
- `CombatBlockService.handleDeclareBlockersStep`, `getBlockableAttackerIndices` and `ConditionEvaluationService.defendingPlayerControlsMatchingPermanent` take the defending player as `getOpponentId` (CR 506.2, 508.5) (CR 509.1). Sure.
- Ingenious Mastery: "an opponent creates two Treasures and they scry 2" gives the tokens to the first seat and has every opponent scry.
- `DrawService`: two Notion Thief / Hullbreacher / Alms Collector sources under different opponents apply in seat order; the affected player should choose (CR 616.1). Sure, low.
- `ReturnCardExiledWithSourceToBattlefieldEffectHandler` puts a creature onto the battlefield attacking with no attack target; its controller should choose one (CR 508.4). Fairly sure.
- Seven cards choose "an opponent" after an exile, search or reveal inside a handler or interaction (Abstract Performance, Brilliant Ultimatum, Hostile Negotiations, Ecological Appreciation, Threats Undetected, Turtles Forever, Verdant Mastery): they need one prompt mid-flow. Sure.
- Wake to Slaughter's opponent choice (`opponentChoosesOneForHand`, its own `wakeToSlaughterChosenCardId` state) is unchecked for multiplayer. Fairly sure from the code.
- Vislor Turlough with 3+ players: whether the goad step resolves after the opponent prompt, with the new controller. Unverified.

#### Simultaneity and APNAP (CR 101.4, 603.10a, 608.2e) (23)

- Multi-dealer damage is dealt one dealer at a time: the bite (runs 37, 38), Rakdos Charm's third mode (T1, defect run 2: needs the open simultaneous event, `DamageSupport.beginDamageEvent`), Judgment of Alexander (P12). Sure.
- Disorienting Choice exiles, Eumidian Wastewaker sacrifices or discards at each player's answer. Sure.
- Ezuri's Predation pairs by battlefield order and fights one at a time (rulings: you choose the pairs, fights are simultaneous). Sure.
- Glimpse of Tomorrow, Morphic Tide put each Aura in its own continuation, not one batch.
- Ol Rin's Searing Light, Gilded Ambusher exile each opponent's choice before the next chooses; Dispersal returns and discards one opponent at a time; The Second Doctor, Kwain draw before the next player chooses (CR 121.2c). Unverified.
- Kairi, Leadership Vacuum, Resounding Wave's cycling return one permanent at a time. Likely the same, untested.
- Unfinished Business puts its two attachments onto the battlefield one after the other. Unverified.
- Mythweaver Poq's simultaneous duplicates are one conjure event each (unsettled). Unverified.
- Disorder deals its damage as two events (the players first, then the white creatures) where its one sentence is one event; `addSimultaneousEffects` would join them. Fairly sure.
- An additional time a triggered ability triggers (CR 603.2d) is made by `GameData.TriggerAwareStackList.add` copying the first entry (`copyForNewStackObject`) once it reaches the stack, so the copies keep the first instance's targets and any mode announced before it got there; each instance should choose its own (CR 603.3c, 603.3d). Unsure.
- `ParleyEffect`'s token form (Cutthroat Negotiator, Innocuous Researcher) creates the tokens inside its own handler, so a token-creation prompt pauses it after the draws' order is set and a rerun would reveal and draw again; Storm Fleet Negotiator and Phabine now use the step form. Unsure (by reading).
- Tribute's "paid" check is `placed >= N`, so a halved tribute (an opponent's Halving Season) reads as unpaid (CR 702.104b: "as specified").
- Experience counters and `MultiPermanentChoiceHandlerService`'s poison counters name no placer, so Vorinclex and Halving Season don't apply to them. Sure.
- The 6-argument entering `replaceCounters` (a named placer) skips `appliesToAllPermanents` doublers on opponents' permanents (Primal Vigor's counter half). Fairly sure.
- `TokenKindReplacements` and `JinnieFayTokenReplacements` read `event.controllerId()`'s battlefield, not the tokens' creator after Crafty Cutpurse; the count replacements resolve with Gather Specimens for creature tokens, though Chatterfang's ruling applies entering-control changes after (CR 616.1). Fairly sure.
- The two remaining fixed-order `getTokenMultiplier` callers (`CreateTokensAttachedToControlledCreaturesEffectHandler`, `ReturnTargetCreatureToHandAndCreateTokenCopyEffectHandler`) skip the ordered token event.
- The recipient damage multipliers (Gisela, Castigator, floated) are one pool option and the controller and source multipliers another, so a "plus N" can't be ordered between two of them (CR 616.1). Sure of the code.
- Precursor Golem, Ink-Treader Nephilim: the copies' controller doesn't choose the order they go on the stack (rulings). Sure.
- `PutExiledCardsIntoOwnersGraveyardEffect` (Covenant of Minds, Dakra Mystic) gives no CR 404.3 order prompt. Sure, low.
- Eumidian Wastewaker counts a sacrificed land token, or a permanent that is a land only on the battlefield (Ashaya), as a land card put into a graveyard (ruling). Sure.
- Ezuri's Predation: "those creatures" are read at the fight, not when counted (CR 608.2h).
- Flame Spill, Pigment Storm, Ravenous Tyrannosaurus, Megatron, Ram Through: the excess ignores damage from other sources dealt at the same time (a simultaneous step's damage to the same creature, CR 120.4a); only `SequenceEffect.simultaneous` could combine them, and none of these cards does. Sure of the code, low.
- `AttackLegalityService.isCantAttackDueToGlobalRestriction`'s planechase branch judges a plane's `CreaturesCantAttackUnlessPredicateEffect` exemption with no filter context (the context it builds is used only by the attack-or-block branch), so a source-relative exemption (Ensnaring Bridge's hand size, `youControl`) on a plane would misjudge; no plane uses one today. Sure of the code, latent.

#### Missing events or records (searches, coin flips, seeks, shuffles, counters, draws) (16)

- The Ring's level 2–4 abilities aren't implemented, only legendary and can't-be-blocked-by-greater-power (CR 701.54c). Sure.
- The queue's may-searches (New Frontiers, Natural Balance, ...) have no "do you search?", so a player can't search and find nothing (CR 701.23a, 701.23b). Unverified.
- A same-name search with no match asks nothing (no look, CR 701.23a); Haunting Echoes with nothing exiled likewise. Unverified.
- A draw replaced by exactly one draw of the same player (Enduring Renewal, Aladdin's Lamp) still reveals: `DrawService` reports no replacement. Unverified.
- `DealDamageTo{AnyTarget,Players,TargetCreature}Effect` lose a player hit from the step result when a replacement-order prompt reruns them. Unverified.
- `DrawCardEffectHandler` records no cards (DRAW outcome amount 0) when a draw is replaced by drawing more (Alhammarret's Archive), so `AmountThisWay(DRAW)` and `HappenedThisWay.drew()` read nothing drawn where cards were drawn because of the instruction; it keeps Malignant Growth on its own handler (CR 614.6). Fairly sure.
- The library-search flows handled before the generic path (same-name, subtype and colour picks, basic-land queues, held-out pools, `HAND_OR_GRAVEYARD`) record no found cards in SEARCH. Sure of the code.
- The selection-follow-up drafts (`LibraryRevealChoice`: Support Skyforge, Adaptive Armorer, Dragon Typhoon, Masterpiece Vault) record no CONJURE step.
- When a library search is prevented, `SearchLibraryAndOrGraveyardForCardToHandEffect` (and the choice handler's `finishSearch`) still shuffles on a fruitless search or a decline ("If you search your library this way, shuffle"). Sure of the code, low.
- Last Light of Durin's Day's "search your hand and/or library" always searches (and shuffles) the library; the player can't choose to search only the hand. Sure, low.
- Suncleanser's player lock (`CantGetCountersEffect.onPlayers`) is not asked by the spark cost, proliferate's experience/energy/poison writes, `AddAnotherCounterOfEachKindToTargetEffectHandler`, `DamagePreventionService` rad, `MayHaveThisLandEnterTappedForRadCountersHandler`, `ExperienceCountersEffectHandler`'s direct put or `DoubleCountersOnControllerEffectHandler`'s energy (Suncleanser ruling: an affected player can't get counters). Sure of the code.
- `MayMiscHandlerService.handleLookAtTopCardPutLandOrCreatureChoice`'s Aura put path records no RETURN step, so an "if you didn't put a card onto the battlefield" after an Aura put would misread. Sure.
- Errant Minion, Power Leak deal 2 − X instead of preventing X: no damage-prevented event, and no CR 616.1 ordering among other prevention or replacement effects (CR 615.1). Sure of the code, low.
- `CombatAttackService` throws "must attack the specified player" for a `MustAttackIfAbleEffect(defenderId)` instead of weighing it against other requirements (CR 508.1d). Sure of the code, low.
- Journey for the Elixir: taking both cards from the graveyard doesn't shuffle, though its text says "search your library and graveyard … then shuffle". Sure of the code.
- `CoinFlipService`'s Pokey d20 (the roll that replaces a flip) ignores dice advantage (Pixie Guide). Unverified.

#### Wrong targets and choices (22)

- `ChoosePermanentsEffect` keeps a lone chosen permanent as the source's chosen permanent, so Deepfathom Echo (if folded onto it) or any copier choosing its model would leave a "the chosen creature" card's copy with a choice it never made (Dauntless Bodyguard) (CR 607.2d). Sure of the code.
- `CardSubject.TriggeringCards` names a card only in the stay its event put it in, so `bindCardSubjects` reads no type for a discarded card that was exiled instead or has left the graveyard, where the text reads last-known characteristics (CR 608.2h; Diviner of Fates' fold). Sure of the code.
- Soundwave's "from their graveyard" is the damaged player's; the scope admits any opponent's. Unverified.
- Opposition Agent's player can take a cast-only card as the find; the AI's search strategy answers only for the searcher. Unverified.
- `beginAnyTargetChoice` and the permanent-or-player prompt don't drop what can't be chosen; about 400 counting callers unswept. Unverified.
- A name prompt that requires a type offers a copy's name by the copy's types (CR 201.4a). Unverified.
- The opponent-owned processor head and Ulamog's Nullifier offer face-down cards one by one where Ruin Processor chooses piles. Unverified.
- `ImprovisationCapstoneCastChoice` offers an Adventure card only as its creature, with no face choice (CR 716.3). Sure of the code.
- `ExileTopCardsAndMayCastSpellsEffect` with a total cap checks an Adventure offer against the card's own mana value, not the Adventure's (CR 716.3a). Fairly sure.
- `SubjectResolutionService.attachedId` gives no host for an Aura that left together with its host when the entry has no source snapshot (the shuffle survivor reads the captured target instead). Sure of the code, low.
- Kefnet, Reversal of Fortune, Spellweaver Volute: a copy of a card is created and cast in exile, not in the card's own zone (CR 707.12; Kefnet and Reversal rulings), so a "cast from hand / graveyard" trigger misses it. Sure.
- The Clone Saga (SPM 28) chapter II offers new targets for the copy although the text doesn't say it may (CR 707.10, 707.10c). Fairly sure.
- Nowhere to Run: "spells and abilities" are any player's, but it is modelled as its controller's only (multiplayer). Sure.
- EyeForAnEye, Harm's Way, Jade Monolith, Shaman en-Kor, General's Regalia, Nova Pentacle collect damage sources with their own lists, missing part of CR 609.7a's set (sources of waiting shields, cards stack objects refer to); move them onto `ChooseDamageSourceEffect`. Sure of the code.
- The copy-service turn-face-up path used by state-based actions doesn't check "can't be turned face up" (CR 101.2). Unverified.
- The AI's filtered must-be-blocked choice with menace (two blockers needed) is approximate: the greedy simulator doesn't narrow, and Easy and the exhaustive phase may skip the block (CR 509.1c). Fairly sure.
- `ExileGraveyardCardsEffect.defendingPlayerOnly` (Graven Abomination): the flag is never read, so any opponent's graveyard can be targeted (multiplayer). Sure of the code.
- Ulamog's Nullifier (`TwoOpponentOwnedExiledCardsToGraveyardChoiceInteractionHandler`): face-down picks are taken by id, not at random from their pile (2015-08-25 ruling). Fairly sure.
- Zara's hand-to-battlefield put calls `handleCreatureEnteredBattlefield` directly rather than the entry batch. Unverified.
- Descendants' Fury offers a choice among creatures that can't be sacrificed after the "may" (CR 608.2d). Sure, low.
- Avatar's Wrath's airbend is scored by `SpellEvaluator` as single-target exile removal (AI). Unverified.
- Rasaad, Radiant Monk: the specialize trigger's target spec is any exiled creature card, not one exiled with this creature. Sure of the code.

#### Payments and costs (25)

- Moxite Refinery's `CounterType.ANY` cost removes kinds in enum order; the player should choose (the kind prompt serves one counter only).
- Feather, Radiant Arbiter's {2} per creature is paid only from the pool: no mana abilities (CR 605.3a). Fairly sure.
- Jaya Ballard's emblem grants flashback (an alternative cost, CR 702.34a, 118.9a) where the text permits casting for the mana cost. Fairly sure.
- Charmed Pendant offers a monocoloured hybrid symbol's generic half; Animation Module offers only poison and energy player counters; Thran Portal's added life cost applies to its non-mana abilities; Tevesh Szat's -10 puts commanders outside the entry batch.
- `ExileTopCardMayPlayThisTurnEffect.withAnyManaType()` skips land cards (a land's Adventure).
- The library and exile free-cast flows (`LibraryChoiceHandlerService.castCardWithoutPaying`, `ExileFreeCastQueueSupport`) pay no cost increases (CR 601.2f): cascade, Sunbird's Invocation, Aetherworks Marvel, Kiora and the rest (fold run 18b fixed the graveyard flow only). Sure of the code.
- The library `CAST_WITHOUT_PAYING` flow offers no Adventure (CR 716.3): Kiora, Velomachus, Aetherworks. Sure of the code.
- A modal double-faced card offered by a matching back face can then be cast as either face (`legalModalOptions` ignores the cast filter; Velomachus with Jadzi). Fairly sure.
- Whispersteel Dagger and Xanathar's "as though it were mana of any color" is applied as any type, so coloured mana pays {C} (CR 609.4b). Fairly sure.
- `MayCastHandlerService.castRevealedCardWithoutPaying`, `SplitCardCastSupport`'s `payManaCost=false` casts and `LibraryChoiceHandlerService`'s search casts pay no cost increase (CR 601.2f); move them onto `payFreeCastCostIncreases` and its mana window. Sure.
- A mandatory exile free cast (`completeInput=false`: Omen Machine, suspend) offers no kicker, though the rulings allow optional additional costs. Sure.
- The free-cast queue asks for the cost-increase mana before modes and targets; a spell then left with no legal target leaves the mana in the pool. Sure of the code.
- The per-target cost readers have no lost-abilities or face-down check (CR 613.1f, 708.2a). Sure.
- `XValueChoiceAiStrategy` answers a life payment with its maximum, so the AI pays its whole life (Plunge into Darkness, Vizkopa Confessor; CR 119.4). Sure of the code.
- `AbilityActivationService.getActivatedAbilityAdditionalGenericCost`: with a {0} equip or power-up cost, the tax is returned without cost reductions applied (CR 601.2f). Fairly sure of the code.
- "Mana of any type" exile and graveyard permissions (`withAnyManaType`, `spendingManaOfAnyType`: Mnemonic Betrayal, Gonti, Dire Fleet Daredevil and others) are paid as any colour when the cost has {C} (`SpellCastingService` `anyColorForColoredSymbols`), so coloured mana can't pay {C} (CR 106.1b, 609.4b). Sure.
- Flotsam // Jetsam (Jetsam) refuses Bone Shards, Minion Missile and Souls of the Lost instead of letting their sacrifice-or-discard cost be paid (CR 118.9d); before fold run 49b it cast them with the cost skipped. Sure of the code.
- Gargantuan Gorilla, Bronze Bombshell, Elder Spawn: damage from a source sacrificed earlier in the same resolution loses Aura-granted keywords (`sourceHasKeyword` reads the trigger-time snapshot; CR 608.2h). Sure of the code.
- Collective Effort and other tap-escalate spells cast from a graveyard through a flashback grant pay no escalate taps (CR 702.120a). Fairly sure.
- Chisei, Heart of Oceans' `ForcedCostOrElseEffect` counter removal may take the first kind of counter instead of letting the payer choose. Unverified.
- Development (Research // Development): two or more opponents letting its controller draw in one round draw one card, not one per accepting opponent (CR 118.12a: each paid cost is that player's; its 2006 rulings don't settle it). Sure of the code.
- A one-permanent sacrifice ransom paid at once (`ResolutionCostPaymentService.pay` of `SacrificePermanentCost`: ward "sacrifice a creature", Blood Funnel, Mana Vortex and now Brain Gorgers) asks the pick even when only one permanent could be sacrificed (the "one possible answer is made unasked" convention, CR 608.2d); the paid-together path picks it unasked. Sure.
- The paid-together tap ransom (Reservoir Kraken) logs "taps an untapped permanent" / "chooses to tap an untapped permanent": the ransom text of `TapMultiplePermanentsCost` names no permanent kind (its filter has no label). Sure.
- An X chosen while paying (`PayXManaCost`) is paid with no answering prompt (`Payment.answering` null), so a {B} paid with 2 life under K'rrik doesn't ask a Lich's Duel Mastery death replacement first (CR 616.1); the old `PayXMana…` handlers couldn't pay with life at all. Sure of the code.
- Madame Null, Power Broker asks "pay 0 life" for a creature with 0 power (HEAD's behaviour, pinned in U8: `MadameNullPowerBrokerTest.zeroPowerAsksForZeroLife`); the U3b orchestrator's decision (Q4) says it doesn't ask (CR 119.4).

#### Layers and copy values (19)

- Wickerwing Effigy's "becomes a black Bird ... Fairly sure, low.
- Esix's copies keep none of the replaced copy's instructions (Faerie Artisans); Dance of Many waits for it. Sure of the code.
- Step 10 run T1: `CreateTokenCopyOfSourceForEachOtherPlayerEffect` (Life of the Party) and `…ForTargetPlayerEffect` (Wedding Ring) offer no first-token-creation replacement to the players who create (another player's Mirrormind Crown, Moonlit Meditation; CR 701.7a). Sure of the code.
- Step 10 run T1 (the plan's note): a token copy of a source reads `PermanentCopierService.cardToCopy`, not `copiableCard`, so a bestowed, Licid or became-a-creature source is copied with non-copiable values (CR 707.2). Unverified on a real card.
- `CreateTokensAttachedToControlledCreaturesEffect` pairs tokens by `getTokenMultiplier`; an order prompt returns no ids. Unverified.
- A split card's whole-card type and colour prohibitions weren't traced. Unverified.
- The returned-permanent animations (Brilliance Unleashed, Tune Up, The War in Heaven) apply after the permanent entered. Unsure.
- Thought Partition and Hydroponics Architect write colour, mana cost, supertype and subtype into runtime copies (layer 1, CR 613). Sure of the code.
- Starfield of Nyx: `isAnimatedByStarfield` makes a battlefield enchantment a creature ignoring later layer-4 type removals, and `wouldEnterAsCreature` ignores Starfield (Torpor Orb). Sure of the code, low.
- `snapshotEnterTappedTypes` / `applyEnterTappedEffects` match printed card types, not the permanent as it would exist on the battlefield (CR 614.12; e.g. an artifact animated by March of the Machines vs Root Maze's type set). Sure of the code, untested.
- Ability borrowers in a CR 613.8b dependency loop are approximated by assembly order, so a three-borrower cycle can miss a lender's borrowed abilities. Sure of the code, rare.
- `ProtectionFromColorsEffectHandler` gives a self-subject fixed protection to the host when its source is attached (a bestowed or reconfigured creature with printed protection); CR 702.16a makes it the object's own. Sure of the code.
- Mask of Law and Grace, Shield of Duty and Reason, Strength of Isolation and Strength of Lunacy stay exempt from their own protection when recoloured, though they have no "doesn't remove" text (CR 702.16c). Fairly sure.
- Kalain and Coin of Mastery read cast-time mana snapshots keyed by card id, so a copy of a permanent spell or a countered spell that later enters may read a stale amount (CR 707.10). Unverified.
- `BecomeCreatureEffect` (Opal Acrolith, Hidden Stag, 24 cards) replaces the card instead of floating a layer-4 and layer-7b effect, so a later copy effect drops the type change (CR 613.1a, 613.1d). Sure of the code.
- `PerpetuallyGrantStaticEffectToTargetCreatureEffectHandler`, `PerpetuallyGrantStaticEffectToSourceEffectHandler`, `DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectsIfCreatureDamagedEffectHandler`, `PerpetuallyBecomeCreatureEffectHandler` and `ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffectHandler` write perpetual changes into an exchanged card, which a copy then copies (CR 707.2). Sure of the code.
- `EnterPermanentsOfTypesTappedEffect.matching` filters judge the entering permanent as it is, not as it would exist on the battlefield (CR 614.12). Unverified.
- `TextChangeTransformer` rewrites change effect identity, so a text-changed targeted trigger can lose its target group. Unsure.
- Whether a text change made to a spell on the stack (Sleight of Mind on an instant, `spellTextReplacements`) reaches its resolution was not traced. Unverified.

#### Logs and hidden information (10)

- `cardsThisWay(REVEAL)` names a card a later step reordered within its library (CR 701.20d); no card reads it so. Unverified.
- Found in D10: Support Skyforge's spellbook selection exiles the two cards not chosen face up and logs their names before the follow-up removes them (`LibraryChoiceHandlerService` rest-to-exile branch); a spellbook card not drafted goes nowhere.
- `ChooseCardEffectHandler` logs a chosen card unnamed whenever it is in a hand (fold run 12e's zone check), even a revealed one; keep hidden-ness on the subject (fold run 20's note: a zone-based prompt check broke Addle and Intuition). Sure, low.
- A free cast refused by Teferi or Rule of Law logs "has no legal targets".
- `SetBasePowerToughnessEffectHandler`'s set and player-target log lines print "null" for a null side. Sure.
- Shadow of Doubt logs nothing since fold run 34: `GrantStaticEffectToPlayerEffect` has no log line. Sure, low.
- `getRevealedOpponentHand` returns only the first opponent's hand (multiplayer). Sure of the code.
- `CantAttackThisTurnEffectHandler`'s set branch logs the source by `entry.getCard().getName()` with no face-down redaction and doesn't bind its filter's chosen values (only the except branch goes through `lastingRule`). Sure of the code.
- `GameViewProjectionFactory.collectFaceDownReveals` decides looks from printed effects while `mayLookAtExiledCard` holds granted looks, so one question is answered twice; `exilesHandsFaceDown` doesn't look inside `MayEffect`. Sure of the code.
- `ClashSupport`'s log says "opponent reveals" without naming the player. Sure, low.

#### Other (104)

- Casting checks left from runs 40 and 41: Scrycast (Biting Remark; `ScryInteractionHandler`, `ScryAiStrategy`, code); Knowledge Pool ignores the chosen card's "cast only during" (its ruling); `SplitCardCastSupport.beginFromExile` judges the whole split card (CR 709.3a, code); the may-cast offers don't ask the shared check; suspend asks no type restriction (Cease-Fire; CR 601.3); an ability leaves the stack before it resolves, where CR 608.2n removes it last. Unverified.
- Outside the entry batch: fixed in fold run 8; but a Painter's Servant it puts onto the battlefield got no as-enters colour prompt in a fold run 8 test (check whether `PutCardsOntoBattlefieldEffect` asks as-enters choices; unsure), fixed in fold run 21b (no observable test), fixed in fold run 10b, Karlach's specialize return (its "perpetually can't block" a static on a runtime copy; P02, low), Knickknack Ouphe's Auras attached after they enter (CR 303.4f, 303.4g; P02, fold: aura-host).
- Lim-Dûl's returned bestow Aura and the triggering-card heads (but the former `ReturnTriggeringCardFromGraveyardToBattlefieldEffect`, since step 10 run R2 `PutCardsOntoBattlefieldEffect(triggeringCards())`, whose entry batch asks the host) enter without a host choice (CR 303.4f). Unverified.
- Bad Wolf Bay's chaos ability is Don't Blink's replacement: creature cards entering from exile are shuffled, others enter; `CardsCantEnterBattlefieldFromZonesEffect` enforces only graveyard and library. Sure.
- Limitless Rekindling's pool is seven YECL printings; its text names a random instant or sorcery card of the whole expansion. Fairly sure.
- Cards with no printing identity: the `Supplier`-built conjures and spellbook drafts, the hand paths not marking a card conjured (runs 29 to 31); Protean War Engine (P08); Darigaaz, Skywriter Djinn, Oracle of the Alpha built by hand, Juggernaut Peddler, Calim, Mephidross Slime by constructor or `Supplier`, Goblin Influx Array's Reckless Ringleader a blank `new Card()` (P16). Sure.
- `ConjureCardToBattlefieldEffectHandler` gives a conjured land no land-enter handling. Unverified for a land with an enter ability (Mishra's Foundry and Forest have none).
- Pestilent Spirit's deathtouch reaches abilities of instant and sorcery cards; Forethought Amulet replaces only spells, not their sources' abilities (a "source is a spell" fact). Unverified.
- The ~25 handler pre-checks of `isPreventedFromDealingDamage` skip the damage event (Selfless Squire misses them) (CR 616.1). Unverified.
- `TriggeringPermanentPower` / `Toughness` prefer the trigger-time value over a departed permanent's snapshot (CR 608.2h). Unverified.
- The set exchanges (Reins of Power, Twist Allegiance) and Perplexing Chimera don't check `cantBeControlledByOtherPlayers`; CR 701.12a's set reading unsettled. Unverified.
- Enduring Angel that can't transform skips its replacement (differs only under "can't lose the game"). Fairly sure, low.
- Fear of Sleep Paralysis' lock is read only by the untap replacement, not other stun removals. Fairly sure, low.
- `hasLukkaExilePermission` matches a name starting "Lukka," and the printed type, not the Lukka subtype. Sure, low.
- Mug's graveyard and exile permissions don't share "a spell". Sure, rare.
- Make Your Own Luck puts all three cards into hand, then plots the chosen one (needs a plot library destination).
- Frontend: the client ignores `MULTI_PERMANENT_PICK`'s minimum (code) and shows graveyards for an index pick over a pool; `GameSimulator` answers a multi-pick with the first maximum.
- `PlayerDamageCandidateSource.context` ignores `DamageEvent.explicitSource`, so a fighting or biting permanent's damage to a player is judged as the stack entry's source. Fairly sure.
- No seeded game random source: about 180 `ThreadLocalRandom` sites, random-target picks included, so tests can't fix a random pick. Sure.
- Shaman's Trance, Tomik: "from their graveyards" is judged by zone, not by owner, so it also stops a cast from another player's graveyard. Sure of the code, low.
- Reality Twist: a Volcanic Island offers the unlisted Island type's U; `RealityTwistTest` asserts it and the 2013 ruling is ambiguous for unlisted types. Unsure.
- Boseiju, Hall of the Bandit Lord: when a land-mana replacement applies, the produced mana may lose its rider (CR 106.6a). Unverified.
- Caged Sun's "a land's ability causes you to add" misses land mana abilities without {T}; `youTapLandForMana` fires only for {T} abilities. Sure, low.
- `TappedLandManaSupport.producedManaTypes`' basic-land-type fallback (Mana Flare, `AddManaForEachOtherLandWithSameNameEffect`) adds mana for a tap that reported none (CR 106.12a). Unsure.
- Reality Scramble stays blocked: `PutTargetOnBottomOfLibraryEffect` records no `DepartureResult`, so `departedThisWay` has nothing to read (the filter exists since fold run 29c). Sure.
- AI: the Hard AI likely scores `PayFreeCastCostIncreaseEffect`'s "may" at 0 and declines a paid free cast. Unverified.
- `CounterSpellEffect` with several targets and `LIBRARY_TOP_OR_BOTTOM` opens overlapping prompts. Sure.
- Bow of Nylea and The Moonbase wait in blocked-other for a subject naming every target card: the activation path (`AbilityActivationService`'s `TargetedGraveyardCardsEffect` keying) needs the `targetGraveyardCards` branch the cast path got in fold run 50c. Sure.
- Panglacial Wurm can't be cast during the later picks of a multi-pick search; cast-only cards are offered only on the first prompt ("while you're searching"). Sure of the code.
- Valki (and any "becomes a copy" of a card with an exile/return pair): the copied return reads cards the copier exiled with its own abilities; it should read only cards its copied exile ability exiled (CR 607.1, 607.2a). Fairly sure.
- Eumidian Wastewaker's discard branch counts a land that a replacement exiled instead of discarding into the graveyard (`lastDiscardedCardTypes`; ruling). Unverified.
- Entry batch: when two members both have as-enters choices, the second one's choice is skipped while the first's prompt is open (CR 614.12). Sure of the code.
- `EachOpponentChoosesCreatureToTapAndGoadEffectHandler`, `PutTargetCreatureCardFromControllerGraveyardUnderTargetPlayerEffectHandler`: goading a creature the same player already goaded adds another goad (CR 701.15d). Sure of the code, low impact.
- `ManaSourceColorSupport` non-land branch: an `AwardAnyColorManaEffect` whose amount is 0 still counts every colour (CR 106.7). Sure of the code, low.
- SILVER is treated as a wildcard like ANY in `PermanentCounterSupport.removableCounterKinds`, `DoubleCountersOnTargetPermanentEffectHandler`, `AdjustEachCounterKindOnTargetEffectHandler` and others, but silver counters are real (Karn). Unverified reachability.
- Coastal Piracy (and other "a creature you control deals combat damage to an opponent" cards): the to-player trigger has no `AttackedTargetIsOpponent` condition, so damage redirected to its controller triggers. Fairly sure.
- Terminal Velocity: its `PutCardToBattlefieldThenEffect` then-steps grant haste and triggers over `Subjects.source()` / `toSourcePermanent`, which may name the spell rather than the put permanent (CR 608.2h). Unverified.
- `PerpetuallyGrantKeywordToSourceEffect` (6 cards): on the battlefield the keyword goes onto a copy of the permanent's card, so it is lost when the permanent changes zones (not perpetual); off the battlefield it is granted by card id in any zone (CR 400.7).
- `PerpetuallyGrantStaticEffectToSourceEffect` (Craving of Yeenoghu, Gale): the same card swap, not perpetual. Fairly sure.
- `PerpetuallyBoostSourceEffect`: the graveyard path rewrites the card's printed P/T instead of using the perpetual store, so a later perpetual base-P/T change overrides the boost. Fairly sure.
- Rothga (`PerpetuallyBoostTriggeringCardByPowerEffect`): a face-down creature spell's X is read from the real card, not its face-down characteristics (CR 708.2). Unsure.
- `RemovePerpetualTriggeredAbilityEffect` (Pass the Torch, Racketeer Boss) and `PerpetuallyGrantKeywordsToStationingCreatureEffect` (Monoist Gravliner): the card is found by id in any zone (CR 400.7). Unverified.
- `RevealedHandChoice.bottomThenDrawMode` / `discardThenDrawMode` have no setter left; the components and their branches in `CardChoiceHandlerService.finishRevealedHandChoice` are dead (17 positional constructor calls; step 11). Sure.
- A card with both a perpetual evoke and a plain perpetual alternative cost offers only the first granted one (`GameQueryService.findGrantedAlternateCast`) (CR 601.2b).
- Myr Welder, Territory Forge, Idris, Locus of Enlightenment: their printed exiles record no link, so a card exiled by a borrowed linkless exile ability counts as "exiled with it" (CR 607.2a). Sure of the code.
- A borrowed "X is the mana value of the exiled card" ability (Prototype Portal) is dropped from exile borrowers instead of granted as one that does nothing. Unsure.
- The spore-counter cards (the Thallids, Sporoloth Ancient, Fungal Bloom, Thelon of Havenwood, Propagator Primordium) use `CounterType.FUNGUS`, also the real fungus counters of Sporogenesis and Mindbender Spores, while Thallid Shell-Dweller uses `SPORE`: Thelon counts fungus counters as spore counters and Sporoloth's granted "remove two spore counters" can't use Shell-Dweller's (CR 122.1). Sure of the code.
- Voracious Brood: "whenever one or more creature cards are put into your graveyard from anywhere, put that many +1/+1 counters" is one trigger per card outside the library path, not one per event (CR 603.2c). Fairly sure, minor.
- Clash of the Eikons and Sigurd (lore counters off "target Saga you control"), Etched Host Doombringer and Portent Tracker (defense counters off a battle an opponent protects, which its caster controls): the AI scores a fixed-kind removal from a target by kind (owner's decision 2026-11-25), so these read HARMFUL and the AI prefers an opponent's permanent where the text wants its own. Sure of the code, minor.
- `DistributeCountersAmongTargetsEffectHandler`'s delayed cleanup / end-step removals (Broodlord, Systems Override, The Earth Crystal, Vhal) are `RemoveCountersEffect.fromTarget` on the delayed ability's entry: the delayed ability refers to that permanent, which it still affects while it stays on the battlefield (CR 603.7c), and declares no target, so `Subjects.named(id)` would say it; whether a shroud or hexproof gained meanwhile stops today's removal was not checked. Unverified.
- Pre-pool prevention checks left outside the five damage records: `DamageSupport.isDamageSourcePreventedWithLog` is still asked first by about 28 other damage handlers (mass, divided, each-target, (fixed in defect run D26, with the each-target, mass, each-matching, permanents-target-controls and defending-player handlers' checks and mass's protection in `damageFilteredCreatures`), attacked-target, ...), `isDamagePreventedForCreature` by about 10 (the target-creature record's `triggering()` and `thisWay()` subjects, Dual-Shot-style two-target handlers, the multi-permanent choice completions), and the divided and mass paths check protection themselves; each denies the CR 616.1 order when a second prevention applies and logs its own line. Sure of the code.
- `DealDamageEffectHandler`'s `triggering()` subject (formerly the target-creature handler's) logs its own " deals N damage to " line beside the damage pool's own line for the same damage, so it is logged twice. Sure, presentation only.
- A "this creature or another [X] enters" ability that its own source triggers (Scourge of Valkas, Hawkeye) records no triggering permanent on its entry, so a `Subjects.triggering()` or `TriggeringPermanentPower` read in such an ability finds nothing; the damage carrier now falls back to the source (step 10 run Damage), other readers weren't checked. Fairly sure of the entry, unsure which cards read it.
- The pool's protection rule covers damage a permanent deals itself (fight, bite, combat) only for a recipient with Phantom Centaur's counter prevention (defect run D26 added noncombat damage the dealing entry's own source deals, fight and bite carriers included); whether the fight and bite handlers check protection themselves was not checked (CR 702.16e) (CR 616.1). Unverified.
- Damage `DealDamageEffect` deals to a permanent through `DamageSupport.resolveAnyTargetDamage` doesn't read the entry source's "damage this deals can't be prevented" (`damageCantBePreventedFromSource`), which `dealDamageToPlayer` reads; the dealer path sets it per dealer since run Damage R (CR 615.12). Unverified (the pool may read it elsewhere).
- K'rrik, Son of Yawgmoth and an "unless pays" ransom (`UnlessPaysEffectHandler.canPay`): when the ransom's mana can be paid only with mana not yet made (lands untapped), the 2 life for each {B} is estimated against the current pool, which may overstate the life and leave the payer unasked; the answer itself pays from the pool as tapped. Fairly sure.
- "Unless pays" and "may pay" mana paid from the pool without the tax-paid event (Tax Taker, CR 118.12): (fixed in step 10 run U2, with Divert and ward waterbend's mana), Tariff, Winter's Chill, Forgotten Lore, (fixed in step 10 run U3b2: the Rishadans are `UnlessPaysEffect.eachUnless`; `TaxTakerTest.rishadanCutpursePayment`), `EachCreatureControllerSacrificesPermanentUnlessPays`, (fixed in step 10 run U3b1: Excise and Carrionette are `UnlessPaysEffect`s; `ExciseTest`, `CarrionetteTest` tax tests), `GrantKeywordToBlockingCreaturesUnlessAttackerPays`, `PreventCombatDamageFromAttackingCreaturesUnlessPays`, Reflect, , `EnchantedControllerMayPayToPreventDamage`, (both fixed in step 10 run U3b': `UnlessPaysEffect` pays each payer through `ResolutionCostPaymentService.pay`; `TaxTakerTest.rhysticTutorPayment`), (U3a), two `MultiPermanentChoiceHandlerService` payments. Sure of the code.
- The morph face-up life cost (`SpellCastingService.validateMorphLifeCost`) and the attack / block life taxes (`CombatAttackService`, `CombatBlockService`) check `canPlayerLifeChange`, not `canPlayerLoseLife` (CR 119.8: a player who can't lose life can't pay life). Sure of the code.
- A player `DamageEvent` (`DamageEvent.toPlayer`) carries no can't-be-redirected flag, so the player pool's redirects (Aegis of Honor, Harm's Way, the source-keyed ones) would ignore "can't be redirected"; `resolveAnyTargetDamage` honoured it for Aegis only, and no card sets it for a player today (Lava Burst's applies to a creature). Sure of the code, harmless today.
- Library of Leng's "if an effect causes you to discard a card" offers its top-of-library replacement for a discard paid as a cost (a ward or "unless" discard ransom, `ResolutionCostPaymentService`'s discard prompt); its ruling: "You can't use the Library of Leng ability  (CR 118.12). Sure of the ruling, fairly sure of the code.
- Ward—Waterbend's payability (`WaterbendPaymentService.canPay`) counts the pool and the untapped artifacts and creatures, not mana the payer could still make (CR 118.3, 605.3a), so a payer with untapped lands and an empty pool isn't asked. Sure of the code.
- The Serpent Society's poison ransom puts the counters with the ward's controller as their placer (`LifeSupport.applyPoisonCounters(..., sourceControllerId)`), and Perplex's discard-hand ransom names the payer as its cause: who "puts" a counter or "causes" a discard paid as a cost was kept as it was (Vorinclex, Obstinate Baloth readers), not checked against the rules. Unsure.
- Gix, Yawgmoth Praetor: "whenever a creature deals combat damage to one of your opponents, its controller may pay 1 life. Sure of the Oracle text and the code.
- The hard AI scores an `UnlessPaysEffect` as value(ifPaid) − value(ifNotPaid) for the payer (step 10 run U3a), but `CounterSpellEffect` is valued as the AI's own counterspell, so a ward or "counter unless" asked of the AI about its own spell scores negative and is declined (it scored 0 and was declined before). Sure of the code.
- Timothar, Baron of Bats: when a replacement creates more than one Bat, only the last one is linked to the exiled card (`CreateTokenLinkedToCardsExiledThisWayEffectHandler` gives each exile entry one source); the ruling says every such Bat has the linked ability and any of them returns the card. Sure of the ruling and the code.
- Ice Cave: a Phyrexian or hybrid symbol in the triggering spell's mana cost is paid by the pool payment as any resolution cost; whether 2 life can pay {W/P} there is unchecked. Unsure.
- The sacrifice ransom's pick prompt reads "Choose a Aura attached to Three Dog to sacrifice." (`ResolutionCostPaymentService.pay`, article). Sure.
- Mana abilities that work from the graveyard, exile or the command zone count as mana sources for `couldPossiblyPay` but can't be activated during a may-pay prompt (only hand abilities can, step 10 run U3a); no implemented card has one today. Sure, harmless today.
- Quenchable Fire, Glass Asp, Nafs Asp, Sabertooth Cobra: the special action that pays before the delayed trigger (`AbilityActivationService.payDelayedTriggerCost`, CR 116.2c) raises no tax-paid event, so Tax Taker doesn't see mana an opponent pays for your spell's or creature's delayed ability; it changes play only with Tax Taker and the rules don't say whether it is a "tax". Sure of the code.
- A one-permanent sacrifice ransom (`ResolutionCostPaymentService.pay`, `SacrificePermanentCost`) still asks the forced pick: Ravenous Vampire, Crovax and Indoraptor, which sacrificed a lone candidate unasked before step 10 run U3b1, now ask it (the U3c line above; CR 608.2d convention). Sure.
- The hard AI valued a "that player loses N life / is dealt N damage unless they pay" branch at 0 when its player subject isn't `you()` (`SpellEvaluator`): step 10 run U3b1 reads a branch naming the payer's own subject as the payer's life loss (ransom scale); other branch effects aimed at the payer (discarding the hand, losing permanents) are still valued from the AI's side. Sure of the code.
- Acererak the Archlich's tokens are created as the step after the paid-together sacrifices (`UnlessPaysEffect.payingTogether()` then the branch), not in the same event as the sacrifices (its ruling: "creatures are sacrificed and tokens are created simultaneously"); nothing can be done between them and no trigger sees one without the other. Unsure it is observable.
- `UnlessPaysEffectHandler.nextStep` now sets the entry's accepted players to the payers who chose to pay before every branch (for `eachUnless`); an entry that also had a `TemptingOfferEffect` or Snort's discard set them before its "unless" would lose that list. Sure of the code.
- The hard AI scores an "unless" branch that sacrifices (or destroys, exiles, phases out) its own source as nothing (`SpellEvaluator`), so it never pays an upkeep to keep a permanent (Phantasmal Forces, the Elder Dragons, the lands): score that branch as the source's value (AI only; `UpkeepRansomAiTest` pins today's parity).
- Several permanents returned for one ransom (Bull Elephant's two Forests, Ovinomancer's three lands) leave one by one inside the one instruction through `PermanentRemovalService.removePermanentToHand`, not as one departure batch, so a leaves-the-battlefield look-back (CR 603.10a) of one wouldn't see the others leave; no ability resolves between them and no card in the pool observes it (the same as the old `returnPlayerMatchingPermanents`). Unsure it is observable.
- Behemoth of Vault 0: "when you do, destroy that permanent" is a reflexive ability about the declared target, which `QueueReflexiveAbilityEffect` would target again (needs a reflexive ability about an earlier target) (CR 118.3).
- The hard AI asked to pay for one object of an `UnlessPaysEffect.forEach` scores the branch effect as a whole (`SpellEvaluator.evaluateAbilityEffect`), not the object asked about (its own Spirit, land or creature). Unsure.
- Sword-Point Diplomacy asks about the revealed cards in library order; its ruling lets the caster choose the order the cards are processed in (it matters only when an opponent's life runs short). Unsure it is worth a prompt.
- Decoy Gambit (`ReturnTargetCreaturesUnlessControllersDrawEffect`) still offers "have you draw a card" when its caster can't draw; whether an opponent may choose it then (CR 608.2d: an impossible option can't be chosen; CR 121.3 bars a player who can't draw from choosing to draw, but here another player makes the choice) is not settled by the rules or its rulings. Sure the question is open.
- Varchild's War-Riders and Wall of Shards pay their cumulative upkeep to `getOpponentId` (the first opponent); their rulings let each age counter's token or life go to any opponent, chosen separately. Sure of the rulings and the code.
- Missing feature (owner): no client chooser for an "[A] or [B]" additional cast cost (`AnyOfCost` as `addCastingCost`) (CR 601.2b).
- The free-cast additional-cost chooser ("Choose how to pay the additional cost.") is asked after the targets, not as the spell is announced (CR 601.2b, before 601.2c).
- A spell played from a graveyard without paying its mana cost (`MayCastHandlerService.handlePlayFromGraveyardChoice`: Horde of Notions and the other users) pays no additional cast cost: it refuses a discard-or-sacrifice one (Bone Shards' shape) and casts Bitter Triumph, Final Payment and Dusk Mangler with theirs skipped (CR 601.2f, 601.2h). Sure of the code.
- A library-top cast of Bitter Triumph with cards in hand is refused: the library-top request carries no discard choice, so only the empty-hand life payment is accepted (CR 601.2b). Sure of the code.
- An exile cast of a "discard a card or pay {m}" spell (Lightning Axe, Pumpkin Bombardment, Titania) can't choose the discard: no discard index reaches the exile path (`playCardFromExile` takes only permanent ids), so it always pays the mana (CR 601.2b). Sure.
- A free cast (`ExileFreeCastQueueSupport`) doesn't offer an "[A] or [B]" cost's graveyard-exile or forage alternative: there is no free-cast graveyard picker, so a free Soaring Stoneglider or Feed the Cycle pays its mana alternative or is skipped (CR 601.2b). Sure.
- Exile and library-top casts still refuse tap-, blight- and graveyard-exile-or-mana spells (Disruption Protocol, Wild Unraveling, Soaring Stoneglider) as "not supported from this zone"; library top takes only sacrifice-or-mana. Sure.
- `BoostEffect.targetPermanent` (Vehicle boosts: Fang Guardian, Captain America's Motorcycle, Blacksmith's Skill, Tributary Vaulter) declares a harmful target spec whatever the sign of the boost, so the AI's target prior treats a pump of its own creature or Vehicle as an attack; derive the prior from the sign as `BoostEffect.target` does.
- Battle cry is built on "creatures you control" (B2 keeps it), but CR 702.91a gives +1/+0 to "each other attacking creature", a teammate's attackers too (Two-Headed Giant).
- Text-changing effects (CR 612.2) reach a source-reading boost's amounts and target filter only: not a self or named-permanent boost's amounts ("+1/+1 for each black creature"), not `targetPermanent`'s filter (Buccaneer's Bravado's "target Pirate"), not the mass boosts' set filters.
- `TriggeredAbilityQueueService.triggerOrderLabel` names a source-reading boost with no target "target creature gets +N/+M" (Sigardian Zealot, Mirkwood Trapper's second trigger, Zenos yae Galvus); a label only.
- `BoostEffectHandler`'s refresh of the boosted permanent's waiting abilities' snapshots changes nothing observable: a departing permanent refreshes every waiting entry's snapshot itself (`PermanentRemovalService`), and a live source is read live. Sure.
- "Attacking creatures get" / "other attacking creatures get" written as `BoostEffect.creaturesYouControl(…, attacking)` (Iroh, Firebending Instructor, Kangee's Lieutenant, Motivated Pony, Targ Nar, Honored Crop-Captain and others) leaves out a teammate's attackers (Two-Headed Giant), as battle cry does (Q7); `allCreatures(…, attacking)` would say it.
- The hard AI gives set damage no value: `DealDamageEffect.toEach` / `toEachTarget` answer `canDamageCreatures()` and `canDamagePlayers()` false (as the absorbed records were no `DamageDealingEffect`), so Simoon scores the same with or without an opposing creature (`SpellEvaluatorTest.setDamageSweeperIsNotScoredAsTargetedDamage` pins it) and no sweeper branch reads them; M2's `BoardWipeEffect` view could cover `toEach`. Sure.
- `CloneService.exileCopiedCardAbility` (Superior Spider-Man, Activated Sleeper, The Mimeoplasm and the other "enter as a copy of a creature card in a graveyard; when you do, exile that card" cards) names the copied card by `setTriggeringCardId` with no graveyard stay, so a card that left the graveyard and came back before the reflexive ability resolves is still exiled; it is a new object (CR 400.7). Sure of the code, not tested.
- `ActivatedAbilityExecutionService`'s inline mana-ability branch for `DrawCardEffect` / `ManaAbilityCardDrawingEffect` draws for the activating player only, ignoring `player()`; latent (an ability with a card draw is never a mana ability here, `AbilityActivationService.movesCardToOrFromLibrary`, CR 605.1a), so likely dead code to delete. Sure of the code.
- `EachOtherPlayerDrawsCardEffectHandler` (the 11 gift cards) draws in seating order from the first seat, not in APNAP order (CR 101.4); fold with the gift opponent. Sure of the code.
- The hard AI prices "each opponent draws" (`SpellEvaluator.drawScore`) as one opponent's cards, not one set per opponent in a multiplayer game. Sure.
- `TargetPolarityClassifier` has no entry for `ChooseOneForTargetCreatureEffect` and reads `ChooseOneForTargetPermanentEffect` as NEUTRAL, though some modes harm the target (Urborg, Walking Sponge, one of Shaper Parasite's): the AI's targeting for these 15 cards ignores the modes. Sure of the code, not of the AI's resulting choices.
- `TheGrimCaptainsLockerTest.doesNotGrantEscapeToNonCreatureCards` asserts a Grizzly Bears in a graveyard that holds only a Shock and never tries to cast the Shock with escape, so it doesn't test what its name says. Sure.
- Rikku, Resourceful Guardian: "until end of turn, that creature can't be blocked by creatures your opponents control" floats a granted `CantBeBlockedByCreaturesMatchingPredicateEffect` ability (`MakeCreatureUnblockableEffect.byCreaturesYourOpponentsControl()`), so a later "loses all abilities" ends it, though the effect gives no ability (CR 611.2a, 613.11). Sure of the code and the rule.
- Sarulf, Realm Eater: its exile (`ExileOtherNonlandPermanentsWithManaValueAtMostEffectHandler`) moves each permanent with its own `removePermanentToExile`, not as one leave batch, so "whenever one or more ... Sure of the code.
- `ExileTargetPermanentEffectHandler`: a plain `target()` over several targets ("exile up to two target creatures") exiles them one by one with no leave batch, and a non-set several-permanent subject (`allOf(source(), target())`, `allTargets()`) gets the leave batch but not the one rules-event occurrence a set has since run K2 (CR 603.2c, 603.10a: one instruction, one event). Sure of the code.
- The AI scores "destroy all [X] target player controls" (`DestroyTargetPermanentEffect.allTargetPlayerControls`) over both battlefields, its own included (`SpellEvaluator.evaluateDestroyAllValue`, parity with the old wipe record). Sure.

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
