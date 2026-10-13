# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 1. A resolving instant or sorcery can be targeted until it finishes resolving

**Status:** open · **Found:** 2026-10-12, while fixing E-card tests

**Failing tests:**
- `ElectrodominanceTest.freeSpellCanTargetResolvingElectrodominance()`
- `EmergentUltimatumTest` ("A free counterspell can target Emergent Ultimatum while it is resolving")

**What the rules require.** CR 608.2n: "As the final part of an instant or sorcery spell's resolution, the spell is put into
its owner's graveyard." Until then the spell is still on the stack, so a spell cast during its
resolution (Electrodominance's free cast, Emergent Ultimatum's opponent-chosen free cast) can target
it. `StackResolutionService.resolveTopOfStack` takes the entry off `gameData.stack` before its effects
run, so `findSpellOnStack`, `ValidTargetService` and `TargetLegalityService` never see it.

**Why it's large.** The resolving entry has to stay on the stack and be targetable (and counterable) through every
resolution-time cast path (`MayCastHandlerService`, `ExileCastTargetSupport`, `SpellCastingService`),
while the rest of the engine, including fizzle checks and stack views, still treats it as resolving.
That touches the core resolution loop.

**Prompt to start it:**

```
Keep a resolving instant or sorcery on the stack, targetable and counterable, until the final step of
its resolution (CR 608.2n — verify with the rules MCP). Today StackResolutionService removes the entry
before its effects run, so a spell cast during resolution can't target it. Start with
ElectrodominanceTest and EmergentUltimatumTest, then ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 2. Demonstrate is a triggered ability whose choice is made on resolution

**Status:** open · **Found:** 2026-10-12, while fixing E-card tests

**Failing tests:**
- `ExcavationTechniqueTest` ("Demonstrate offers the caster a chance to choose a new copy target",
  "The decision to demonstrate is made when the trigger resolves")

**What the rules require.** CR 702.144a: demonstrate is a triggered ability ("When you cast this spell, you may copy it and you
may choose new targets for the copy..."), and CR 603.5 says an optional trigger goes on the stack and
"the choice is made when the ability resolves". `TriggerCollectionService` turns a lone no-target
`MayEffect` in `ON_SELF_CAST` into a `PendingMayAbility` right at cast time, so the player decides
before the trigger is on the stack. The new-target choice for the caster's and the opponent's copies
is also missing.

**Why it's large.** The tests for Healing Technique, Incarnation Technique, Replication Technique, Creative Technique,
Transforming Flourish and The Twelfth Doctor all answer the may prompt right after casting, so they
need to change too, and the copy new-target prompts are new interactions.

**Prompt to start it:**

```
Make demonstrate (CR 702.144a — verify with the rules MCP) a trigger that goes on the stack when the
spell is cast, with the "may copy" decided on resolution (CR 603.5) and new targets offered for both
copies. Today TriggerCollectionService asks the may at cast time. Start with ExcavationTechniqueTest,
update the other demonstrate cards' tests, then ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 3. "Enter as a copy" for lands put onto the battlefield by effects

**Status:** open · **Found:** 2026-10-12, while fixing E-card tests

**Failing tests:**
- `EchoingDeepsTest.landPutOntoBattlefieldByAnEffectStillOffersCopyChoice()`

**What the rules require.** "As [this permanent] enters..." and "[this permanent] enters as..." are replacement effects
(CR 614.1c) that apply however the permanent enters. Echoing Deeps' copy choice is only offered on
the land-play paths in `SpellCastingService` (`LandCopyOnEnterService.prepare`). Lands put onto the
battlefield by an effect (library search, `LibraryChoiceHandlerService`, `putPermanentOntoBattlefield`,
`BattlefieldEntryBatchSupport`) enter without it, and Clone-style permanents have the same gap.

**Why it's large.** Every effect-driven battlefield entry needs an async replacement choice, including the batch entry
path. `LandCopyOnEnterService` depends on `BattlefieldEntryService`, so wiring it in risks a
dependency cycle.

**Prompt to start it:**

```
Offer "enter as a copy" replacements (CR 614.1c — verify with the rules MCP) on every
battlefield-entry path, not just land plays and casts: library searches, direct puts and batch entry.
Start with EchoingDeepsTest, check the Clone-style cards, then ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 4. Sacrificing several permanents as one cost happens simultaneously

**Status:** open · **Found:** 2026-10-12, while fixing E-card tests

**Failing tests:**
- `EmrakulsEvangelTest.sacrificedObserversSeeEachOtherDieSimultaneously()`

**What the rules require.** Emrakul's Evangel sacrifices itself and any number of other non-Eldrazi creatures as one cost, so
they all leave the battlefield at the same time. Leaves-the-battlefield triggers look back in time
(CR 603.10a), so two Ulvenwald Observers should each see the other die (4 draws). The engine pays
activation-cost sacrifices one at a time through `AbilityActivationService.sacrificePermanentAsCost`
(and `SacrificeXPermanentsCostHandler`), so the second Observer never sees the first (3 draws).

**Why it's large.** Cost sacrifices are paid across several async prompts and completion paths in
`AbilityActivationService`. They all need to collect the chosen permanents and remove them as one
simultaneous batch, with dies triggers collected against the batch.

**Prompt to start it:**

```
Make multi-permanent sacrifice costs remove all chosen permanents simultaneously, so dies triggers
look back across the whole batch (CR 603.10a — verify with the rules MCP). Today
AbilityActivationService sacrifices them one at a time. Start with EmrakulsEvangelTest, then ask me to
run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

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
