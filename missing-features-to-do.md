# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 1. Choosing the order of competing replacement effects

**Status:** in progress (phases A–F landed) · **Found:** 2026-10-10, while fixing D-card tests

**Done.** CR 616.1 ordering runs through `ReplacementOrderService` for land mana (every land-type
replacement on both tap paths, plus Mana Reflection-style multipliers) and for damage to a player or a
permanent (every redirect, multiplier, bonus, prevention and final-stage replacement in one pool; a
prompt in the middle of an effect resolves the effect again through `DamageJournal`). Combat damage is
one event per recipient per damage step, with a portion per source. Damage multipliers and "+N"
bonuses are pool options for noncombat and combat damage (dealing effects and combat assignment pass the
unmodified amount; `DamageMultiplierRatchetTest` keeps it that way). The last fixed-chain damage paths
are gone: Megrim's discard damage and Rubble Rouser's "when you do" mana-ability rider are now triggered
abilities on the stack (CR 603.12, 605.5a), and Myr Battlesphere's attack damage goes through
`DamageSupport` to the player or planeswalker it's attacking. Discard-triggered life loss (Liliana's
Caress, Fell Specter, Raider's Wake, Midnight Oil) uses the stack too (CR 603.3), and triggers from a
mana ability completed by a cost choice reach the stack when it completes (CR 117.3c). Damage a
redirect shield takes over (Harm's Way) is the same damage continuing through the new recipient's
pool (combat damage as an event of the same damage step), with source-side effects that already applied still spent (CR 614.5); a redirect whose destination
has left or is no longer a battle, creature or planeswalker does nothing (CR 614.9). The
original failing tests (`DelayingShieldTest.controllerChoosesWhichShieldReplacesDamage`,
`DeepWaterTest` "The mana recipient chooses the order of competing mana replacements") pass.

**Still open.**
- The "+N" bonus families are one option per family (the sum of every instance), so a multiplier
  can't be applied between two bonuses of the same family; global multipliers are one option each.
- Combat: "prevent all combat damage" (Fog) and the guard/"dealt to this creature instead" routing
  apply before the pool (the rerouted damage then goes through the new recipient's pool). The all-creature-damage redirect ("have N damage dealt to you instead")
  attributes a multi-source event to its first source.
- Taps through Piracy keep the old land-mana precedence; mana previews and the AI's
  `wouldTapForManaOpenChoice` do not yet treat a contested land as opening a choice; the
  "colored mana becomes white" replacement is applied after the others instead of being ordered.
- The AI answers a replacement-order prompt with the first option instead of the least harmful one.

**Prompt to start it:**

```
Continue CR 616.1 replacement-effect ordering (see missing-features-to-do.md, entry 1, "Still open").
Read the "Replacement ordering (CR 616.1)" bullet in agent-docs/ARCHITECTURE.md first.
1. Pick the next open item, e.g. route the remaining fixed-chain damage paths through DamageSupport.
Verify every CR number with the rules MCP. List shared engine files changed after each phase.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 2. Lifelink for simultaneous damage as a single life-gain event

**Status:** done (2026-10-11, `4a109bc2cb`) · **Found:** 2026-10-10, while fixing D-card tests

**Done.** While a spell or ability resolves, `DamageSupport.checkSpellLifelink` adds each lifelink
gain to `GameData.pendingLifelinkGains`, keyed by source and gaining player. `EffectResolutionService`
applies each one as a single life-gain event when the outermost resolution ends, before the loss check.
Damage outside a resolution still gains life immediately. Combat already summed damage per creature.
The original failing test
(`DelayedBlastFireballTest.lifelinkDamageToPlayerAndCreatureCausesOnlyOneLifeGainTrigger`) passes;
`LifelinkMechanicTest` covers multi-target spells and combat.

**Known limitation.** The resolution is the batching unit, so damage from one source in genuinely
sequential steps of a single resolution ("…, then …") is also one gain. Cards write simultaneous
damage as separate steps too (Delayed Blast Fireball), so the engine can't tell the two apart.

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
