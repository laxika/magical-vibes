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
- `GiltLeafAmbushTest` (clash placements)

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

## 10. Batched simultaneous entry for "any number" puts and multi-card returns

**Status:** open · **Found:** 2026-10-14, while fixing G-card tests

**Failing tests:**
- `GhaltaStampedeTyrantTest` "Creatures entering together see each other's entry regardless of selection order"
- `GishathSunsAvatarTest.selectedDinosaursEnterSimultaneously()`
- `GenesisWaveTest` / `GenesisUltimatumTest` (Aura chosen together with other permanents gets no host choice)
- `GruesomeMenagerieTest` (three creatures chosen first, then returned together)
- `GraveSifterTest` (cards returned to hand one at a time, one trigger per card)

**What the rules require.** CR 603.6a: when one event puts several permanents onto the battlefield, all
of them (including the newcomers) are checked for enters triggers, so each sees the others enter. An Aura
entering this way still has its controller choose what it enchants (CR 303.4f). The engine's "any number"
put (`PutCardToBattlefieldEffect.anyNumber`, `CardChoiceHandlerService` repeat loop) and several
reveal/return flows place cards one at a time as they are picked.

**Why it's large.** Each flow is a separate interaction chain. They need a "collect all picks, then place
together" stage like `BattlefieldEntryBatchSupport` / `WorldsWithinWorldsSupport.finish()`, including Aura
host selection inside a batch and a hand-return batch for "one or more cards leave" triggers.

**Prompt to start it:**

```
Make "put any number of cards onto the battlefield" and multi-card reveal/return effects place all chosen
cards in one simultaneous event (CR 603.6a), with Aura host choices inside the batch (CR 303.4f). Start
with GhaltaStampedeTyrantTest, GishathSunsAvatarTest, GenesisWaveTest, GenesisUltimatumTest,
GruesomeMenagerieTest and GraveSifterTest, then ask me to run the full suite.
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
