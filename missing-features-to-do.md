# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---


## 5. Power and toughness larger than a 32-bit int

**Status:** open · **Found:** 2026-10-12, while fixing E-card tests

**Failing tests:**
- `ExponentialGrowthTest.largeXDoesNotWrapPositivePowerIntoNegativePower()`

**What the rules require.** The rules don't cap power (CR 107.1b only covers negative values). Exponential Growth doubles power
X times, so it can exceed 2,147,483,647. Power, toughness and their modifiers are `int` throughout
the model and layer system, so `DoubleTargetCreaturePowerEffectHandler` wraps to a negative number.

**Why it's large.** It's a type migration across `Permanent`, `CharacteristicState`, the layer system, damage, combat
and the networking/frontend views, or else a rules decision to saturate at a maximum value.

**Prompt to start it:**

```
Stop large power and toughness values from overflowing (Exponential Growth doubling past
Integer.MAX_VALUE). Decide with me whether to move P/T to long or saturate at a cap, then implement it
across the model, layer system, damage and views. Start with ExponentialGrowthTest, then ask me to run
the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 21. Undiagnosed G-card failures

**Status:** open · **Found:** 2026-10-14, while fixing G-card tests

These G-card test classes still fail after two Haiku rounds and a Sonnet pass. No one found a single
shared root cause. Most are one-card engine gaps (wrong timing of a choice, a missing replacement or
trigger condition, a lost-ability or LKI case) rather than test setup. Each needs its own investigation.

**Remaining:** `GhoulishProcessionTest`, `GilraenDNedainProtectorTest`, `GisaGloriousResurrectorTest`, `GlimpseOfTomorrowTest`, `GlimpseTheUnthinkablePlaytestTest`, `GlintWeaverTest`, `GlitchInterpreterTest`, `GloomshriekerTest`, `GloomSurgeonTest`, `GoblinArsonistTest`, `GoblinBarrageTest`, `GoblinCrashPilotTest`, `GoblinDarkDwellersTest`, `GoblinFlectomancerTest`, `GoblinFlotillaTest`, `GoblinGrapplerTest`, `GoblinKitesTest`, `GoblinSappersTest`, `GoblinTestPilotTest`, `GoblinTinkererTest`, `GoblinTrapfinderTest`, `GoblinWarBuggyTest`, `GoddricCloakedRevelerTest`, `GodEternalKefnetTest`, `GodEternalOketraTest`, `GodosIrregularsTest`, `GogoMasterOfMimicryTest`, `GogoMysteriousMimeTest`, `GoldenArgosyTest`, `GoldlustTriadTest`, `GoldmeadowTest`, `GoldveinPickTest`, `GoliathDaydreamerTest`, `GoNinjaGoTest`, `GontiLordOfLuxuryTest`, `GontisAetherHeartTest`, `GorillaWarCryTest`, `GoShintaiOfLostWisdomTest`, `GossipsTalentTest`, `GraazUnstoppableJuggernautTest`, `GracefulAdeptTest`, `GrafRatsTest`, `GraftedGrowthTest`, `GrahaTiaScionRebornTest`, `GrapplingHookTest`, `GraspingShadowsTest`, `GraspOfTheHieromancerTest`, `GraveExpectationsTest`, `GravenArchfiendTest`, `GravePactTest`, `GraveResearcherReanimateTest`, `GravitationalShiftTest`, `GreasefangOkibaBossTest`, `GreatbowDoyenTest`, `GreatFangChroniclersTest`, `GreatGildedBoatTest`, `GreatOakGuardianTest`, `GreatTrainHeistTest`, `GreenGoblinBackForMoreTest`, `GreenSunsTwilightTest`, `GreenSunsZenithTest`, `GreenwardenOfMurasaTest`, `GremlinInfestationTest`, `GriefTest`, `GrimeGorgerTest`, `GrimHaruspexTest`, `GrimoireOfTheDeadTest`, `GrimoireThiefTest`, `GrimReaperLethalLegionnaireTest`, `GrimReapersSprintTest`, `GristleGluttonTest`, `GrixisSojournersTest`, `GrizzledHuntmasterTest`, `GrotagNightRunnerTest`, `GroveOfTheDreampodsTest`, `GrubStoriedMatriarchTest`, `GruesomeDiscoveryTest`, `GruesomeEncoreTest`, `GruesomeSlaughterTest`, `GuiltyConscienceTest`, `GunnerConscriptTest`, `GusthasScepterTest`, `GutFanaticalPriestessTest`, `GutterSkulkerTest`, `GuyInTheChairTest`, `GwafaHazidProfiteerTest`, `GwenomRemorselessTest`, `GwenStacyTest`.

**Diagnosed, need engine work:**
- `GilraenDNedainProtectorTest` — the card wraps its whole flicker in a `MayEffect`, so "return it?" is
  asked before the exile. Oracle exiles first, then "you may return that card…; if you don't, … at the
  next end step". `FlickerEffect` has no exile-then-may-return-else-delayed mode.
- `GutFanaticalPriestessTest.whiteCopiesHaveOneSharedDelayedSacrificeTrigger` — "create two tokens …
  Sacrifice them at the beginning of your next end step" is one delayed trigger; the token-copy
  effect registers one per token.
- `GreenSunsTwilightTest` — at X ≥ 5 the hand/battlefield choice is made after the reveal and the
  card choice; the card uses a cast-time `ChooseOneEffect`. The reveal effect needs a
  "choose the destination after choosing cards" option.
- `GlintWeaverTest.choosesTargetsWhenTriggerGoesOnStack` — the same gap as entry 22: an ETB
  "distribute counters among targets" trigger that wasn't cast goes on the stack with no targets or
  division instead of asking for them.

**What the rules require.** It differs per card. Verify the Oracle text and rulings for each one before
changing anything. Several of these tests already had wrong agent "fixes" reverted, so read the whole
test class first.

**Why it's large.** About 88 classes, each with its own cause.

**Prompt to start it:**

```
Work through the classes in missing-features-to-do.md entry 21 a few at a time: run them, find each root
cause, and fix the engine or card (never weaken an assertion without an official ruling or verified CR
text). Don't run the full test suite; ask me.
Don't commit until I say so.
```

---

## 27. Escalate on spells cast without paying their mana cost

**Status:** open · **Found:** 2026-10-16, while fixing H-card tests

**Failing tests:**
- `HazoretsUndyingFuryTest$ExileAndCast.mayPayEscalateToChooseAnotherModeOfAFreeSpell()`

**What the rules require.** Escalate is an additional cost, so it can still be paid when a spell is cast
without paying its mana cost (Hazoret's Undying Fury ruling 2017-07-14: "You can, however, pay additional
costs"). `ExileFreeCastQueueSupport.castNextFromQueue` caps an escalate spell at one mode
(`maximumChoices = min(1, …)`), so the caster can never choose more modes and pay for them.

**Why it's large.** The free-cast queue has no interactive payment for a variable additional cost.
Choosing N modes has to be followed by paying N−1 escalate costs (discard, sacrifice, tap or mana) as
part of 601.2h, after targets are chosen. The target step (`ExileCastSpellTarget`) currently puts the
spell straight on the stack, so it would need a cost-payment step in between.

**Prompt to start it:**

```
Let spells cast without paying their mana cost from the free-cast queue (ExileFreeCastQueueSupport) choose
extra escalate modes and pay the escalate cost for each (Collective Brutality via Hazoret's Undying Fury).
Pay the cost after targets are chosen (CR 601.2h). Start with HazoretsUndyingFuryTest
mayPayEscalateToChooseAnotherModeOfAFreeSpell. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 29. Powerstone and Thran Turbine mana for resolution-time costs

**Status:** open · **Found:** 2026-10-16, while adding Hydraulic Helper's restriction

**Failing tests:**
- None yet. Add one per card: Powerstone mana paying Mana Leak's {3}, and Thran Turbine mana paying it.

**What the rules require.** A Powerstone token's mana "can't be spent to cast a nonartifact spell" (Karn
and Jetfire make the same mana), and Thran Turbine's mana "can't be spent to cast spells." Neither
restriction covers a cost paid while something resolves, so both kinds of mana can pay Mana Leak's {3},
Rhystic Study's tax and similar costs. Hydraulic Helper's mana already can, through
`ManaCost.canPayResolutionCost`/`payResolutionCost`, which promote only
`ManaPool.promoteNonartifactSpellProhibitedMana()`. Powerstone mana sits in its own colorless bucket
(`powerstoneOnlyColorless`), and Thran Turbine's mana is tagged `spellProhibitedMana` inside the
ability-only bucket (`ManaRestriction.SpellsProhibited`). Neither is promoted for a resolution-time cost.

**Why it's large.** Less than it was: `ManaPool.promoteSpecialActionMana()`/
`restorePromotedSpecialActionMana()` already promote exactly these three kinds of mana for special
actions, keeping Powerstone's persistent subset (Karn's mana lasts until end of turn) right. Switching
`canPayResolutionCost`/`payResolutionCost` to it may be enough, but every "unless pays" / "may pay"
handler that calls them needs a regression check, and plain ability-only mana must keep refusing them.

**Prompt to start it:**

```
Let Powerstone mana (ManaRestriction.Powerstone, including Karn's persistent mana) and Thran Turbine's
"can't be spent to cast spells" mana pay resolution-time costs like Mana Leak's, through
ManaCost.canPayResolutionCost/payResolutionCost. Keep "spend this mana only to activate abilities" mana
(ManaRestriction.Abilities) unable to pay them. Start by adding Mana Leak tests to ThranTurbineTest and a
Powerstone card's test. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 30. Casting an Adventure from the top of the library

**Status:** open · **Found:** 2026-10-16, while fixing J-card tests

**Failing tests:**
- `JohannApprenticeSorcererTest.adventureOnTopIsOfferedAsPlayableUsingItsInstantCharacteristics()`

**What the rules require.** Johann lets you cast "an instant or sorcery spell from the top of your
library." CR 715.3a: when an adventurer card is cast as an Adventure, only the alternative
characteristics are evaluated to see if it can be cast. So an adventurer card whose Adventure is an
instant or sorcery (Questing Druid's Seek the Beast) can be cast as that Adventure from the top of the
library. The engine only looks at the front face: `CastingPermissionService.matchesTopLibraryPermission`
and `canCastFromTopOfLibrary` check the creature's types, and `SpellCastingService.playCardFromLibraryTop`
has no Adventure path.

**Why it's large.** Library-top casting needs a whole Adventure path: the permission check and mana cost
against the Adventure face, a cast-as-Adventure flag on `playCardFromLibraryTop` (mirroring the hand
Adventure cast, including exile-on-resolution and the once-per-turn library-top bookkeeping), the flag
threaded through the networking message and the test harness's library-top cast helpers, and
`GameViewProjectionFactory.getPlayableLibraryTopCards` offering the Adventure.

**Prompt to start it:**

```
Support casting an adventurer card as its Adventure from the top of the library when a permission
allows instant/sorcery (or matching) spells from there (Johann, Apprentice Sorcerer). Per CR 715.3a only
the Adventure's characteristics are evaluated. Update CastingPermissionService.matchesTopLibraryPermission/
canCastFromTopOfLibrary, add an Adventure option to SpellCastingService.playCardFromLibraryTop mirroring
the hand Adventure cast, thread it through the networking message, the harness helpers and
GameViewProjectionFactory.getPlayableLibraryTopCards. Start with JohannApprenticeSorcererTest
adventureOnTopIsOfferedAsPlayableUsingItsInstantCharacteristics. Don't run the full test suite; ask me.
Don't commit until I say so.
```

---

## 34. Asking death replacements before the creature leaves, outside state-based actions

**Status:** open · **Found:** 2026-10-18, while implementing CR 616.1 death replacement ordering

**Failing tests:**
- None yet. Write one first: Kalitas, Traitor of Ghet on one side, Gravebane Zombie on the other, and
  Murder (or Diabolic Edict) on the Zombie. Expect the Zombie's controller to be asked "library or
  Kalitas" while the Murder is still resolving, with the Zombie leaving inside the Murder's event.

**What the rules require.** CR 616.1: when several replacement effects would modify how a creature
dies, its controller chooses one, and the event then happens with that choice. State-based deaths do
this already (`PermanentRemovalService.awaitStateBasedDeathReplacementChoices` asks first, then the
batch dies together). Outside state-based actions, a death whose options all exile leaves at once and
the controller picks the rider afterwards (`DeathExileRiderChoice`), which gives the same outcome.
The remaining gaps:
- When one option keeps the card (Firestorm Phoenix's return to hand, Gravebane Zombie's library,
  Pulmonic Sliver's "may"), the creature stays on the battlefield until its controller answers
  (`DeathExileReplacementChoice` with `stateBased = false`). Meanwhile the resolving effect carries
  on: a sacrifice reports failure (cost payment, "sacrifice, then…" handlers), "destroy, then…"
  steps run with the creature still there, and creatures dying with it lose CR 603.10a look-back.
- A rider chosen during resolution is applied after the rest of the *same* effect handler. Later
  effects of the spell already wait (`EffectResolutionService` parks them).

**Why it's large.** Doing it right means asking before the creature leaves, inside a running effect
handler or a cost payment, and the engine can't pause a handler partway through. Approaches looked at:
- Leave the battlefield now and choose the destination later: the hand, library and graveyard
  pipelines fire different leave-the-battlefield triggers depending on the destination, so delaying
  them loses their look-back.
- Simulate the effect on a copy of `GameData` first, collect the death questions, ask them, then
  resolve for real with the answers: needs engine services to run on a copy mid-resolution, and
  still misses deaths that come after another prompt inside the effect.
- A death journal like `DamageJournal`, re-running the effect after the answer: every handler would
  need to be safe to re-run (choices, tokens, life changes made before the death replay correctly).

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 34: outside state-based actions, competing death
replacements where one option keeps the card (hand/library) must be chosen before the creature
leaves, without leaving it on the battlefield while the effect keeps resolving. Start from
PermanentRemovalService.removePermanentToGraveyard (the DeathExileReplacementChoice path with
stateBased = false) and EffectResolutionService's rerunCurrentEffectAfterInteraction. Write the
Kalitas + Gravebane Zombie + Murder test first. Don't run the full test suite; ask me.
Don't commit until I say so.
```

---

## 35. Choosing the order of token-creation replacement effects

**Status:** open · **Found:** 2026-10-18, while fixing Q-card test failures

**Failing tests:**
- `QueenAllenalOfRuadachTest.doesNotSilentlyForceTokenDoublingBeforeQueenReplacement()`

**What the rules require.** CR 616.1: when several replacement effects would change how tokens are
created for a player, that player chooses which one applies first, then the next, and so on
(CR 616.1e: any applicable one may be chosen). With Queen Allenal of Ruadach ("those tokens plus an
additional 1/1 Soldier") and Parallel Lives (twice that many) out, Captain's Call can make 8 Soldiers
(Queen first: (3+1)x2) or 7 (Parallel Lives first: 3x2+1). The engine always applies the multiplier
(`MultiplyTokenCreationEffect`) to the base amount and then adds the Queen/Frog/Squirrel-style
additions unmultiplied, in a fixed order with no prompt, so the player always gets 7.

**Why it's large.** Token creation doesn't go through `ReplacementOrderService`. The fixed order is
duplicated in three creation paths (`PermanentControlSupport` ~L120-240, `DestructionSupport` ~L1012,
`TokenCopySupport` ~L193) plus the additive helpers in `TokenCreationReplacementSupport`. A fix needs
a token-creation replacement event that collects every applicable multiplier and addition, a choice
prompt (or a single "order" choice) when more than one applies, and suspend/resume at each creation
site — some of which run mid-effect. Silently picking the "best" order is not acceptable: the
player must choose.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 35: let the player order token-creation replacement
effects (CR 616.1) — multipliers like Parallel Lives/Doubling Season and additions like Queen
Allenal of Ruadach, Quina Qu Gourmet, Chatterfang. Unify the fixed ordering in PermanentControlSupport,
DestructionSupport and TokenCopySupport behind one token-creation replacement event, and reuse the
ChoiceContext / ReplacementOrderService approach from the CR 616.1 death replacement ordering.
Make QueenAllenalOfRuadachTest.doesNotSilentlyForceTokenDoublingBeforeQueenReplacement pass.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 36. Quenchable Fire's pay-{U}-before-upkeep special action

**Status:** open · **Found:** 2026-10-18, while fixing Q-card test failures

**Failing tests:**
- None. Write one: after Quenchable Fire resolves, the targeted player pays {U} during the caster's
  end step (or any time they could cast an instant) and no damage is dealt at the caster's upkeep.

**What the rules require.** Quenchable Fire: "It deals an additional 3 damage to that player or
planeswalker at the beginning of your next upkeep step unless that player or that planeswalker's
controller pays {U} before that step." Its ruling: paying is a special action that player may take
any time they have priority before that upkeep starts; once the delayed trigger is on the stack it
can't be stopped. The engine now pushes the delayed damage trigger under the caster's control at
upkeep (`StepTriggerService`, `DamageAtNextUpkeepUnlessPays`), but there is no way to pay, so the
damage always happens.

**Why it's large.** It needs a new player action: a special action (no stack) offered while the
player has priority, listed in the action-availability service and the client UI, which pays the
mana and removes the matching `DamageAtNextUpkeepUnlessPays` delayed action.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 36: add a special action that lets the affected player
pay {U} before the caster's next upkeep to cancel Quenchable Fire's delayed damage
(DamageAtNextUpkeepUnlessPays). Offer it whenever that player has priority, through
GameActionAvailabilityService and the networking/frontend action list. Don't run the full test
suite; ask me. Don't commit until I say so.
```

---

## 37. Choosing modes (and tier costs) when casting a modal spell from a graveyard

**Status:** open · **Found:** 2026-10-18, while fixing Q-card test failures

**Failing tests:**
- `QuistisTrepeTest.tieredSpellRequiresChoosingAModeBeforeItIsCast()`

**What the rules require.** CR 601.2b: modes are announced as the spell is cast, and a tiered spell's
chosen tier adds its cost. Casting from hand does this (`SpellCastingService.playCard`, with
`tieredManaSuffix`). Casting from a graveyard (`MayCastHandlerService.handleCastCardFromGraveyardChoice`,
Quistis Trepe and other "you may cast target card from a graveyard" effects) announces no mode: the
spell goes on the stack with an unresolved `ChooseOneEffect` and pays no tier cost.

**Why it's large.** Needs a new graveyard-cast mode choice (a `ChoiceContext` next to
`LibraryFreeCastModeChoice` in `ChoiceHandlerService`), unwrapping the chosen mode via
`SpellCastingService.prepareModalSpellCast`, carrying a replacement mana cost for the tier through
`GraveyardCastSpellTarget` (many constructors) to both pay sites (`MayCastHandlerService` untargeted
pay and `PermanentChoiceSpellHandlerService.handleGraveyardCastSpellTarget`), and building targets
from the chosen mode's effects. Roughly 150-200 lines across 5-6 files.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 37: casting a modal or tiered spell from a graveyard
(MayCastHandlerService.handleCastCardFromGraveyardChoice) must announce the mode (CR 601.2b) and pay
the tier cost. Add a graveyard-cast mode choice next to LibraryFreeCastModeChoice, unwrap via
prepareModalSpellCast, pass the tiered replacement cost through GraveyardCastSpellTarget, and make
QuistisTrepeTest.tieredSpellRequiresChoosingAModeBeforeItIsCast pass. Don't run the full test suite;
ask me. Don't commit until I say so.
```

---

## 38. "Until end of turn" effects under Runed Terror's phase-by-phase turn structure

**Status:** open · **Found:** 2026-10-19, while fixing R-card test failures

**Failing tests:**
- `RunedTerrorTest.temporaryBoostSurvivesOtherPlayersCleanup()`

**What the rules require.** Runed Terror makes players take each phase in sequence instead of whole
turns, so every player gets a cleanup step during one turn cycle. Its ruling (2024-11-08, found by web
search; re-check before implementing)
says an "until end of turn" effect ends at the cleanup step of the player who was active when the
effect began. CR 514.2 ends "until end of turn" and "this turn" effects at cleanup. The engine's
`TurnCleanupService.applyCleanupResets` / `resetEndOfTurnModifiers` clears every temporary modifier at
whichever cleanup comes first, so Titanic Growth cast during player 2's phases ends at player 1's
cleanup.

**Why it's large.** Every until-end-of-turn store (power/toughness modifiers, granted keywords,
floating layer effects, "this turn" flags and sets) would need to record the active player at creation
and be cleared only at that player's cleanup while Runed Terror's structure is in effect. That touches
most of `TurnCleanupService` and every place that creates such effects.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 38: while RunedTerrorEffect is active, "until end of turn"
effects must end at the cleanup step of the player who was active when the effect was created
(Runed Terror ruling; CR 514.2). Record the creating active player on temporary modifiers / floating
effects and make TurnCleanupService.applyCleanupResets clear only those belonging to the player whose
cleanup it is. Make RunedTerrorTest.temporaryBoostSurvivesOtherPlayersCleanup pass without changing
normal-game behaviour. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 39. Non-mana triggers from mana abilities are held during the mana-undo window

**Status:** open · **Found:** 2026-10-19, while fixing R-card test failures

**Failing tests:**
- `RelicPutrescenceTest.manaAbilityResolvesBeforePoisonTrigger()` ("Tapping for mana adds mana
  immediately and puts poison on the stack")

**What the rules require.** CR 605.3b: an activated mana ability resolves immediately. Relic
Putrescence's "Whenever enchanted artifact becomes tapped, its controller gets a poison counter" is
not a mana ability, so per CR 603.3 it goes on the stack the next time a player would receive
priority. The engine's `GameService.flushManaAbilityTriggersAfterActivation` deliberately keeps such
triggers in `pendingManaAbilityTriggers` while `revertableManaActivations` is non-empty, so the
player can still undo the mana tap. The trigger therefore is not on the stack after the tap.

**Why it's large.** It is a conflict between rules timing and the client's "undo mana tap" UX.
Fixing it means deciding that a mana activation whose trigger leaves the mana-ability world can't
be reverted (or ending the revert window at that point), then changing the revert bookkeeping, the
flush logic, and the frontend's undo affordance.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 39: when an activated mana ability causes a non-mana
triggered ability (e.g. Relic Putrescence), put that trigger on the stack at the next priority
(CR 603.3) instead of holding it in pendingManaAbilityTriggers during the revertable-mana window.
Decide how this interacts with revertableManaActivations (likely: that activation becomes
non-revertable) and update GameService.flushManaAbilityTriggersAfterActivation and the client undo
UI. Make RelicPutrescenceTest.manaAbilityResolvesBeforePoisonTrigger pass. Don't run the full test
suite; ask me. Don't commit until I say so.
```

---

## 40. Mana riders and spending restrictions tied to specific units of mana

**Status:** open · **Found:** 2026-10-19, while fixing S-card test failures

**Failing tests:**
- `SunkenPalaceTest.activatingAnotherManaAbilityDoesNotConsumeTheCopyRider()`
- `SunkenPalaceTest.freeSpellDoesNotConsumeTheCopyRider()`
- `SunkenPalaceTest.spellPaidWithOtherManaDoesNotTriggerACopy()`
- `SunkenCitadelTest` "Damping Sphere changes mana type and amount but preserves the spending restriction"

**What the rules require.** CR 106.6: a mana ability can restrict how its mana is spent, add an
effect to the spell or ability that mana is spent on, or create a delayed trigger that fires when
that mana is spent. Sunken Palace's {U} copies the spell or ability *that mana* is spent on, so the
copy must fire only when that exact unit pays for something. Sunken Citadel's two mana "spend this
mana only to activate abilities of land sources" keeps its restriction even when Damping Sphere
replaces it with different mana. The engine instead arms the Palace rider per player:
`ActivatedAbilityExecutionService` calls `TriggerCollectionService.consumeManaLinkedCopyOnManaAbility`
on any later mana ability, and the next spell is copied whichever mana paid for it. Citadel's
restriction is lost when a replacement changes the mana.

**Why it's large.** `ManaPool` tracks counts per color plus a few special pools. Riders and
restrictions have to stay attached to individual units through every payment path, cost
reduction, free cast, mana-doubling and mana-replacement effect, and spell copy. That touches the
pool, all of `ManaCost`'s payment helpers, and every caller that spends mana.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 40: tag mana units in ManaPool with their rider/spend
restriction (CR 106.6) so Sunken Palace's "when you spend this mana ... copy that spell or ability"
fires only for the spell or ability that the tagged {U} actually paid for, and Sunken Citadel's
land-ability-only restriction survives Damping Sphere-style mana replacement. Replace the per-player
consumeManaLinkedCopyOnManaAbility arming. Make the failing SunkenPalaceTest and SunkenCitadelTest
cases pass. Don't run the full test suite; ask me. Don't commit until I say so.
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
