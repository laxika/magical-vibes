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
has left or is no longer a battle, creature or planeswalker does nothing (CR 614.9). Guards and
"dealt to this creature instead" absorbers (Kjeldoran Royal Guard, Palisade Giant, Pariah) are one
redirect option per absorber instead of routing combat damage before the pool. The
original failing tests (`DelayingShieldTest.controllerChoosesWhichShieldReplacesDamage`,
`DeepWaterTest` "The mana recipient chooses the order of competing mana replacements") pass.

**Still open.**
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

## 6. Conjure triggers for non-spellbook conjure effects

**Status:** open · **Found:** 2026-10-12, while adding spellbook conjure triggers

**Failing tests:**
- None yet. Add a Thayan Evokers (`ON_CONTROLLER_CONJURES`) test per handler family as it is fixed.

**What's missing.** "Whenever you conjure one or more cards" abilities (Thayan Evokers, Crescendo
Conductor) only trigger from `ConjureCardToHandEffectHandler`, `ConjureCardToBattlefieldEffectHandler`
and the spellbook conjure handlers. About 60 other conjure handlers in `service/effect/normalfx`
never call `TriggerCollectionService.checkConjureTriggers`: duplicates (`ConjureDuplicateOf*`,
`ConjureDuplicatesOf*`, `Choose*AndConjureDuplicate*`, `*ThenConjureDuplicate*`), named and random
cards (`ConjureCardNamed*`, `ConjureCardInto*`, `ConjureCardOnto*`, `ConjureCardTo{Graveyard,
OpponentBattlefield}`, `ConjureCards{ToBattlefield,IntoTopOfLibraries,AttachedToOpposingCreatures}`,
`ConjureRandomCreature*`, `ConjureRandomCardFromTargetPlayerLibrary`, `ConjurePowerNineIntoLibrary`),
and combined effects (`EachOpponentSacrifices…ConjuresDuplicates`, `EachPlayerDiscardsHand…Conjures…`,
`ExileAnotherCreatureAndConjureRandomCreature`, `PayXManaConjure…AndCloak`,
`SeekLibraryAndConjureDuplicatesInGraveyard`, `ReturnCardAndConjureOpponentGraveyardDuplicate`,
`TargetPlayerSacrifices…ThenConjuresDuplicate`). The rule is one trigger check per conjure event,
whatever the destination, passing the number of cards conjured in that event.

**Why it's large.** Many handlers, and the conjuring player isn't always the effect's controller:
"each opponent conjures", "target player conjures" and "conjure into target player's hand" need the
right player passed to `checkConjureTriggers`. Effects that conjure for several players need one
check per player. Look up the official ruling for any wording where it's unclear who conjures.

**Prompt to start it:**

```
Implement conjure triggers for the non-spellbook conjure effects (missing-features-to-do.md,
entry 6). Every conjure handler must call TriggerCollectionService.checkConjureTriggers once per
conjure event for the player who conjures, with the number of cards conjured. Check each card's
oracle text for who conjures. Add Thayan Evokers trigger tests per handler family. Don't run the
full suite; ask me. Don't commit until I say so.
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
