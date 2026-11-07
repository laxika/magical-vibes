#!/usr/bin/env python3
"""Step 15, run 17j: the agent-docs and the implement-card skill show the "you attack" family's new forms.

A condition of the "whenever you attack" / "whenever you attack a player (or planeswalker)" trigger event is the
ability's event condition (`withEventCondition`), and "whenever one or more [filter] you control attack" is
`Triggers.youAttack().withFilter(filter)` ("that many" is the number of matching attackers, the event value). Exact
edits; fails if an anchor is missing. Idempotent: an edit already made is skipped.

Usage: step15-run17j-docs.py
"""
import pathlib
import sys

EDITS = [
    ("agent-docs/ACTIVATED_ABILITY_GUIDE.md",
     "The attacker count is passed via xValue. Supports `ConditionalEffect(new MinimumAttackers(minimumAttackers), "
     "wrapped)` wrapper for \"N or more creatures\" conditions. Used by Warcry Phoenix |",
     "The attacker count is passed via xValue. \"Whenever you attack with N or more creatures\" is "
     "`TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(N))`, part of the trigger event (CR 603.4: not an "
     "intervening \"if\"). Used by Warcry Phoenix |"),
    ("agent-docs/ACTIVATED_ABILITY_GUIDE.md",
     "The attacked player is captured as the trigger's non-targeting `attackedTargetId`. Checked in "
     "`CombatAttackService.declareAttackers`. Used by Bitter Work and Karazikar, the Eye Tyrant |",
     "The attacked player is captured as the trigger's non-targeting `attackedTargetId`. \"With one or more creatures "
     "with power 4 or greater\" is the ability's event condition (`withEventCondition(new MinimumMatchingAttackers(1, "
     "…))`, `HasAttacker`), judged against the creatures attacking that player, with their number as the X. Checked in "
     "`CombatAttackService.declareAttackers`. Used by Bitter Work and Karazikar, the Eye Tyrant |"),
    ("agent-docs/CARD_IMPLEMENTATION_PLAYBOOK.md",
     "| `ConditionalEffect(new MinimumAttackers(minimumAttackers), wrapped)` | - | yes | yes (attack) |",
     "| `TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(minimumAttackers))` | - | - | yes (attack) | "
     "\"with N or more creatures\", \"this and at least N-1 other creatures\": the trigger event's attacker count |"),
    ("agent-docs/CARD_IMPLEMENTATION_PLAYBOOK.md",
     "| `ConditionalEffect(new HasAttacker(predicate), wrapped)` | - | yes | yes (attack) |",
     "| `TriggerOptions.DEFAULT.withEventCondition(new HasAttacker(predicate))` | - | yes | yes (attack) | \"whenever you "
     "attack with your commander\"; a `DeclaredAttackCondition`, so as an intervening \"if\" (\"if a Pirate and a Vehicle "
     "attacked this combat\") its wrapper comes off once judged. \"Whenever one or more [filter] you control attack\" is "
     "`Triggers.youAttack().withFilter(predicate)` instead |"),
    ("agent-docs/CARD_PATTERN_INDEX.md",
     "`addTrigger(Triggers.youAttack(), TriggerOptions.DEFAULT.withInterveningIf(new MinimumAttackers(3))"
     ".withLimit(TriggerLimit.ONCE)",
     "`addTrigger(Triggers.youAttack(), TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(3))"
     ".withLimit(TriggerLimit.ONCE)"),
    ("agent-docs/CARD_PATTERN_INDEX.md",
     "`Triggers.youAttackPlayer() ConditionalEffect(HasAttacker(token), "
     "ExileTopCardMayPlayWhileExiledEffect(AttackedWithTokenThisTurn))`",
     "`Triggers.youAttackPlayer()` + `withEventCondition(new HasAttacker(token))`, "
     "`ExileTopCardMayPlayWhileExiledEffect(AttackedWithTokenThisTurn)`"),
    ("agent-docs/CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md",
     "Triggers.youAttack() with ConditionalEffect(new MinimumAttackers(3), TransformSelfEffect()).",
     "Triggers.youAttack() with `TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(3))`, "
     "TransformSelfEffect()."),
    ("agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "Triggers.youAttackPlayer() `ConditionalEffect(MinimumMatchingAttackers(1, AllOf(IsCreature, PowerAtLeast(4))), "
     "DrawCardEffect())`",
     "Triggers.youAttackPlayer() + `withEventCondition(new MinimumMatchingAttackers(1, AllOf(IsCreature, "
     "PowerAtLeast(4))))`, `DrawCardEffect()`"),
    ("agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "Triggers.youAttack() from the graveyard ConditionalEffect(new MinimumAttackers(3), MayPayManaEffect(\"{2}{R}\", "
     "ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(CardIsSelfPredicate).returnAll(true)"
     ".enterTapped(true).enterAttacking(true).build()))",
     "Triggers.youAttack() from the graveyard + `withEventCondition(new MinimumAttackers(3))`, "
     "MayPayManaEffect(\"{2}{R}\", ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD)"
     ".filter(CardIsSelfPredicate).returnAll(true).enterTapped(true).enterAttacking(true).build())"),
    ("agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "Triggers.youAttack() `ConditionalEffect(HasAttacker(BIRD), LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect"
     "(PermanentCount(attacking BIRDS, CONTROLLER)))`",
     "`Triggers.youAttack().withFilter(PermanentHasSubtypePredicate(BIRD))`, "
     "`LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect(new EventValue())` (\"that many\": the matching "
     "attackers)"),
    ("agent-docs/EFFECTS_INDEX.md",
     "| `ConditionalEffect(new MinimumAttackingCreaturesOfSubtype(minimum, subtype), wrapped)` | see construction shape | "
     "Attack-group trigger condition: \"whenever you attack with N or more [subtype]\". Counts the controller's current "
     "attacking creatures matching the subtype (including Changeling), filters at `Triggers.youAttack()` collection time, "
     "and unwraps the effect so the event qualification is not re-evaluated after attackers are removed. |",
     "| `TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackingCreaturesOfSubtype(minimum, subtype))` | see "
     "construction shape | \"Whenever you attack with N or more [subtype]\": the trigger event (CR 603.4). Counts the "
     "controller's attacking creatures matching the subtype (including Changeling) as the attackers are declared; not "
     "checked again as the ability resolves. |"),
    ("agent-docs/EFFECTS_INDEX.md",
     "| `ConditionalEffect(new HasAttacker(predicate), wrapped)` | see construction shape | Trigger-condition wrapper: "
     "\"whenever one or more matching permanents you control attack\". Filtered at trigger collection time in "
     "CombatAttackService (only fires if at least one attacking permanent matches the predicate) and re-checked at "
     "resolution time. Place in Triggers.youAttack() ability.",
     "| `TriggerOptions.DEFAULT.withEventCondition(new HasAttacker(predicate))` | see construction shape | \"Whenever you "
     "attack with your commander\" / \"with one or more matching permanents\": the trigger event, judged as the attackers "
     "are declared and not checked again (a `DeclaredAttackCondition`). When \"that many\" / \"they\" refer to the "
     "matching attackers, write the trigger's filter instead: `Triggers.youAttack().withFilter(predicate)` (the event "
     "value is their number)."),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md",
     "- `ConditionalEffect(new HasAttacker(predicate), wrapped)` — one or more matching attackers",
     "- `HasAttacker(predicate)` — one or more matching attackers; a \"you attack\" trigger's event condition "
     "(`withEventCondition`) or, for \"whenever one or more [filter] you control attack\", the trigger's filter "
     "`Triggers.youAttack().withFilter(predicate)` (\"that many\" is the event value)"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md",
     "- `DeclaredAttackCondition` — marker of a condition about the attack as declared "
     "(`AttackingCreaturesTotalPowerAtLeast`, `ExactlyAttackers`): as a \"whenever this creature attacks\" ability's "
     "intervening \"if\"",
     "- `DeclaredAttackCondition` — marker of a condition about the attack as declared "
     "(`AttackingCreaturesTotalPowerAtLeast`, `ExactlyAttackers`, `HasAttacker`; `isDeclaredAttackFact` also takes an "
     "`AllOf` / `AllConditions` of them): as an attack ability's intervening \"if\""),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
     "| `Triggers.youAttack() ConditionalEffect(MinimumMatchingAttackers(1, PermanentAllOfPredicate(PermanentHasKeywordPredicate"
     "(FLYING), PermanentNotPredicate(PermanentIsSourcePermanentPredicate))), effect using EventValue)`; the +2/+2 face is "
     "`Triggers.youAttack().withFilter(that predicate)` with",
     "| `Triggers.youAttack().withFilter(PermanentAllOfPredicate(PermanentHasKeywordPredicate(FLYING), "
     "PermanentNotPredicate(PermanentIsSourcePermanentPredicate)))`, the effect using `EventValue` (the number of "
     "matching attackers); the +2/+2 face is the same trigger with"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
     "`Triggers.youAttackPlayer() ConditionalEffect(HasAttacker(token), ExileTopCardMayPlayWhileExiledEffect"
     "(AttackedWithTokenThisTurn))`",
     "`Triggers.youAttackPlayer()` + `withEventCondition(new HasAttacker(token))`, "
     "`ExileTopCardMayPlayWhileExiledEffect(AttackedWithTokenThisTurn)`"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever you attack a player with one or more creatures with power 4 or greater, draw a card\" | "
     "`ConditionalEffect(MinimumMatchingAttackers(1, PermanentAllOfPredicate(IsCreature, PowerAtLeast(4))), "
     "DrawCardEffect())` |",
     "| \"Whenever you attack a player with one or more creatures with power 4 or greater, draw a card\" | "
     "`TriggerOptions.DEFAULT.withEventCondition(new MinimumMatchingAttackers(1, PermanentAllOfPredicate(IsCreature, "
     "PowerAtLeast(4))))`, `DrawCardEffect()` |"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
     "| `ConditionalEffect(new MinimumAttackers(3), new MayEffect(new DiscardAndDrawCardEffect(1, 2, true), prompt))` | "
     "Triggers.youAttack() |",
     "| `TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(3))`, `new MayEffect(new "
     "DiscardAndDrawCardEffect(1, 2, true), prompt)` | Triggers.youAttack() |"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
     "| `ConditionalEffect(HasAttacker(PermanentHasSubtypePredicate(BIRD)), LookAtTopCardsChooseOneToHandThenLandsTo"
     "BattlefieldTappedEffect(PermanentCount(attacking BIRDS, CONTROLLER)))` | Triggers.youAttack() | Choco, Seeker of "
     "Paradise (FIN). The once-per-combat trigger is filtered by `HasAttacker`; the effect's count is the number of "
     "attacking Birds, not the total number of attackers. |",
     "| `LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect(new EventValue())` | "
     "`Triggers.youAttack().withFilter(PermanentHasSubtypePredicate(BIRD))` | Choco, Seeker of Paradise (FIN). The "
     "once-per-combat trigger is filtered to Birds; \"that many\" is the event value, the number of attacking Birds, not "
     "the total number of attackers. |"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
     "`Triggers.youAttackPlayer()` + `ConditionalEffect(HasAttacker(PermanentIsEquippedPredicate), DrawCardEffect())` | "
     "trigger | Akiri, Fearless Voyager. The player attack slot fires once per directly attacked player and the "
     "condition checks the attacking group at trigger time. |",
     "`Triggers.youAttackPlayer()` + `withEventCondition(new HasAttacker(PermanentIsEquippedPredicate))`, "
     "`DrawCardEffect()` | trigger | Akiri, Fearless Voyager. The trigger fires once per directly attacked player and "
     "the event condition checks the creatures attacking that player as they are declared. |"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
     "`Triggers.youAttack() ConditionalEffect(MinimumAttackers(2), PutCountersOnSelfThenReflexiveEffect(",
     "`Triggers.youAttack()` + `withEventCondition(new MinimumAttackers(2))`, `PutCountersOnSelfThenReflexiveEffect("),
    (".claude/skills/implement-card/SKILL.md",
     "\"attacks a battle / an opponent / the monarch\", \"whenever all non-Wall creatures you control attack\" — is "
     "`TriggerOptions.DEFAULT.withEventCondition(condition)`,",
     "\"attacks a battle / an opponent / the monarch\", \"whenever all non-Wall creatures you control attack\", "
     "\"whenever you attack a player with one or more equipped creatures\" — is "
     "`TriggerOptions.DEFAULT.withEventCondition(condition)`; \"whenever one or more Dragons you control attack\" / "
     "\"whenever you attack with one or more Zombies\" is the trigger's filter, "
     "`Triggers.youAttack().withFilter(predicate)` (\"that many\" is `new EventValue()`, the number of matching "
     "attackers);"),
]


def main():
    failed = False
    for path, before, after in EDITS:
        file = pathlib.Path(path)
        text = file.read_text()
        if after in text:
            continue
        if text.count(before) != 1:
            print(f"{path}: anchor found {text.count(before)} times: {before[:90]}")
            failed = True
            continue
        file.write_text(text.replace(before, after))
        print(f"{path}: edited")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
