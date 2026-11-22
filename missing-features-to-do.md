# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 67. Ability copies go on the stack before their new targets are chosen

**Status:** open · **Found:** 2026-10-28, copy-spell staging fix (C batch)

**Failing tests:**
- None yet. Add one per retarget handler: while the "choose new targets" prompt for a copied activated or
  triggered ability is pending, the copy must not be on `gd.stack`.

**What the rules require.** CR 707.10c: "Once the player has decided what the copy's targets will be, the
copy is put onto the stack with those targets." This applies to copies of abilities as well as spells. Spell
copies now wait in `GameData.stagedSpellCopies` until the decision is made
(`CopySupport.addCopyToStack(..., retargetPrompt)` and `commitDecidedStagedCopies`, called from the
`InputCompletionService` epilogue). Ability copies still go straight onto the stack first and are then
retargeted there: `CopyAbilityRetargetHandler`, `CopyActivatedAbilityRetargetHandler`,
`CopyTriggeredAbilityRetargetHandler`, the ability path of `CopyTargetSpellOrAbilityEffectHandler`, and the
ability per-target (psychic-battle) prompts. Becomes-target triggers such as ward (CR 702.21a) can
therefore fire for the copy's original targets even when it is retargeted.

**Why it's large.** It spans several copy handlers (Rings of Brighthearth, Strionic Resonator, Lithoform
Engine, Kurkesh and similar) and their retarget handlers. Each retarget lookup must find staged ability
copies, and many existing tests assert the copy on the stack while its prompt is pending (the spell fix
changed about 45 such assertions).

**Prompt to start it:**

```
In magical-vibes, extend the spell-copy staging from CopySupport (stagedSpellCopies,
addCopyToStack with a retargetPrompt, commitDecidedStagedCopies) to copies of activated and triggered
abilities, so an ability copy enters the stack only after its controller decides its targets (CR 707.10c;
verify with get_rule). Cover CopyAbilityRetargetHandler, CopyActivatedAbilityRetargetHandler,
CopyTriggeredAbilityRetargetHandler, the ability path of CopyTargetSpellOrAbilityEffectHandler and the
ability psychic-battle prompts. Commit triggers (ability-copy triggers, becomes-target triggers for the
final targets only) must fire when the copy enters the stack. Add tests (copy absent from the stack while
the prompt is pending; ward on a retargeted-away original target doesn't trigger), update tests that read
the copy from the stack mid-prompt, and run the copy-card regression tests one class at a time.
```

## 68. Triggers from a committed copy are placed between copies created together

**Status:** open · **Found:** 2026-10-28, copy-spell staging fix (C batch)

**Failing tests:**
- None yet. Add one with storm (or another multi-copy effect) plus a "whenever you copy a spell" or ward
  trigger, checking that all copies are on the stack before any of those triggers.

**What the rules require.** CR 603.3: a triggered ability is put on the stack "the next time a player would
receive priority." When one effect creates several copies, no player receives priority between them, so
every copy should be on the stack before any trigger they caused, and those triggers then go on top, in
APNAP order (CR 603.3b). `CopySupport.commitDecidedStagedCopies` (and the immediate `addCopyToStack` path)
calls `checkSpellCopyTriggers` and `checkBecomesTargetOfSpellTriggers` per copy. Any trigger those checks
put onto the stack right away can end up between copies. The pre-staging code did the same per copy.

**Why it's large.** The fix is to queue those triggers as pending until the batch of copies is committed,
then stack them together. That touches how the copy and becomes-target trigger collectors push entries,
and those collectors are shared with ordinary spell casting.

**Prompt to start it:**

```
In magical-vibes, make the triggers caused by copying spells (checkSpellCopyTriggers and
checkBecomesTargetOfSpellTriggers called from CopySupport.addCopyToStack and commitDecidedStagedCopies)
wait until all copies made by the same effect are on the stack, then go on the stack together in APNAP
order (CR 603.3 / 603.3b; verify with get_rule). First write a failing test (e.g. Grapeshot or another
storm card with a "whenever you copy a spell" permanent or a ward target) showing a trigger landing
between copies. Keep ordinary cast-time trigger behavior unchanged; run the copy-card and storm
regression tests one class at a time.
```

---

## 69. The frontend can't activate an "any player may activate" ability on another player's permanent

**Status:** open (backend done 2026-10-30) · **Found:** 2026-10-28, F-card test batch

**What the rules require.** Flailing Soldier and similar permanents (Oona's Prowler and others flagged
`ActivatedAbility.isActivatableByAnyPlayer()`) let any player activate the ability. The backend now accepts an
optional `sourcePermanentId` on `ActivateAbilityRequest` (resolved through `GameService.activateAbility`'s
source-ID overload), so the index ambiguity is gone server-side; the harness uses `activateAbilityOfPermanent`.
The Angular app still only offers activations on the player's own battlefield (`targeting-choice.service.ts`
reads `myBattlefieldFn()`), and the permanent view doesn't expose `activatableByAnyPlayer`.

**Why it's large.** The permanent view needs the flag, opponent permanents need click handling and an
ability menu, and the request has to carry `sourcePermanentId`.

**Prompt to start it:**

```
In magical-vibes, let the Angular frontend activate "any player may activate" abilities (ActivatedAbility
.isActivatableByAnyPlayer()) on permanents the player doesn't control: expose the flag in the permanent view,
offer those abilities when clicking an opponent's permanent, and send ActivateAbilityRequest.sourcePermanentId.
Read agent-docs/ARCHITECTURE.md first (view immutability, networking).
```

---

## 70. Snowfall's "may add" mana needs an optional choice inside a triggered mana ability

**Status:** open · **Found:** 2026-10-30, O/P mixed card-test batch

**Disabled tests:**
- `SnowfallTest` "The Island's controller may decline Snowfall's additional mana"

**What the rules require.** Snowfall's Oracle text says that whenever an Island is tapped for mana, its
controller *may* add an additional {U} (and another if the Island is snow). That is a triggered mana ability
(CR 605.1b): it resolves immediately, without the stack, but the "may" is still a choice for the Island's
controller. `LandTapTriggerCollectorService.handleAddRestrictedManaWhenSubtypeLandTapped` adds the mana with no
choice at all.

**Why it's large.** The engine has no way to ask a question in the middle of a mana ability, because mana
abilities resolve synchronously inside cost payment and auto-tapping. It needs a pending optional-choice path
for triggered mana abilities that also works while a cost is being paid. The other `SnowfallTest` tests
(`opponentIslandAlsoBenefits`, `islandAddsRestrictedBlue`, `snowIslandAddsTwoRestrictedBlue`,
`islandManaPaysCumulativeUpkeep`, `multipleSnowfallsAddSeparateSnowBonuses`, `insufficientManaSacrificesSnowfall`)
assume the mana is added with no prompt. Once the prompt exists they have to answer it, or the engine has to
add the mana by default and only ask when it matters.

**Prompt to start it:**

```
In magical-vibes, support optional choices in triggered mana abilities (CR 605.1b), starting with Snowfall's
"that player may add an additional {U}". LandTapTriggerCollectorService.handleAddRestrictedManaWhenSubtypeLandTapped
currently adds the mana unconditionally. Design how a mana ability can ask its controller a yes/no question,
including while mana is being tapped to pay a cost, then make SnowfallTest "The Island's controller may decline
Snowfall's additional mana" pass and update the other SnowfallTest flows to answer the new prompt. Read
agent-docs/ARCHITECTURE.md first and verify every CR number with the rules MCP.
```

---

## 71. Fluttershy's "creature with a tail" depends on card art

**Status:** open · **Found:** 2026-10-30, mixed card-test batch

**Disabled tests:**
- `FluttershyTest` "Only creatures with tails receive counters"

**What the rules require.** Fluttershy (`SLD` 1538) reads "Put a +1/+1 counter on each creature with a tail
target player controls." Whether a creature has a tail is decided by looking at its art, as with the
Unfinity-style physical-characteristic cards. Scryfall has no ruling and the card data has no field for it. The
engine currently puts a counter on every creature the target player controls.

**Why it's large.** It needs a way to answer art-based questions: either per-card data (a curated "has a
tail" flag, a guess for thousands of cards) or a prompt that lets the players decide each creature. Neither
exists.

**Prompt to start it:**

```
In magical-vibes, decide how art-based characteristics such as Fluttershy's "creature with a tail" should be
answered (curated per-card data or a player prompt), implement it, restrict Fluttershy's counter effect to
creatures with a tail, and re-enable FluttershyTest "Only creatures with tails receive counters". Read
agent-docs/ARCHITECTURE.md first.
```

---

## 72. The AI's must-attack-alongside hint ignores attacker-count caps

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Failing tests:**
- None yet. Add an AI test: Silent Arbiter (or another "no more than one creature can attack") with a conditional
  "attacks if another creature attacks" requirement; the AI's declaration must be legal.

**What the rules require.** CR 508.1d: the declaration must obey the maximum number of requirements that can be obeyed
without disobeying a restriction. The engine's legality check searches for that maximum (`CombatAttackService`), but
`CombatAttackService.getMustAttackAlongsideIndices`, which `AiDecisionEngine` uses to add required attackers, adds every
creature a conditional requirement names without checking attacker caps, so the AI can propose an illegal declaration.

**Why it's large.** The hint has to reuse the requirement search's restriction model (caps, taxes, alone/power
restrictions) rather than a list of indices.

**Prompt to start it:**

```
Do missing-features-to-do.md entry 72: make CombatAttackService.getMustAttackAlongsideIndices (used by
AiDecisionEngine) respect attacker-count caps and the other restrictions the requirement search obeys (CR 508.1d,
verify with get_rule). Add an AI test with Silent Arbiter and a conditional requirement. Don't commit until I say so.
```

---

## 73. Attack and block requirement searches give up after 200,000 declarations

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Failing tests:**
- None. A board large enough to exhaust the budget is needed to show it.

**What the rules require.** CR 508.1d and 509.1c: a declaration is illegal if it obeys fewer requirements than the
maximum possible. `CombatAttackService` and `CombatBlockService` find that maximum by search
(`REQUIREMENT_SEARCH_BUDGET = 200_000`); past the budget they settle for the best found so far, so on a huge board a
declaration obeying fewer than the true maximum can be accepted.

**Why it's large.** An exact answer needs a better algorithm (pruning, a matching formulation for block requirements)
whose cost stays bounded for AI simulations.

**Prompt to start it:**

```
Do missing-features-to-do.md entry 73: replace the budgeted requirement searches in CombatAttackService and
CombatBlockService (REQUIREMENT_SEARCH_BUDGET) with exact maximum-requirement computations that stay fast (CR 508.1d,
509.1c; verify with get_rule). Benchmark against the current searches and keep the existing requirement tests green.
Don't commit until I say so.
```

---

## 74. Underbridge Warlock: is its one-time boon spent as it triggers or as it resolves?

**Status:** open · **Found:** 2026-11-19, closing the durations refactor

**Failing tests:**
- None; this is an open rules question.

**What the rules require.** Unsettled: Underbridge Warlock's boon ("at the beginning of your end step, if you have a
boon, …") has no official ruling on when a one-time boon is used up. Other boons are spent as they trigger
(`GameData.useDelayedAbility`); this one is spent as it resolves (`CreateBoonEffect.usedAsItResolves`,
`ConsumeBoonEffect`), so its "if you have a boon" rider still sees it, which keeps the engine's earlier behaviour.

**Why it's large.** Not large; it needs a ruling (Scryfall `get_rulings`, then the web) before any change.

**Prompt to start it:**

```
Do missing-features-to-do.md entry 74: check Underbridge Warlock's rulings with get_rulings and search the web for how
a one-time boon is used up. If spent as it triggers, drop usedAsItResolves/ConsumeBoonEffect for it and update its
tests; otherwise remove this entry. Don't commit until I say so.
```

---

## 75. Exiling a resolving spell: no CR 616.1 choice beyond two source-tracked exiles

**Status:** open · **Found:** 2026-11-19, Triggering-class audit

**Failing tests:**
- None yet. Add one: a dream-counter (or plot / suspend / Feather-style return) exile and a source-tracked exile
  (Rod of Absorption) both apply to one instant; its controller must choose which.

**What the rules require.** CR 616.1: when two or more replacement effects would modify the same event, the affected
object's controller chooses one to apply. `ExileTriggeringSpellEffectHandler` offers that choice only between two
`TRACKED_BY_SOURCE` exiles (`StackEntry.competingExileSourcePermanentIds`). Any other pair is settled by marking order:
the handler returns early once the spell has an `ExileSpellEffect`, while `DREAM_COUNTER` / `PLOTTED` go to
`GameData.spellsWithDreamCounterOnResolution` / `spellsWithPlotOnResolution` and `RETURN_TO_HAND_AT_NEXT_END_STEP` to a
`StackEntry` flag without checking the others.

**Why it's large.** Every `TriggeringSpellExile` kind needs one competing-replacement record and one choice point where
the spell resolves.

**Prompt to start it:**

```
Do missing-features-to-do.md entry 75: generalize StackEntry.competingExileSourcePermanentIds into a record of every
TriggeringSpellExile replacement that applies to a resolving instant/sorcery, and let its controller choose one as it
resolves (CR 616.1, verify with get_rule). Read AGENTS.md and agent-docs/ARCHITECTURE.md first. Add tests for each kind
competing with a source-tracked exile. Run only touched test classes by FQCN. Don't commit until I say so.
```

---

## 76. Swindler's Scheme compares a face-down spell's real card types

**Status:** open · **Found:** 2026-11-19, Triggering-class audit

**Failing tests:**
- None yet. Add one: an opponent casts a face-down creature spell from hand and Swindler's Scheme reveals a noncreature
  card of one of its real types; the spell must not be countered (and a revealed creature card must counter it).

**What the rules require.** CR 708.4: effects that care about a face-down spell's characteristics see only the face-down
characteristics (a 2/2 creature with no name or mana cost). In
`RevealTopCardAndCounterTriggeringSpellIfMatchesEffectHandler.matches`, the `SAME_MANA_VALUE` branch treats a
face-down spell as mana value 0, but the `SHARES_CARD_TYPE` branch reads `spellCard.getType()` /
`getAdditionalTypes()` of the real card.

**Why it's large.** Not large: one branch and a test.

**Prompt to start it:**

```
Do missing-features-to-do.md entry 76: make the SHARES_CARD_TYPE branch of
RevealTopCardAndCounterTriggeringSpellIfMatchesEffectHandler use {CREATURE} for a face-down spell
(StackEntry.isCastFaceDown()), per CR 708.4 (verify with get_rule). Add Swindler's Scheme tests. Don't commit until I
say so.
```

---

## 77. "Cast from your hand" triggers match a spell cast from another player's hand

**Status:** open · **Found:** 2026-11-19, Triggering-class audit

**Failing tests:**
- None yet. Add one: under Sen Triplets, its controller casts an instant from an opponent's hand while controlling
  Goliath Daydreamer; Goliath must not trigger.

**What the rules require.** Goliath Daydreamer ("from your hand"), Lilah, Undefeated Slickshot and Gandalf of the Secret
Fire trigger only for spells cast from their controller's own hand; Swindler's Scheme ("from their hand") only for the
caster's own hand. `StackEntryCastFromZonePredicate` compares only `StackEntry.getSourceZone()`
(`PredicateEvaluationService.matchesStackEntryPredicate`, `TargetLegalityService`), and a Sen Triplets cast through
`SpellCastingService.playFromHandOf` records `Zone.HAND` (`GameData.castingSourceZone`), so the spell matches.

**Why it's large.** Not large: record that the spell came from another player's hand (or compare the card's owner with
the caster, CR 400.3 puts a card only in its owner's hand) and teach the predicate.

**Prompt to start it:**

```
Do missing-features-to-do.md entry 77: make StackEntryCastFromZonePredicate(HAND) (or a variant) match only spells cast
from the caster's own hand, so a Sen Triplets cast from an opponent's hand doesn't fire Goliath Daydreamer, Lilah,
Gandalf of the Secret Fire or Swindler's Scheme. Verify every CR number with get_rule. Add tests with Sen Triplets.
Don't commit until I say so.
```

---

## 79. A card's id survives zone changes, so an opponent can recognize a card cast face down

**Status:** open · **Found:** 2026-11-22, face-down stack leak fix (`b6fa735b07`), by reading the code

**Failing tests:**
- None yet. Add one: an opponent is sent a card face up (revealed from a hand, or returned from the battlefield to a hand),
  it's then cast face down, and no id in what that opponent is sent afterwards matches an id sent with the card's name.
  `FaceDownStackIdentityTest.differentMorphsLookTheSame` leaves the ids out of its comparison today.

**What the rules require.** CR 708.5: a player can't look at face-down spells or permanents another player controls.
CR 400.7: an object that changes zones becomes a new object with no relation to its previous existence, and CR 701.20d
says the same of revealed library cards that are reordered. CR 708.6 still needs face-down objects to be told apart
while they stay where they are. The engine keeps one `Card.getId()` for the card's whole game and sends it to every
player: `StackEntryViewFactory.createFaceDownSpell` uses `getTargetableId()` (the card's id, for a spell) and
`PermanentViewFactory` passes `permanent.getCard().getId()` to `CardViewFactory.createFaceDown`. The masked views hide
the name, but a client that remembers the id from when the card was public or revealed knows which card it is.

**Why it's large.** It needs opaque ids across the protocol: the engine keys zones, targets, tracking maps and prompts by
the card's id, the networking views, the game log's card segments and the prompts send it, and the client sends it back
in answers and uses it to follow cards. An id that changes as a card becomes hidden has to be translated in both
directions, per game, and survive reconnects, and any path that still sends the engine's id reopens the leak.

**Prompt to start it:**

```
Do missing-features-to-do.md entry 79: stop clients from linking a hidden or face-down card to an earlier sighting by
its id. First read agent-docs/ARCHITECTURE.md, agent-docs/architecture/casting-stack.md (the face-down section),
FaceDownVisibility and FaceDownStackIdentityTest, and verify CR 400.7, 701.20d, 708.5, 708.6 and 708.9 with get_rule.
Then inventory every card id that leaves the server (views, log segments, prompts, events, reconnect) and every id the
client sends back, including hands, libraries and cards exiled face down, and propose a design: prefer opaque ids at
the protocol boundary that change when a card becomes hidden and stay stable while it stays put, over changing
Card.getId() in the engine. Show me the design and wait for my approval before implementing it, since it changes the
protocol and the frontend. Then start with a failing test. Don't commit until I say so.
```

---

<!--
Template — copy everything between the lines, give it the next number, and append above.

## N. <Short title>

**Status:** open · **Found:** YYYY-MM-DD, <context>

**Failing tests:**
- `<TestClass>.<test>()` (or the display name)

**What the rules require.** <Verified CR text / rulings, and what the engine does instead.>

**Why it's large.** <Scope and regression risk.>

**Prompt to start it:**

```
<A self-contained prompt that can be pasted to kick off the feature.>
```
-->
