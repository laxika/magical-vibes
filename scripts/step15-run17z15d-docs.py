#!/usr/bin/env python3
"""Step 15, run 17z15d: the agent docs say what the limits, the trigger record and the combat collectors do now.

- `TriggerLimit.FIRST_TIME_EACH_TURN` on `Triggers.attacks()` ("attacks for the first time each turn"), in place of
  `ONCE_EACH_TURN` and the per-attacker mark `GameData.onceEachTurnAttackTriggersFiredThisTurn`.
- Every limit is marked for the ability (`TriggeredAbility.limitKey()`), "do this only once each turn" included: the
  stack entry carries the key (`StackEntry.onceEachTurnActionKey`).
- `GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect` is gone (no card wrote it).
- Komainu Battle Armor is one ability of two trigger conditions.
- The trigger record is carried by every target walk and division too.

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z15d-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("with `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` | Triggers.attacks() | Aurelia, the "
         "Warleader (GTC). Flying/vigilance/haste are auto-loaded. The limit is judged at `Triggers.attacks()` "
         "collection at most once per turn per permanent (`GameData.onceEachTurnAttackTriggersFiredThisTurn`), so "
         "attacking again in the extra combat phase it grants can't loop.",
         "with `TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN)` | Triggers.attacks() | Aurelia, "
         "the Warleader (GTC). Flying/vigilance/haste are auto-loaded. \"For the first time each turn\" is part of the "
         "event: the attack collector reads it from the creature's attacks this turn, whatever the ability's \"if\" "
         "then says (Scourge of the Throne, Fear of Missing Out), so attacking again in the extra combat phase it "
         "grants can't loop. `ONCE_EACH_TURN` on an attack ability is \"this ability triggers only once each turn\", "
         "marked for that ability as it triggers."),
        ("| `Triggers.dealsCombatDamageToPlayer()` + `Triggers.equippedCreatureDealsCombatDamageToPlayer()`, both "
         "with `GoadCreaturesDamagedPlayerControlsUntilNextTurnEffect()` |",
         "| `addTrigger(List.of(Triggers.dealsCombatDamageToPlayer(), "
         "Triggers.equippedCreatureDealsCombatDamageToPlayer()), "
         "new GoadCreaturesDamagedPlayerControlsUntilNextTurnEffect())`: one ability of two trigger conditions |"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("\"Attacks for the first time each turn\" is `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` "
         "(Aurelia, the Warleader) — at most once per turn per permanent |",
         "\"Attacks for the first time each turn\" is "
         "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN)` (Aurelia, the Warleader): the "
         "creature's first attack of the turn, whatever the ability's conditions then say; `ONCE_EACH_TURN` is \"this "
         "ability triggers only once each turn\", marked for that ability as it triggers |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("read by the token-creation (Akim, the Soaring Wind), land-to-graveyard (Crawling Sensation) and \"whenever "
         "you attack\" (Desert Were-Worm, with `withEventCondition(new AttackingCreaturesTotalPowerAtLeast(12))`) "
         "collectors.",
         "read by the token-creation (Akim, the Soaring Wind), land-to-graveyard (Crawling Sensation), \"whenever "
         "you attack\" (Desert Were-Worm, with `withEventCondition(new AttackingCreaturesTotalPowerAtLeast(12))`) "
         "and \"whenever this creature attacks\" (Aurelia, the Warleader: its first attack of the turn) collectors. "
         "Every limit is the ability's own, marked by `TriggeredAbility.limitKey()` on its source: two limited "
         "abilities of one permanent each keep theirs."),
    ],
    "EFFECTS_INDEX.md": [
        ("| `GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect` | `()` | until end of turn, creatures you "
         "control gain \"Whenever this creature deals damage to an opponent, you may return target creature that "
         "player controls to its owner's hand.\" Sets `hasDamageToOpponentCreatureBounce` flag on controlled "
         "creatures; CombatService creates `ReturnPermanentsOnCombatDamageToPlayerEffect(PermanentIsCreaturePredicate)` "
         "trigger with xValue=1. Cleared on `resetModifiers()`. Used by Arm with Aether |",
         "| `GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect` | — | **Gone** (effect-slot-refactor.md step "
         "15, run 17z15d; no card wrote it): Arm with Aether grants a real ability, "
         "`GrantEffectToOwnCreaturesUntilEndOfTurnEffect(new TriggeredAbility(Triggers.dealsDamageToOpponent(), "
         "new MayEffect(ReturnToHandEffect.target(creature that player controls), …)))`, which the damage collectors "
         "read as any ability a creature has until end of turn |"),
    ],
    "CARD_PATTERNS_LANDS_SPELLS.md": [
        ("| Grant triggered bounce | `a/ArmWithAether.java` | GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect "
         "— grants \"damage to opponent, may bounce creature\" to all controlled creatures until end of turn |",
         "| Grant triggered bounce | `a/ArmWithAether.java` | "
         "`GrantEffectToOwnCreaturesUntilEndOfTurnEffect(new TriggeredAbility(Triggers.dealsDamageToOpponent(), "
         "new MayEffect(ReturnToHandEffect.target(…))))` — grants \"damage to opponent, may bounce creature\" to all "
         "controlled creatures until end of turn |"),
    ],
    "ARCHITECTURE.md": [
        ("gives it to the choice as its `triggerRecord` (`TriggeredModalTrigger`, `SpellGraveyardTargetTrigger`, "
         "`AttackTriggerTarget`, `GraveyardTargetOperationState`; `withTriggerRecord`),",
         "gives it to the choice as its `triggerRecord` (`TriggeredModalTrigger`, `SpellGraveyardTargetTrigger`, "
         "`AttackTriggerTarget`, `EntersTriggerTarget`, `ETBSpellTargetTrigger`, the group-by-group walk "
         "`ETBTokenMultiTargetTrigger` (`withProgress` keeps it from step to step) and its "
         "`CounterDistributionAssignment` division, `GraveyardTargetOperationState`; `withTriggerRecord`),"),
        ("Don't hand-copy single fields into a choice context for a new route.",
         "Don't hand-copy single fields into a choice context for a new route. A block-step ability that chooses no "
         "target goes on the stack through `CombatTriggerService.putOnStackOrChooseMode`, which routes a mode chosen "
         "as the ability triggers to its choice with the entry as the record. Limits are the ability's own "
         "(run 17z15d): \"triggers only once each turn\", \"triggers only once\", the first-time and \"one or more\" "
         "marks and \"do this only once each turn\" are all kept under `TriggeredAbility.limitKey()` for the source "
         "(`GameData.keyedOncePerTurnTriggersFiredThisTurn` / `keyedOnceOnlyTriggersFired`); the last is marked as "
         "the action is taken, from the key the stack entry carries (`StackEntry.onceEachTurnActionKey`, "
         "`TriggeredAbilityStackService.markActionTaken`). Don't add a per-permanent limit set or flag."),
    ],
}


def main() -> int:
    failures = []
    changed = 0
    for name, replacements in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        original = text
        for old, new in replacements:
            if new in text:
                continue
            if text.count(old) != 1:
                failures.append(f"{name}: {old[:60]!r} occurs {text.count(old)} times")
                continue
            text = text.replace(old, new)
        if text != original:
            path.write_text(text)
            changed += 1
    for failure in failures:
        print("FAILED", failure)
    print(f"{changed} doc files changed")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
