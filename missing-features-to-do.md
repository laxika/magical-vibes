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

## 9. Reflexive "when you do" triggers that choose targets after the action

**Status:** open · **Found:** 2026-10-14, while fixing G-card tests

**Failing tests:**
- `GlorifierOfSufferingTest` (5 tests: may, then sacrifice, then up to two targets)
- `GoShintaiOfAncientWarsTest`, `GoShintaiOfBoundlessVigorTest`, `GoShintaiOfHiddenCrueltyTest` ("you may pay {1}. When you do, ... target ...")

**What the rules require.** CR 603.12: a resolving ability that says "when you do" creates a reflexive
triggered ability. It triggers after the action is taken, goes on the stack as its own ability, and
chooses its targets then. The engine either chooses the targets with the original trigger (card-level
`target(...)` before the "may") or, for `MayPayManaEffect.reflexiveTarget`, resolves the wrapped effect
inline on payment with no separate stack object.

**Why it's large.** About 11 cards share the `MayPayManaEffect.reflexiveTarget` path, and multi-target
reflexive triggers (Glorifier's "up to two") have no way to declare targets for a reflexive ability.
Target selection, stack entries and the may/payment prompts all have to be re-sequenced.

**Prompt to start it:**

```
Implement reflexive triggered abilities per CR 603.12: after the "you may [pay/sacrifice]" action, put a
separate triggered ability on the stack and choose its targets then (including multi-target "up to two").
Migrate MayPayManaEffect.reflexiveTarget users and Glorifier of Suffering. Start with
GlorifierOfSufferingTest and the three Go-Shintai tests, then ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 16. Per-mana color choice for "that much mana in any combination of colors"

**Status:** open · **Found:** 2026-10-14, while fixing G-card tests

**Failing tests:**
- `GrandWarlordRadhaTest` "Two attackers can produce one red and one green mana"

**What the rules require.** Grand Warlord Radha adds "that much mana in any combination of {R} and/or
{G}", so each mana gets its own color. The engine asks once and adds all of it in one color.

**Why it's large.** The mana-color choice interaction returns a single color. It needs a "split N mana
across colors" choice (client UI included) and reuse by other "any combination" producers.

**Prompt to start it:**

```
Add a "distribute N mana among these colors" choice and use it for Grand Warlord Radha's "any
combination of {R} and/or {G}". Start with GrandWarlordRadhaTest, then ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 21. Undiagnosed G-card failures

**Status:** open · **Found:** 2026-10-14, while fixing G-card tests

These G-card test classes still fail after two Haiku rounds and a Sonnet pass. No one found a single
shared root cause. Most are one-card engine gaps (wrong timing of a choice, a missing replacement or
trigger condition, a lost-ability or LKI case) rather than test setup. Each needs its own investigation.

**Probably test flow, worth trying first** (`passUntil` stops on a discard, attack or may prompt that the
test didn't expect, so check whether the prompt is legitimate before changing the test):
`GempalmSorcererTest`, `GiantCaterpillarTest`, `GiantTurtleTest`, `GloriousEndTest`, `GodPharaohsGiftTest`, `GrimMonolithTest`, `GuardianOfTheGuildpactTest`, `GundabadOpportunistTest`, `GyomeMasterChefTest`.

**Everything else:** `GadwicksFirstDuelTest`, `GaleWaterdeepProdigyTest`, `GalvanicKeyTest`, `GalvanothTest`, `GandalfPartyGuestTest`, `GandalfSparkStarterTest`, `GangOfDevilsTest`, `GarthOneEyeTest`, `GarzasAssassinTest`, `GateToPhyrexiaTest`, `GatherSpecimensTest`, `GatherTheTownsfolkTest`, `GazeOfPainTest`, `GazeOfTheGorgonTest`, `GemOfBecomingTest`, `GenesisTest`, `GenjiGloveTest`, `GeodeGolemTest`, `GeosurgeTest`, `GeothermalKamiTest`, `GerrardsHourglassPendantTest`, `GetawayCarTest`, `GhaltaAndMavrenTest`, `GhirapurTest`, `GhiredConclaveExileTest`, `GhituLavarunnerTest`, `GhostHoundsTest`, `GhostlyDancersTest`, `GhostlyFlameTest`, `GhostlyWingsTest`, `GhostTownTest`, `GhostwayTest`, `GhoulishImpetusTest`, `GhoulishProcessionTest`, `GhoulsNightOutTest`, `GhyrsonStarnKelermorphTest`, `GiantAdephageTest`, `GiantInheritanceTest`, `GiantOysterTest`, `GideonBlackbladeTest`, `GideonJuraTest`, `GideonMartialParagonTest`, `GideonsSacrificeTest`, `GilaCourserTest`, `GildedDrakeTest`, `GilraenDNedainProtectorTest`, `GirderGoonsTest`, `GisaGloriousResurrectorTest`, `GixianPuppeteerTest`, `GlaciersTest`, `GlamermiteTest`, `GlamorousOutlawTest`, `GlarbCalamitysAugurTest`, `GlarewielderTest`, `GlassAspTest`, `GlassOfTheGuildpactTest`, `GlasspoolMimicTest`, `GleamingOverseerTest`, `GleamOfAuthorityTest`, `GlenElendrasAnswerTest`, `GlimpseOfTomorrowTest`, `GlimpseTheUnthinkablePlaytestTest`, `GlintHawkTest`, `GlintRakerTest`, `GlintSleeveArtisanTest`, `GlintWeaverTest`, `GlissaTheTraitorTest`, `GlitchInterpreterTest`, `GloomlanceTest`, `GloomshriekerTest`, `GloomSurgeonTest`, `GloriousPurposeTest`, `GlowingOneTest`, `GlyphOfDoomTest`, `GoblinArsonistTest`, `GoblinBarrageTest`, `GoblinCrashPilotTest`, `GoblinDarkDwellersTest`, `GoblinDiplomatsTest`, `GoblinFlectomancerTest`, `GoblinFlotillaTest`, `GoblinGrapplerTest`, `GoblinKitesTest`, `GoblinSappersTest`, `GoblinslideTest`, `GoblinTestPilotTest`, `GoblinTinkererTest`, `GoblinTownTest`, `GoblinTrapfinderTest`, `GoblinTraprunnerTest`, `GoblinWarBuggyTest`, `GoblinWarCryTest`, `GoddricCloakedRevelerTest`, `GodEternalKefnetTest`, `GodEternalOketraTest`, `GodosIrregularsTest`, `GogoMasterOfMimicryTest`, `GogoMysteriousMimeTest`, `GoldenArgosyTest`, `GoldlustTriadTest`, `GoldmeadowTest`, `GoldveinPickTest`, `GoliathDaydreamerTest`, `GoNinjaGoTest`, `GontiLordOfLuxuryTest`, `GontisAetherHeartTest`, `GorillaWarCryTest`, `GoShintaiOfLostWisdomTest`, `GossipsTalentTest`, `GraazUnstoppableJuggernautTest`, `GracefulAdeptTest`, `GracefulAntelopeTest`, `GracefulTakedownTest`, `GrafRatsTest`, `GraftedGrowthTest`, `GrahaTiaScionRebornTest`, `GrapplingHookTest`, `GrapplingKrakenTest`, `GraspingShadowsTest`, `GraspOfFateTest`, `GraspOfTheHieromancerTest`, `GraveExpectationsTest`, `GravenArchfiendTest`, `GravePactTest`, `GraveResearcherReanimateTest`, `GravestormTest`, `GravitationalShiftTest`, `GreasefangOkibaBossTest`, `GreatbowDoyenTest`, `GreatFangChroniclersTest`, `GreatGildedBoatTest`, `GreatOakGuardianTest`, `GreatTrainHeistTest`, `GreenGoblinBackForMoreTest`, `GreenSunsTwilightTest`, `GreenSunsZenithTest`, `GreenwardenOfMurasaTest`, `GremlinInfestationTest`, `GriefTest`, `GrimCaptainsCallTest`, `GrimDraugrTest`, `GrimeGorgerTest`, `GrimHaruspexTest`, `GrimHarvestTest`, `GrimoireOfTheDeadTest`, `GrimoireThiefTest`, `GrimReaperLethalLegionnaireTest`, `GrimReapersSprintTest`, `GristleGluttonTest`, `GrixisSojournersTest`, `GrizzledHuntmasterTest`, `GrotagNightRunnerTest`, `GroveOfTheDreampodsTest`, `GrubStoriedMatriarchTest`, `GruesomeDiscoveryTest`, `GruesomeEncoreTest`, `GruesomeSlaughterTest`, `GuardianArchonTest`, `GuardianOfGhirapurTest`, `GuardiansOfKoilosTest`, `GuildFeudTest`, `GuildpactGreenwalkerTest`, `GuiltyConscienceTest`, `GunnerConscriptTest`, `GurmagRakshasaTest`, `GusthasScepterTest`, `GutFanaticalPriestessTest`, `GutterSkulkerTest`, `GutTrueSoulZealotTest`, `GuulDrazAssassinTest`, `GuyInTheChairTest`, `GwafaHazidProfiteerTest`, `GwenomRemorselessTest`, `GwenStacyTest`, `GyrusWakerOfCorpsesTest`.

**What the rules require.** It differs per card. Verify the Oracle text and rulings for each one before
changing anything. Several of these tests already had wrong agent "fixes" reverted, so read the whole
test class first.

**Why it's large.** About 184 classes, each with its own cause.

**Prompt to start it:**

```
Work through the classes in missing-features-to-do.md entry 21 a few at a time: run them, find each root
cause, and fix the engine or card (never weaken an assertion without an official ruling or verified CR
text). Start with the "probably test flow" group. Don't run the full test suite; ask me.
Don't commit until I say so.
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
