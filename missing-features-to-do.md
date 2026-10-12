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
