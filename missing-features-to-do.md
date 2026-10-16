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
- `GisaGloriousResurrectorTest.opposingControllerChoosesBetweenCompetingExileReplacements` — with
  Gisa and Liesa both out, the dying creature's controller must choose which "exile it instead"
  applies (CR 616.1). `PermanentRemovalService.opponentDyingCreatureExileReplacement` just takes the
  first match; the death pipeline has no pause point for that choice. The class's other tests pass.
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

## 26. Replacement-order prompt for counters a permanent enters with

**Status:** open · **Found:** 2026-10-16, while fixing H-card tests

**Failing tests:**
- `HardenedScalesTest.controllerChoosesOrderWithCounterDoubler()`

**What the rules require.** CR 616.1: when two or more replacement effects would modify how an event
affects an object, its controller chooses which to apply first. Hardened Scales ("one additional") and
Corpsejack Menace ("twice that many") give different totals depending on order (5 → 12 or 11). The
engine already prompts for counters put on a permanent already on the battlefield
(`ChoiceContext.CounterReplacementOrder`). Counters a permanent *enters with* go through
`GameQueryService.replaceCounters` inline during placement (`BattlefieldPlacementService`,
`GraveyardReturnSupport.putCardOntoBattlefield`, and others), which applies a fixed order and never asks.

**Why it's large.** The prompt has to pause battlefield entry partway through. The permanent is built
but hasn't entered yet, so ETB triggers, the legend rule and "as enters" choices all have to wait for
the answer and then resume. There are many entry call sites.

**Prompt to start it:**

```
Make counters a permanent enters with go through the CR 616.1 replacement-order prompt (Hardened Scales +
Corpsejack Menace) instead of being applied inline by GameQueryService.replaceCounters during placement.
Start with HardenedScalesTest.controllerChoosesOrderWithCounterDoubler, then check the other "enters with
counters" paths. Don't run the full test suite; ask me. Don't commit until I say so.
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
