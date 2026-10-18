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
