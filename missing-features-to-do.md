# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

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

## 6. Several "enter as a copy" choices in one non-batch entry

**Status:** open · **Found:** 2026-10-13, while adding copy choices to every battlefield entry

**Failing tests:**
- None yet. Write one first, e.g. a search that puts two Echoing Deeps onto the battlefield at once
  (`LibraryChoiceHandlerService.placeCardsOnBattlefieldSimultaneously`), or a flicker that returns two
  Clones together.

**What the rules require.** "[This permanent] enters as . . ." is a replacement effect (CR 614.1c), so
every permanent with one gets its own copy choice, however many enter together. The engine offers the
choice from `BattlefieldPlacementService.place` through `CloneService.beginEntryCopyChoice`, but
`GameData.cloneOperation` holds only one open choice. When a second copy-capable permanent reaches
placement while the first choice is open, it enters without being offered a copy. Paths that go through
`BattlefieldEntryBatchSupport` are fine, because it asks for every copy choice before any card moves.

**Why it's large.** The non-batch simultaneous paths keep placing the rest of the group after the first
entry pauses, so they also break the "can't see each other" rule (CR 614.12) for copy targets. The clean fix
is to move those paths (simultaneous library placement, multi-card flicker returns and similar loops)
onto `BattlefieldEntryBatchSupport`. The other option is to queue open copy choices instead of keeping
one `CloneOperationState`. Either way the change touches many entry paths and their follow-up work
(counters, delayed actions, ETB collection).

**Prompt to start it:**

```
Make every copy-capable permanent in one simultaneous entry get its own "enter as a copy" choice
(CR 614.1c — verify with the rules MCP). Today GameData.cloneOperation holds one open choice, so a
second Clone/Echoing Deeps entering in the same non-batch placement enters without a choice. Start
by writing a failing test (two Echoing Deeps put onto the battlefield by one search, or two Clones
flickered together), then move those paths onto BattlefieldEntryBatchSupport or queue the copy
choices. Then ask me to run the full suite.
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
