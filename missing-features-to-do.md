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

## 7. Reveal-and-put effects keep going while a found card's copy choice is open

**Status:** open · **Found:** 2026-10-13, while giving each simultaneous entry its own copy choice

**Failing tests:**
- None yet. Write one first, e.g. Collected Company putting a Phantasmal Image onto the battlefield
  while another creature is on the battlefield.

**What the rules require.** "Enter as a copy" is a replacement effect (CR 614.1c), so the Image's
choice is made as it enters, before the spell goes on to put the rest of the cards on the bottom.
`LibraryChoiceHandlerService.handleLibraryRevealChoice` places the selected cards and then begins the
`LibraryReorder` for the rest right away. That replaces the open "enter as a copy" may-ability prompt,
so the Image never gets its choice answered. Library searches already stop when a found card's entry
choice is open (`finishSearchAndResume`); the reveal path doesn't.

**Why it's large.** `handleLibraryRevealChoice` has many destination branches (graveyard, exile, hand,
random bottom, ordered bottom, shuffle) that all run after placement, plus follow-ups and the parked
spell. Each needs to wait for the entry choice and then resume.

**Prompt to start it:**

```
Make library reveal-and-put effects (Collected Company and similar) wait for a placed card's
"enter as a copy" choice before handling the rest of the revealed cards (CR 614.1c — verify with the
rules MCP). Today handleLibraryRevealChoice begins the LibraryReorder over the open copy prompt.
Start with a failing Collected Company + Phantasmal Image test, then ask me to run the full suite.
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
