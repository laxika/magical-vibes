# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 1. Choosing the order of competing replacement effects

**Status:** done (2026-10-12) · **Found:** 2026-10-10, while fixing D-card tests

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
Piracy taps, mana previews, the AI's mana planning, "the mana a land could produce" (CR 106.7), Pygmy
Hippo's drain, False Dawn and Quarum Trench Gnomes all go through the land-mana pool, and the AI orders
replacements by each option's preview (least damage, most mana).

---

## 2. The caster keeps priority after casting

**Status:** open · **Found:** 2026-10-12, while fixing E-card tests

**Failing tests:**
- `ExquisiteFirecraftTest` (non-active caster keeps priority after casting a counterspell)
- `ElectrosiphonTest` (illegal-target case)
- `ExtinguishTest` (priority after player two casts)
- `EnvelopTest` (player two passes after casting)

**What the rules require.** CR 117.3c: "If a player has priority when they cast a spell, activate an
ability, or take a special action, that player receives priority afterward." The engine doesn't store
who holds priority. `GameQueryService.getPriorityPlayerId` works it out from `priorityPassedBy`
(the active player unless they passed, then the other player). Casting clears `priorityPassedBy`,
so after the non-active player casts, priority always goes back to the active player.

**Why it's large.** Priority would need an explicit holder that casting, activating, special actions,
resolution (CR 117.3b) and step changes all update. The harness helpers (`passBothPriorities`,
`passUntil`, auto-pass), the AI and the networking priority view all assume the derived model.
Thousands of tests rely on it, so it needs a full-suite run.

**Prompt to start it:**

```
Make the engine track the priority holder explicitly so that a player who casts a spell, activates
an ability or takes a special action while holding priority receives priority afterward (CR 117.3c),
while the active player still receives priority after resolution (CR 117.3b). Verify rule numbers
with the rules MCP. Today GameQueryService.getPriorityPlayerId derives priority from priorityPassedBy.
Update the auto-pass, harness and AI paths. Start with ExquisiteFirecraftTest, ElectrosiphonTest and
ExtinguishTest, then ask me to run the full suite.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 3. Choosing ETB trigger targets when the trigger goes on the stack

**Status:** open · **Found:** 2026-10-12, while fixing E-card tests

**Failing tests:**
- `EarthKingdomSoldierTest` (7 tests: "up to two" ETB targets after a cast with none chosen)
- `EnduranceTest` (ETB target prompt, evoke)
- `ExpeditionRaptorTest` (support targets after entering)

**What the rules require.** A permanent spell's ETB triggered ability chooses its targets when the
trigger is put on the stack (CR 603.3d), not while the permanent spell is cast. The engine lets the
caster pick a cast permanent's ETB targets during casting (`target(...)` groups on the card) and carries
them to the trigger. `EtbTriggerService` then skips an "up to N" ETB trigger entirely when zero
targets were picked at cast (`castChoseZeroOptionalTargets`), so the controller never gets the
trigger-time prompt the tests expect. Targets picked at cast can also be stale by the time the
trigger exists (the spell can be responded to).

**Why it's large.** Every creature, artifact and enchantment card with a targeted ETB and every test
that passes ETB targets to `castCreature` relies on cast-time selection. Removing the shortcut in one
place (tried during this fix run) broke `EnnisDebateModeratorTest` and was rolled back to an X-only
exception. A correct fix moves ETB target selection for cast permanents to trigger time across the
cast, harness and AI paths, and needs a full-suite run.

**Prompt to start it:**

```
Move ETB triggered-ability target selection for cast permanents from cast time to when the trigger is
put on the stack (CR 603.3d — verify with the rules MCP). Today cards declare ETB targets with target(...)
groups that SpellCastingService fills during casting, and EtbTriggerService reuses them or skips "up to N"
triggers with no cast-time targets (castChoseZeroOptionalTargets). Start with EarthKingdomSoldierTest,
EnduranceTest and ExpeditionRaptorTest, keep EnnisDebateModeratorTest and ExtraordinaryJourneyTest green,
update the harness/AI cast paths, then ask me to run the full suite.
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
