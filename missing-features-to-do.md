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

## 31. Ordering competing "dies → exile instead" replacements

**Status:** open · **Found:** 2026-10-16, `K` test batch

**Failing tests:**
- `KalitasTraitorOfGhetTest.opponentChoosesBetweenKalitasAndFlayingTendrilsReplacements()`

**What the rules require.** CR 616.1: when two or more replacement effects try to modify the same event, the affected object's controller chooses one to apply. With Kalitas, Traitor of Ghet ("exile it instead … create a 2/2 Zombie") and Flaying Tendrils ("exile it instead") both applying to an opponent's dying creature, that creature's controller picks which applies. The engine's `PermanentRemovalService` / `tryApplyExileReplacementEffect` just applies the exile replacements in a fixed order with no prompt, so Kalitas's Zombie rider can be lost.

**Why it's large.** Creature death goes through `removePermanentToGraveyard` from many callers that expect a synchronous result. A prompt needs a dying-creature `ReplacementEvent` listing candidate sources, plus a resume path through every caller, including simultaneous-death batches.

**Prompt to start it:**

```
Implement CR 616.1 ordering for competing "if a creature would die, exile it instead" replacement effects (Kalitas, Traitor of Ghet; Flaying Tendrils; Rest in Peace-style effects with riders). When two or more apply to the same dying creature and their outcomes differ, prompt the creature's controller to choose which applies, then resume the removal. Start from PermanentRemovalService.tryApplyExileReplacementEffect and the existing ReplacementOrderService. Make KalitasTraitorOfGhetTest.opponentChoosesBetweenKalitasAndFlayingTendrilsReplacements pass without breaking other death-replacement tests.
```

---

## 32. Simultaneous batches: "one or more" events and APNAP choices

**Status:** open · **Found:** 2026-10-17, while fixing M-card tests

**Failing tests:**
- `MascotExhibitionTest` — Esix must replace all three tokens of one creation event, not each `CreateTokenEffect` separately
- `MythweaverPoqTest.simultaneousLandsEachReceiveADuplicate` — "one or more nontoken lands … for each of them", once each turn
- `MichelangeloImproviserTest` — a creature and a land must enter as one simultaneous batch
- `MogMoogleWarriorTest` — each player's discard choice must stay hidden until all players have chosen
- `MindSwordsTest` — each player's exile-from-hand choice must happen before any card is exiled

**What the rules require.** CR 101.4: when several players make choices at the same time, they choose in
APNAP order, then the actions happen simultaneously. CR 603.2c: an ability triggers once per event unless one
event contains multiple occurrences, so "one or more" triggers need to see one grouped event. The engine
creates tokens and moves cards one effect (or one player) at a time, so these triggers and replacements see
several separate events, and later players can see earlier players' choices.

**Why it's large.** It needs a shared "event batch" concept spanning token creation, battlefield entry,
discard and exile-from-hand choices, plus the replacement (Esix) and trigger collectors that read those
batches. Regression risk is high across token, landfall and discard cards.

**Prompt to start it:**

```
Add simultaneous event batching to the engine (missing-features-to-do.md entry 32): one token-creation event
for multi-token effects (Esix), grouped "one or more lands/permanents enter" triggers, and APNAP choose-then-act
for each-player discard/exile choices (CR 101.4). Start with the five listed M-card tests, verify CR numbers with
the rules MCP, then ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 33. Remaining M-card failures

**Status:** open · **Found:** 2026-10-17, while fixing M-card tests

These M-card tests still fail after two Haiku rounds, two Sonnet passes and a Sonnet escalation with real test
runs. Each is a one-card engine gap:

- `MeriekeRiBeritTest` (4 tests) — needs a per-activation delayed trigger ("when you lose control of Merieke or it
  becomes untapped, destroy that creature") instead of a static linked ability (CR 603.7a).
- `MangarasTomeTest` (2) — several Tomes' replacement effects need a player choice between them, and one draw
  should take a card from each pile.
- `MarchesasSurprisePartyTest` (2) — the secret-mission choice isn't modeled, so the end-step trigger fires
  automatically.
- `MikeyMonaMutantSittersTest` (2) — each mode must target a different player, and targets should be prompted in
  selection order.
- `MarkovBaronTest` — the madness cast pays only from the mana pool, with no convoke.
- `MarshlandHordemasterTest` — granted keywords are a set, so multiple battle cry instances can't exist.
- `MeteoricMaceTest` — a free cascade cast ignores additional costs such as Harrow's land sacrifice.
- `MmmenonTheRightHandTest` — its mana must not pay for abilities, but it shares `ManaRestriction.NonHandSpells`
  with cards whose mana may; it needs its own mana-pool bucket.
- `MemoricideTest` — non-matching hand and library cards must be shown to the searcher, which needs a protocol
  change (the frontend matches card views to pickable ids by index).
- `MalcolmKeenEyedNavigatorTest` — damage-to-opponent triggers only see creature sources; a Pirate spell source isn't
  supported.
- `MalcolmAlluringScoundrelTest.discardedCardCanBeCastWhenAReplacementExilesIt` — casting a discarded card that a
  replacement exiled instead needs cast-from-exile support; no official ruling was found.
- `MurktideRegentTest.escapeExilePaymentIsSeparateFromOptionalDelve` — the test expects 2 counters when delve is used
  together with an escape-style graveyard cast; the engine gives 0. No official ruling was found either way.

**What the rules require.** It differs per card. Verify the Oracle text and rulings for each one first. Several
of these tests already had wrong agent "fixes" reverted, so read the whole test class before changing anything.

**Why it's large.** Twelve classes, each needing its own engine feature.

**Prompt to start it:**

```
Work through the classes in missing-features-to-do.md entry 33 one at a time: run them, confirm the root cause,
and fix the engine or card (never weaken an assertion without an official ruling or verified CR text).
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
