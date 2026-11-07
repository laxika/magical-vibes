#!/usr/bin/env python3
"""Step 15, run 17j: scan the combat abilities' conditions but the self-attack family's, classified by the Oracle text.

Run 17i's scan (`step15-run17i-scan.py`) widened to every combat trigger factory but `attacks` (the self-attack
family, run 17i): the other attack families, "attacks and isn't blocked", blocks, becomes blocked and combat damage.
For every triggered-ability statement whose trigger is one of them: its option conditions (IF: intervening "if", EV:
event condition) and its effects' `ConditionalEffect` conditions (CE), each intervening "if" or effect conditional
classified by the card's Oracle text as run 17i's: "IF" when every combat-trigger line has an ", if" right after its
trigger condition (CR 603.4), "EVENT" when none has, "MIXED" otherwise. Read-only.

Usage: step15-run17j-scan.py [--effects]   (--effects also lists the effect conditionals)
"""
import collections
import importlib.util
import pathlib
import re
import sys

spec = importlib.util.spec_from_file_location("scan17i", pathlib.Path(__file__).with_name("step15-run17i-scan.py"))
scan17i = importlib.util.module_from_spec(spec)
spec.loader.exec_module(scan17i)

FACTORIES = ("creatureYouControlAttacks creatureAttacks creatureAttacksYouOrYourPlaneswalker creatureAttacksYou "
             "creatureAttacksWhileYouDefend youAttack youAttackPlayer youAttackPlayerOrPlaneswalker playerAttacks "
             "creaturesAttackYou opponentAttacksYourPlaneswalker attackingCreatureYouControlAbilityTriggers "
             "attacksUnblocked enchantedCreatureAttacksUnblocked creatureYouControlAttacksUnblocked "
             "creaturesAttackYouUnblocked blocks blocksTwoOrMore creatureBlocks creaturesBlock becomesBlocked "
             "creatureYouControlBecomesBlocked creaturesYouControlBecomeBlocked creatureBecomesBlocked "
             "dealsCombatDamageToPlayer equippedCreatureDealsCombatDamageToPlayer "
             "creatureYouControlDealsCombatDamageToPlayer creatureDealsCombatDamageToPlayer "
             "creatureWithSameNameDealsCombatDamageToPlayer creatureDealsCombatDamageToOpponent "
             "goadedCreaturesDealCombatDamageToOpponent creatureDealsCombatDamageToItsOwner dealsCombatDamageToCreature "
             "dealsCombatDamageToBlockingCreature dealsCombatDamage equippedCreatureDealsCombatDamage "
             "creatureYouControlDealsCombatDamage dealsCombatDamageToPlayerOrPlaneswalker "
             "creatureYouControlDealsCombatDamageToPlayerOrPlaneswalker dealsCombatDamageToPlayerOrBattle "
             "equippedCreatureDealsCombatDamageToPlayerOrBattle creatureYouControlDealsCombatDamageToPlayerOrBattle "
             "creatureDealsCombatDamageToYou dealtCombatDamage youAreDealtCombatDamage "
             "youOrYourPlaneswalkerDealtCombatDamage").split()

scan17i.FACTORIES = FACTORIES

TRIGGER_LINE = re.compile(r"\b(When|Whenever)\b[^.]*?\b(attack|block|combat damage)", re.I)
INTERVENING = re.compile(r"\b(When|Whenever)\b[^,.]*?\b(attack|block|combat damage)[^,.]*?, if\b", re.I)


def classify(path):
    lines = [line for line in re.split(r"\n|•", scan17i.oracle_named(path) or "") if TRIGGER_LINE.search(line)]
    count = sum(bool(INTERVENING.search(line)) for line in lines)
    return "IF" if lines and count == len(lines) else "EVENT" if lines and count == 0 else "MIXED"


def main():
    with_effects = "--effects" in sys.argv
    result = collections.defaultdict(lambda: collections.defaultdict(set))
    for path, trigger, options, effects in scan17i.abilities():
        kind = classify(path)
        for option in options:
            if option.startswith("IF:"):
                result[(kind, trigger, "IF")][option[3:]].add(path.stem)
        if with_effects:
            for condition in effects:
                result[(kind, trigger, "CE")][condition].add(path.stem)
    for (kind, trigger, form), conditions in sorted(result.items()):
        print(f"## {kind} {trigger} {form}")
        for condition, cards in sorted(conditions.items()):
            print(f"   {condition}: {' '.join(sorted(cards))}")


if __name__ == "__main__":
    main()
