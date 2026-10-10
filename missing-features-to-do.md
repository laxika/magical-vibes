# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 1. Choosing the order of competing replacement effects

**Status:** open · **Found:** 2026-10-10, while fixing D-card tests

**Failing tests:**
- `DelayingShieldTest.controllerChoosesWhichShieldReplacesDamage()`
- `DeepWaterTest` — "The mana recipient chooses the order of competing mana replacements"

**What the rules require.** CR 616.1: when two or more replacement and/or prevention effects try to
modify the same event, the affected object's controller (or the affected player) chooses which one
to apply first, then the remaining ones are re-checked. The engine instead applies a fixed
precedence: for damage it takes the first applicable shield (Delaying Shield vs. another Delaying
Shield / prevention shield); for land mana `GameQueryService` silently orders Deep Water against
other mana replacements such as Infernal Darkness.

**Why it's large.** It needs a new player interaction that can pause inside the damage and mana
replacement pipelines and resume after the choice, and every replacement-effect path has to route
through it. High regression risk across damage, prevention and mana tests.

**Prompt to start it:**

```
Implement CR 616.1 replacement-effect ordering (see missing-features-to-do.md, entry 1).
Failing tests: DelayingShieldTest.controllerChoosesWhichShieldReplacesDamage and DeepWaterTest
"The mana recipient chooses the order of competing mana replacements".
1. Verify CR 616.1 and its subrules with the rules MCP get_rule tool before citing them.
2. Inventory how damage replacement/prevention and land-mana replacement are applied today
   (DamageSupport, prevention shields, GameQueryService mana replacement) and where a fixed
   precedence is used. Read agent-docs/ARCHITECTURE.md first.
3. Design one interaction that asks the affected player/controller to pick the next applicable
   replacement when more than one applies, resumes the event, and re-checks the rest. Skip the
   prompt when only one applies.
4. Make the two failing tests pass, add tests for the single-effect and three-effect cases, and
   list every shared engine file changed so I can run a broad test pass.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 2. Lifelink for simultaneous damage as a single life-gain event

**Status:** open · **Found:** 2026-10-10, while fixing D-card tests

**Failing tests:**
- `DelayedBlastFireballTest.lifelinkDamageToPlayerAndCreatureCausesOnlyOneLifeGainTrigger()`

**What the rules require.** CR 120.3f: damage dealt by a source with lifelink causes its
controller to gain that much life. CR 119.9: "whenever you gain life" means "whenever a source causes
you to gain life", so one source's simultaneous damage to several objects/players is a single
life-gain event and triggers once (CR 702.15e: only *multiple* lifelink sources cause separate
events). The
engine applies lifelink per damage call, so a spell that damages a player and a creature at once
produces two life-gain events.

**Why it's large.** It needs simultaneous damage from one source to be batched into a single event
across every damage path (spells, abilities, combat), and life-gain triggers to fire per batch.
Touches damage handling engine-wide; lifelink and life-gain trigger tests everywhere may change.

**Prompt to start it:**

```
Implement batched lifelink for simultaneous damage (see missing-features-to-do.md, entry 2).
Failing test: DelayedBlastFireballTest.lifelinkDamageToPlayerAndCreatureCausesOnlyOneLifeGainTrigger.
1. Re-verify CR 120.3f, 119.9 and 702.15e with the rules MCP get_rule tool before citing them.
2. Inventory every place lifelink life gain is applied (DamageSupport, combat damage, damage
   effect handlers) and how "whenever you gain life" triggers are collected. Combat damage may
   already batch; reuse that if so. Read agent-docs/ARCHITECTURE.md first.
3. Make one source's simultaneous damage produce one life-gain event (one trigger), keeping
   separate sources separate.
4. Make the failing test pass, add tests for multi-target damage spells and combat, and list every
   shared engine file changed so I can run a broad test pass.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 3. Dance of Many linking to more than one token

**Status:** blocked — needs a rules decision · **Found:** 2026-10-10, while fixing D-card tests

**Failing tests:**
- `DanceOfManyTest` — "Leaving exiles every token created by a doubled copy ability"
- `DanceOfManyTest` — "Either doubled token leaving sacrifices Dance and exiles the other token"

**What's missing.** Dance of Many links itself to the token it creates through a single
`chosenPermanentId`. With a token doubler (Doubling Season) two tokens are created, but only the last
one is linked, so the tests' expected "every token" / "either token" behavior can't happen. No
official ruling was found for how the link works with multiple tokens; decide the intended behavior
(link every token created by the ability vs. only one) before implementing.

**Prompt to start it:**

```
Implement multi-token links for Dance of Many (see missing-features-to-do.md, entry 3).
Decided behavior: <fill in: every token created by the ETB ability is linked / only one>.
Failing tests: DanceOfManyTest "Leaving exiles every token created by a doubled copy ability" and
"Either doubled token leaving sacrifices Dance and exiles the other token".
1. Search for an official ruling first and verify any CR number (rules MCP get_rule).
2. Generalize the single chosenPermanentId link used by the CreateTokenCopyAndLink /
   RemoveLinkedPermanentEffect path to a set of linked permanents without breaking the other
   cards that use one link (e.g. Tyrannical Pitlord).
3. Make the failing tests pass, add a test for the undoubled case, list shared files changed.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 4. Conjure-to-battlefield (spellbook) triggers

**Status:** open · **Found:** 2026-10-10

**Failing tests:**
- `DroverOfTheSwineTest.conjuringTheChosenPigTriggersConjureAbilities()`

**What's missing.** Conjure triggers only fire from `ConjureCardToHandEffectHandler`.
`SpellbookDraftChoiceInteractionHandler` (to-battlefield path) and
`ConjureSkywriterDjinnSpellbookEffectHandler` never call `checkConjureTriggers`. Spellbook
"draft" (e.g. Garruk, Celestial Vault) is not conjure, so the draft effect needs a conjure flag
(~30 call sites) or a dedicated conjure-from-spellbook effect.

**Prompt to start it:**

```
Implement conjure triggers for conjure-from-spellbook effects (missing-features-to-do.md,
entry 4). Failing test: DroverOfTheSwineTest.conjuringTheChosenPigTriggersConjureAbilities.
Distinguish conjure from draft without breaking the ~30 DraftCardFromSpellbookEffect users,
fire checkConjureTriggers for conjured cards on every destination, and add tests. Don't run the
full suite; ask me. Don't commit until I say so.
```

---

## 5. Nested player prompts during cost payment

**Status:** open · **Found:** 2026-10-10

**Failing tests:**
- `DiamondLionTest.ordinaryManaAbilityCanActivateDuringCounterspellManaPayment()`

**What's missing.** Activating a mana ability that needs its own choice (any-color mana) while
another prompt is pending (a may-pay cost) queues the inner prompt behind the outer one, so the
inner answer is rejected. The interaction state needs to support a nested prompt that is answered
first and then returns to the outer one (e.g. mana abilities during cost payment, CR 605.3a).

**Prompt to start it:**

```
Implement nested prompts for mana abilities activated while another prompt is pending
(missing-features-to-do.md, entry 5). Failing test: DiamondLionTest
.ordinaryManaAbilityCanActivateDuringCounterspellManaPayment. Verify CR 605.3 and 601.2g-h with
the rules MCP. Keep single-prompt flows unchanged and add tests. Don't run the full suite; ask
me. Don't commit until I say so.
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
