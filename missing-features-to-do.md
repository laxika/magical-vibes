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

## 24. Players don't choose the order of simultaneous attack (and most other) triggers

**Status:** open · **Found:** 2026-10-16, while fixing H-card tests

**Failing tests:**
- `HeroOfBladeholdTest` "Controller can choose the order of the simultaneous attack triggers"

**What the rules require.** CR 603.3b: when several abilities trigger since a player last received
priority, each player in APNAP order puts the triggered abilities they control on the stack in any
order they choose. The engine only asks for an order for spell-cast triggers
(`beginSpellCastTriggerOrder`). Attack triggers (and other batches) go on the stack in collection order.

**Why it's large.** Adding the prompt to attack triggers changes the flow of every test that declares
attackers with two or more triggers and then calls `passBothPriorities()` without answering a prompt
(many classes across all letters). It needs a general trigger-ordering interaction plus either a
harness auto-answer for "default order" or rewriting those tests.

**Prompt to start it:**

```
Implement CR 603.3b trigger ordering for simultaneous triggered abilities beyond spell-cast triggers,
starting with attack triggers (HeroOfBladeholdTest.controllerChoosesAttackTriggerOrder). Generalise the
existing beginSpellCastTriggerOrder flow into one ordering interaction used whenever a player controls
two or more non-equivalent triggers put on the stack together. Add a harness default so existing tests
that don't care about order keep passing, verify the CR text with the rules MCP, and add tests.
```

## 25. Remaining H-card failures

**Status:** open · **Found:** 2026-10-16, while fixing H-card tests

These H-card test classes still fail after three Haiku rounds and two Sonnet rounds. Most are one-card
engine gaps. Each needs its own investigation.

**Diagnosed, need engine work:**
- `HardenedScalesTest.controllerChoosesOrderWithCounterDoubler` — counters a creature enters with go
  through `GameQueryService.replaceCounters` inline in `BattlefieldPlacementService`, so there is no
  CR 616.1 order prompt when Hardened Scales and Corpsejack Menace both apply.
- `HydraulicHelperTest` — "can't be spent to cast a nonartifact spell" mana is modeled as
  artifact-spells-or-abilities-only mana, so it can't pay Mana Leak's cost. Needs its own mana restriction.
- `HitTheMotherLodeTest.freeCasting…AdditionalCosts…` — discover free casts skip `TapMultiplePermanentsCost`.
- `HiveOfTheEyeTyrantTest.repeatedAnimationGrantsTwoSeparateExileTriggers` — `CombatAttackService` bundles
  all graveyard-target ON_ATTACK effects into one trigger.
- `HopelessNightmareTest` — the scry runs twice in one resolution after the sacrifice ability.

**Remaining (undiagnosed or partly fixed):** `HaktosTheUnscarredTest`, `HaliyaGuidedByLightTest` (warp on a
later turn), `HammerheadTyrantTest`, `HammerOfBogardanTest`, `HarmsWayTest`, `HarvesterOfSoulsTest`,
`HazoretsUndyingFuryTest` (escalate on a free spell; lands-don't-untap scope), `HellcatUndyingVigilanteTest`,
`HellkiteCourserTest`, `HerculesOlympianHeroTest`, `HildibrandMandervilleTest`, `HinderTest`,
`HollowMarauderTest`, `HomaridWarriorTest` (next-untap restriction vs control change),
`HornOfPlentyTest`, `HostOfTheHereafterTest`, `HotPursuitTest`, `HousemeldTest`,
`HulklingYoungAvengerTest`, `HuntersTalentTest` (spell target arity counts the attack target group),
`HuntmasterOfTheFellsTest`, `HurkylsRecallTest`, `HydroManFluidFelonTest`.

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
