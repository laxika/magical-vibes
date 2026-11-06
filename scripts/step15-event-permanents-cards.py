#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 9: "they", "them" and "each of them" read the stack entry.

Idempotent; run once per letter module:

    for m in magical-vibes-card/*/; do python3 scripts/step15-event-permanents-cards.py "$m"; done

No collector binds the attackers of "whenever you attack with one or more ..." or the creatures of "whenever one or
more creatures you control deal combat damage to a player" into the effect any more. The stack entry carries them: the
attack collector gives a filtered "whenever you attack" entry the matching attackers as its attacking snapshots,
declared recipients and triggering permanents, and the combat damage collector gives a "one or more" entry the matching
dealers as its triggering permanents. So:

- "whenever you attack with one or more [filter] creatures" is Triggers.youAttack().withFilter(filter), which the
  collector judges against the declared attackers (CR 603.2), instead of a predicate on the effect;
- the attack-group effects become existing ones that read the entry: "they gain [keyword]" GrantKeywordEffect with
  GrantScope.TARGETS (Tyvar, and Valor's Reach as Love on the Battlefield writes it), "those creatures get +2/+2"
  BoostAllOwnCreaturesEffect.forDeclaredAttackers() (Lulu), "their total power"
  TotalPowerOfControlledCreatures.declaredAttackers(null) (Witch-king) and "put a +1/+1 counter on each of them"
  PutCounterOnEachMatchingPermanentEffect with EachPermanentScope.TRIGGERING_PERMANENTS (Vrestin);
- "put a +1/+1 counter on each of those creatures" of a combat damage batch is the same counter effect (Heroes in a
  Half Shell, Vulture).
"""
import pathlib
import re
import sys

EFFECT = "com.github.laxika.magicalvibes.model.effect."
FILTER = "com.github.laxika.magicalvibes.model.filter."
COUNTER_ON_EACH = ("new PutCountersOnCombatDamageDealersEffect(\n"
                   "                                CounterType.PLUS_ONE_PLUS_ONE, 1)",
                   "new PutCounterOnEachMatchingPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,\n"
                   "                                new PermanentTruePredicate(), EachPermanentScope.TRIGGERING_PERMANENTS)")
COUNTER_ON_EACH_IMPORTS = {
    "add": [EFFECT + "PutCounterOnEachMatchingPermanentEffect", EFFECT + "EachPermanentScope",
            FILTER + "PermanentTruePredicate"],
    "remove": [EFFECT + "PutCountersOnCombatDamageDealersEffect"],
}

CARDS = {
    "HeroesInAHalfShell.java": {"replace": [COUNTER_ON_EACH], **COUNTER_ON_EACH_IMPORTS},
    "VultureFeatheredFiend.java": {"replace": [COUNTER_ON_EACH], **COUNTER_ON_EACH_IMPORTS},
    "LuluForgetfulHollyphant.java": {
        "replace": [
            ("""            card.addTrigger(Triggers.youAttack(),
                    new BoostTriggeringAttackersEffect(otherFlyers, 2, 2));""",
             """            card.addTrigger(Triggers.youAttack().withFilter(otherFlyers),
                    new BoostAllOwnCreaturesEffect(2, 2).forDeclaredAttackers());"""),
        ],
        "add": [EFFECT + "BoostAllOwnCreaturesEffect"],
        "remove": [EFFECT + "BoostTriggeringAttackersEffect"],
    },
    "TyvarTheBellicose.java": {
        "replace": [
            ("""        addTrigger(Triggers.youAttack(), new GrantKeywordToTriggeringAttackersEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ELF), Keyword.DEATHTOUCH));""",
             """        addTrigger(Triggers.youAttack().withFilter(new PermanentHasSubtypePredicate(CardSubtype.ELF)),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGETS));"""),
        ],
        "add": [EFFECT + "GrantKeywordEffect"],
        "remove": [EFFECT + "GrantKeywordToTriggeringAttackersEffect"],
    },
    "ValorsReach.java": {
        "replace": [
            ("""                new GrantKeywordToTriggeringAttackersEffect(
                        new PermanentIsAttackingPredicate(), Keyword.DOUBLE_STRIKE));""",
             """                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGETS));"""),
        ],
        "add": [EFFECT + "GrantKeywordEffect", EFFECT + "GrantScope"],
        "remove": [EFFECT + "GrantKeywordToTriggeringAttackersEffect", FILTER + "PermanentIsAttackingPredicate"],
    },
    "VrestinMenoptraLeader.java": {
        "replace": [
            ("""        PermanentAllOfPredicate attackingInsect = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.INSECT)));
        addTrigger(Triggers.youAttack(),
                new PutCounterOnTriggeringAttackersEffect(CounterType.PLUS_ONE_PLUS_ONE, attackingInsect));""",
             """        addTrigger(Triggers.youAttack().withFilter(new PermanentHasSubtypePredicate(CardSubtype.INSECT)),
                new PutCounterOnEachMatchingPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentTruePredicate(), EachPermanentScope.TRIGGERING_PERMANENTS));"""),
        ],
        "add": [EFFECT + "PutCounterOnEachMatchingPermanentEffect", EFFECT + "EachPermanentScope",
                FILTER + "PermanentTruePredicate"],
        "remove": [EFFECT + "PutCounterOnTriggeringAttackersEffect", FILTER + "PermanentAllOfPredicate",
                   FILTER + "PermanentIsAttackingPredicate"],
    },
    "WitchKingSkyScourge.java": {
        "replace": [
            ("""        addTrigger(Triggers.youAttack(),
                new ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.WRAITH)));""",
             """        addTrigger(Triggers.youAttack().withFilter(new PermanentHasSubtypePredicate(CardSubtype.WRAITH)),
                new ExileTopCardsMayPlayThisTurnEffect(TotalPowerOfControlledCreatures.declaredAttackers(null)));"""),
        ],
        "add": [EFFECT + "ExileTopCardsMayPlayThisTurnEffect",
                "com.github.laxika.magicalvibes.model.amount.TotalPowerOfControlledCreatures"],
        "remove": [EFFECT + "ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect"],
    },
}


def fix_imports(src, add, remove):
    """Drops the removed imports and adds each missing one after the project import that sorts just before it."""
    for name in remove:
        src = src.replace(f"import {name};\n", "")
    for name in add:
        line = f"import {name};\n"
        if line in src:
            continue
        project = re.findall(r"^import com\.github\.[^;]+;\n", src, flags=re.M)
        before = [existing for existing in project if existing < line]
        anchor = before[-1] if before else project[0]
        at = src.index(anchor) + (len(anchor) if before else 0)
        src = src[:at] + line + src[at:]
    return src


def convert(path, spec):
    src = path.read_text()
    converted = src
    for old, new in spec["replace"]:
        if new in converted:
            continue
        if old not in converted:
            raise SystemExit(f"{path}: neither the old nor the new form of {old!r}")
        converted = converted.replace(old, new)
    converted = fix_imports(converted, spec["add"], spec["remove"])
    if converted != src:
        path.write_text(converted)
        print(f"converted {path}")


def main(module):
    for name, spec in CARDS.items():
        for path in sorted(pathlib.Path(module).rglob(name)):
            if "/build/" not in str(path):
                convert(path, spec)


if __name__ == "__main__":
    main(sys.argv[1])
