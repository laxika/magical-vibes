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

## 14. Conjured cards are not tokens

**Status:** open · **Found:** 2026-10-14, while fixing G-card tests

**Failing tests:**
- `GyoxBrutalCarnivoraTest` (`gyoxTriggersForItsOwnDeathWithOil()`, `conjuredCreatureCanTriggerGyoxWhenItDies()`,
  "A nontoken creature with oil counters dying conjures scaled perpetual duplicates into the library")
- `GoblinMoraleSergeantTest.enlistingPreviouslyConjuredCreatureStillOffersDuplicate()`

**What the rules require.** Conjure (a digital-only mechanic, no CR entry) creates a *card* in a zone; per
the Alchemy rulings a conjured card is not a token, so "nontoken creature" checks and duplicate effects
treat it like any other card. The engine builds conjured cards with the token flag set.

**Why it's large.** The token flag currently also drives cleanup (tokens cease to exist outside the
battlefield). Conjured cards need their own marker so they persist in every zone while still counting as
nontoken, and every conjure effect has to switch over.

**Prompt to start it:**

```
Stop marking conjured cards as tokens: give them their own "conjured" marker, keep them in every zone,
and make "nontoken" checks treat them as cards. Check the Alchemy rulings first. Start with
GyoxBrutalCarnivoraTest and GoblinMoraleSergeantTest, then ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 15. Last-known information for sources that changed zones

**Status:** open · **Found:** 2026-10-14, while fixing G-card tests

**Failing tests:**
- `GiantAlbatrossTest` "Exiling the Albatross in response does not erase its death trigger's damage history"
- `GomazoaTest.stillTucksBlockedCreatureWhenSourceIsReturnedToHand()`

**What the rules require.** CR 608.2h: when the source has left the zone it was expected in, the effect
uses its last known information. Giant Albatross's death trigger must still know which creatures it dealt
damage to after the card is exiled from the graveyard; Gomazoa's ability must still find the creatures it
blocked after Gomazoa itself is put into a library or hand. The engine looks this data up on the live
permanent, which is gone.

**Why it's large.** Per-permanent combat and damage history isn't snapshotted onto stack entries. It
needs a general LKI snapshot (combat relations, damage dealt this turn) captured when the ability is put
on the stack or when its source leaves.

**Prompt to start it:**

```
Snapshot a source's combat and damage history onto its stack entry so effects use last-known
information when the source has left its zone (CR 608.2h). Start with GiantAlbatrossTest and GomazoaTest,
then ask me to run the full suite. Don't run the full test suite; ask me. Don't commit until I say so.
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

## 17. Layer timestamps and text-changing effects

**Status:** open · **Found:** 2026-10-14, while fixing G-card tests

**Failing tests:**
- `GaeasLiegeTest` "A resolved conversion overrides an earlier Evil Presence even if Liege entered first"
- `GlamerdyeTest` "Changing nongreen to nonblue makes a blue creature target illegal",
  "Changing a color word changes which multicolored spell triggers the target",
  "A text change to a target spell carries onto the permanent it becomes (CR 400.7a)"

**What the rules require.** CR 613.7: within a layer, effects apply in timestamp order, so Gaea's Liege's
resolved land-type change (timestamped when its ability resolved) overrides an earlier Evil Presence. CR
612.1: a text-changing effect changes the words on the object, so Glamerdye's changed color word changes
the targeting restrictions and triggers that read it. The engine stamps the Liege's effect with the
permanent's timestamp and has no real text-changing layer (layer 3).

**Why it's large.** Effects created by resolution need their own timestamps in layer 4, and text changes
need a layer-3 rewrite of color/type words that the predicates and triggers read.

**Prompt to start it:**

```
Give resolved type-changing effects their own timestamps (CR 613.7) and add a layer-3 text-changing
effect for color words (CR 612.1) that targeting predicates and triggers read. Start with GaeasLiegeTest
and GlamerdyeTest, then ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 18. Playing exiled cards during resolution (Gix, Yawgmoth Praetor)

**Status:** open · **Found:** 2026-10-14, while fixing G-card tests

**Failing tests:**
- `GixYawgmothPraetorTest` "Discarding X cards exiles X cards and offers play during resolution",
  "Activation on an opponent's turn offers a creature spell during resolution"

**What the rules require.** Gix's last ability lets you play lands and cast spells from among the exiled
cards *while the ability resolves*, without paying their mana costs, ignoring timing restrictions (a
creature spell on an opponent's turn). The engine only grants a play-from-exile permission for later.

**Why it's large.** Casting spells in the middle of a resolution needs the "cast during resolution" loop
used by cascade-style effects, extended to lands and to a chosen subset of several exiled cards.

**Prompt to start it:**

```
Let Gix, Yawgmoth Praetor's ability cast spells and play lands from among the exiled cards during its
resolution, without paying mana costs and ignoring timing. Reuse the cascade-style cast-during-resolution
flow. Start with GixYawgmothPraetorTest, then ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 19. One search per event and the "may" before searching (needs a decision)

**Status:** open · **Found:** 2026-10-14, while fixing G-card tests

**Failing tests:**
- `GaeasBalanceTest` "All selected lands enter together after the search finishes" (also see #10),
  "Searching an empty library triggers an opponent's search ability only once"
- `GhostQuarterTest` "Controller can decline searching even when the library has no basic lands",
  "Controller can decline the search without shuffling"
- `GeomancersGambitTest` "The controller may decline searching entirely and the caster still draws"

**What the rules require.** Gaea's Balance is one search for five land types, so it's one search event
(one trigger for "whenever an opponent searches"), and the lands enter together. Ghost Quarter and
Geomancer's Gambit say "may search": other tests in these classes expect the search window directly
(declining by finding nothing), these expect a yes/no prompt first and no shuffle on "no". Earlier
attempts to wrap the search in a `MayEffect` broke the other tests.

**Why it's large.** The search flow runs one search per land type, and the classes' own tests disagree
on the "may" flow. Pick one behavior from the official rulings first, then update the losing tests.

**Prompt to start it:**

```
Decide from official rulings whether "you may search" asks a yes/no first or opens the search directly,
and make it consistent across GhostQuarterTest and GeomancersGambitTest. Then make Gaea's Balance a
single search event whose lands enter together. Ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 20. Monstrosity must still be activatable on a monstrous creature

**Status:** open · **Found:** 2026-10-14, while fixing G-card tests

**Failing tests:** none in the G set now (Gluttonous Cyclops was fixed). Wildfire Cerberus, Nessian Asp,
Nemesis of Mortals, Ill-Tempered Cyclops and Vitality Hunter still refuse the activation once monstrous,
and about seven of their tests assert that wrong behavior.

**What the rules require.** CR 701.37a: "Monstrosity N" means "If this permanent isn't monstrous, put N
+1/+1 counters on it and it becomes monstrous." The ability can still be activated (and the cost paid);
it just does nothing on resolution.

**Why it's large.** It's small in code, but it means rewriting other cards' test assertions, which the
G-card task wasn't allowed to do.

**Prompt to start it:**

```
Remove the "already monstrous" activation gate from the remaining Monstrosity cards so the ability can
be activated and does nothing on resolution (CR 701.37a), and fix their tests that assert the gate.
Ask me to run the full suite. Don't run the full test suite; ask me. Don't commit until I say so.
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

## 22. A split card keeps its full name after a half resolves

**Status:** open · **Found:** 2026-10-14, while giving split halves their own characteristics

**Failing tests:**
- `CeaseDesistTest` "Cease's player target is independent of the chosen graveyard" (the graveyard card is
  named "Cease", not "Cease // Desist")

**What the rules require.** CR 709.4: in every zone except the stack, a split card has the combined
characteristics of both halves. Once Cease resolves, the card in the graveyard is Cease // Desist again.
The engine puts the stack's runtime copy, which has only the cast half's name and characteristics, into
the graveyard instead of restoring the whole card.

**Why it's large.** Every path where a spell leaves the stack (resolve, counter, fizzle, exile, return to
hand) has to restore the physical card's whole-card characteristics. Copies and spells cast from other
zones must keep working.

**Prompt to start it:**

```
When a split-card spell leaves the stack, restore the card's whole-card characteristics (CR 709.4) so
Cease // Desist is named "Cease // Desist" in the graveyard after Cease resolves. Cover resolving,
countering, fizzling and other stack exits. Start with CeaseDesistTest, then run the other
split/aftermath/fuse card tests and ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 23. Fuse only from hand, and only on cards with fuse

**Status:** open · **Found:** 2026-10-14, while giving split halves their own characteristics

**Failing tests:**
- `ProfitLossTest` "Fuse cannot be cast from the library through Melek"
- `ToilTroubleTest` "Fuse cannot be cast from the library with Melek"
- `TurnBurnTest.fuseCannotBeCastFromLibraryWithMelek()`
- `InvertInventTest` "Both halves cannot be cast together because the card has no fuse ability"
- `ThrashThreatTest` "Thrash // Threat cannot fuse because it has no fuse ability"

**What the rules require.** CR 702.102a: fuse applies only while the card is in a player's hand. Only
when it is cast from the hand may the player cast both halves. The engine offers the fuse mode when the
card is cast from exile or the library too. The last two tests expect a cast of both halves to be
rejected for split cards without fuse, and the engine accepts it. Neither cause has been diagnosed yet.

**Why it's large.** Fuse is a regular `ChooseOneEffect` mode, so the cast path, the mode list shown to
the player and every "cast from exile/library" permission (Melek, Possibility Storm, Primeval Spawn and
others) have to filter it by source zone.

**Prompt to start it:**

```
Only offer and allow the fuse mode of a split card when it is cast from its owner's hand (CR 702.102a),
and reject casting both halves of a split card without fuse. Start with ProfitLossTest and
InvertInventTest, then run the other split/fuse card tests and ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
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
