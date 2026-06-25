# Refactor prompt: Resolve first-strike and regular combat damage as separate steps

## Goal

Make combat damage rules-correct (CR 510.4) by resolving the **first-strike combat
damage step** and the **regular combat damage step** as two distinct steps, with the
triggered abilities from the first step being put on the stack and resolved (with the
normal priority windows) **before** the regular combat damage step happens.

Today the engine collapses both into a single resolution pass and only collects/fires
combat-damage triggers once, after all damage has been dealt. This is incorrect for any
first-strike / double-strike creature whose power/toughness or board state changes between
the two steps.

## Concrete failing case (reference card)

`Markov Blademaster` (DKA 96): a 1/1 with **double strike** and
"Whenever this creature deals combat damage to a player, put a +1/+1 counter on it."

When it attacks unblocked:

| Behavior            | Current engine | Rules-correct |
|---------------------|----------------|---------------|
| +1/+1 counters      | 1              | **2** (trigger fires once per damage step) |
| Damage to defender  | 1 + 1 = 2      | **3** (1 from first strike, then 2 from regular because the first-strike counter has already resolved, making it 2/2) |

The card class itself is correct and needs no changes:
`magical-vibes-card/.../cards/m/MarkovBlademaster.java` —
`addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutCountersOnSourceEffect(1, 1, 1))`.

There is a `@Disabled` test capturing the rules-correct expectation:
`magical-vibes-backend/.../cards/m/MarkovBlademasterTest.java#doubleStrikeIsRulesCorrect()`.
**Re-enable it (remove `@Disabled`) when this refactor is done, and update/remove the
`dealingCombatDamageGivesCounter()` test that currently asserts the buggy 1-counter / 2-damage
behavior.**

## Where the problem lives

`magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/combat/CombatDamageService.java`

In `resolveCombatDamage(...)` (around lines 104–263):

1. Phase 1 (first strike) is resolved at lines ~148–151 via `resolveDamagePhase(..., true)`.
2. Phase 2 (regular) is resolved at lines ~180–181 via `resolveDamagePhase(..., false)`.
3. **All** combat-damage triggers are collected/queued only after BOTH phases:
   - `processCombatDamageToCreatureTriggers` (line ~218)
   - `processDealtDamageTriggers` (line ~221)
   - `processCombatDamageToPlayerTriggers` (line ~236)
   - defender-side / loot / opponent-creature triggers (lines ~223–246)

The root cause for Markov Blademaster specifically: `state.combatDamageDealtToPlayer` is a
`Map<Permanent, Integer>` that **accumulates the total** damage a creature dealt across both
phases, and `processCombatDamageToPlayerTriggers` (line ~597) iterates once per creature, so
the trigger fires once even though damage was dealt in two separate steps.

There is already a re-entry mechanism (`gameData.combatDamagePhase1Complete`,
`combatDamagePhase1State`, `combatDamagePendingIndices`, `savePhase1State`/`restorePhase1State`)
used to pause between phases for manual damage assignment. That state machine is the natural
seam to hang the new "resolve first-strike triggers, then continue to regular damage" behavior on.

## Required behavior

1. If any combat participant has first strike or double strike, run the first-strike damage
   step. After dealing that damage:
   - Apply life loss / lifelink / marked damage / SBA death removal for the first-strike step.
   - Collect and queue ALL triggers caused by first-strike damage (to-player, to-creature,
     dealt-damage, defender-side, etc.), reordered APNAP.
   - Hand priority back so those triggers resolve normally (counters get placed, etc.) BEFORE
     the regular damage step. Reuse the existing `CombatResult.AUTO_PASS_RESOLVE_COMBAT_TRIGGERS`
     / re-entry flow rather than inventing a new control path if possible.
2. Then run the regular combat damage step using the **current** (possibly modified) power/
   toughness and board state, and collect/resolve its triggers the same way.
3. Creatures with first strike but **not** double strike must still deal damage only in the
   first step (existing `bAtkSkipPhase2` logic at lines ~160–162 must be preserved).
4. Creatures with neither keyword deal damage only in the regular step (current behavior).
5. Damage within a single step remains simultaneous (do not break existing simultaneous-damage
   semantics, deathtouch, trample assignment, redirects, infect conversion at line ~187, etc.).

## Things to watch / regression surface

- This touches **every** first-strike and double-strike interaction. Run the full backend test
  suite (`./gradlew :magical-vibes-backend:test`, >20 min — run in background) and check the many
  existing first-strike/double-strike card tests (e.g. `AbattoirGhoulTest`, `BaneslayerAngelTest`,
  `MirranCrusaderTest`, `WhiteKnightTest`, `PaladinEnVecTest`, `SpiralingDuelistTest`,
  `GoringCeratopsTest`, `TrueConvictionTest`, and `CombatDamageServiceTest`).
- Several existing card tests pace combat with a fixed number of `passBothPriorities()` calls
  assuming a single damage/trigger resolution. Adding a real priority window between the two
  damage steps may change how many passes are needed — audit and update those tests.
- Watch the manual-damage-assignment re-entry path (`handleCombatDamageAssigned`,
  `combatDamagePendingIndices`) so it still works when interleaved with first-strike trigger
  resolution.
- `combatDamageToPlayersThisTurn` and any "did this creature deal combat damage this turn"
  bookkeeping should reflect both steps correctly.
- Lifelink, "gain life equal to damage dealt", `ON_DEALT_DAMAGE`, `ON_DAMAGED_CREATURE_DIES`,
  and planeswalker damage must all fire per-step, not once for the combined total.

## Definition of done

- `MarkovBlademasterTest#doubleStrikeIsRulesCorrect()` passes with `@Disabled` removed
  (2 counters, defender at 17), and the interim `dealingCombatDamageGivesCounter()` test is
  removed or updated.
- Full backend test suite is green.
- Update the agent-docs if the combat-damage flow / EffectSlot timing notes change
  (e.g. `agent-docs/TRIGGER_SLOT_TARGETING.md`, `ACTIVATED_ABILITY_GUIDE.md`).
