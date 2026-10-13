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

## 6. Split cards keep each half's own characteristics

**Status:** open · **Found:** 2026-10-13, while fixing F-card tests

**Failing tests:**
- `FlotsamJetsamTest.jetsamCannotBeCastDuringCombat()`

**What the rules require.** CR 709.3a: only the half being cast is evaluated to see if it can be cast,
and only that half is put onto the stack. Jetsam is a sorcery half of an instant // sorcery card, so it
can only be cast at sorcery speed. The engine stores one card type per card (the front face's), and the
Scryfall loader only reads face 0 of a split card, so Jetsam is timed as an instant.

**Why it's large.** Per-half type, mana cost and timing data has to come through the card-data loader,
`Card` and the cast path (`selectedSplitModeCharacteristics`), and every split, aftermath and
fuse card needs re-checking.

**Prompt to start it:**

```
Give each half of a split card its own characteristics (types, mana cost, timing) per CR 709.3a,
loaded from Scryfall's card_faces, and use the chosen half's values when casting. Start with
FlotsamJetsamTest, then run the other split/aftermath/fuse card tests and ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 7. Choose in APNAP order, then move simultaneously (clash, each-player discards)

**Status:** open · **Found:** 2026-10-13, while fixing F-card tests

**Failing tests:**
- `FireJugglerTest` "Both clash cards move only after both placement choices are made"
- `FrayingOmnipotenceTest` "Discard choices remain hidden until every player has chosen"

**What the rules require.** CR 101.4: when several players make choices at the same time, they choose
in APNAP order and then the actions happen simultaneously. CR 701.30c applies this to clash: placements
are decided in APNAP order, then the cards move at the same time. The engine moves each clash card and
discards each player's card as soon as that player answers.

**Why it's large.** Clash runs as two separate scry interactions, and each-player discards go through
the shared discard queue (also used by Pox-style effects). Both need a "collect every choice, then apply"
stage across interactions without breaking the existing sequencing.

**Prompt to start it:**

```
Make simultaneous multi-player choices follow CR 101.4: collect every player's choice in APNAP order,
then apply them together. Do it for clash (CR 701.30c) and for "each player discards" effects. Start with
FireJugglerTest and FrayingOmnipotenceTest, run the other clash and each-player-discard card tests, then
ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 8. Face-up schemes in the command zone

**Status:** open · **Found:** 2026-10-13, while fixing F-card tests

**Failing tests:**
- `FearMyAuthorityTest.faceUpSchemeInCommandZoneBuffsCreatures()`
- `FearMyAuthorityTest.faceUpSchemeInCommandZoneTriggersAtControllersUpkeep()`

**What the rules require.** CR 314.4: while a scheme is face up in the command zone, its static abilities
affect the game and its triggered abilities can trigger. CR 701.33b: abandoning a scheme turns it face down
and puts it on the bottom of its owner's scheme deck. The layer system and trigger collection only scan
permanents, so an ongoing scheme's static boost and upkeep trigger never apply.

**Why it's large.** The command zone needs to become a source of static and triggered abilities for the
layer system and every trigger collector, plus a scheme deck to abandon to.

**Prompt to start it:**

```
Let face-up ongoing schemes in the command zone apply their static abilities and trigger their
triggered abilities (CR 314.4), and implement abandoning to the bottom of the scheme deck (CR 701.33b).
Start with FearMyAuthorityTest, then ask me to run the full suite.
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
