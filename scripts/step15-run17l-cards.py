#!/usr/bin/env python3
"""Step 15, run 17l: the other combat families' trigger-event conditions are event conditions.

Run 17i's rule (CR 603.4: an intervening "if" is only an "if" that immediately follows the trigger condition) for the
abilities `step15-run17j-scan.py` lists as EVENT outside the attack families of runs 17i to 17k:

- "whenever this creature attacks a player and isn't blocked" (Master of Cruelties, `SourceAttackingPlayer`, both
  abilities);
- a Class's level or a Spacecraft's station on which the ability exists (Fighter Class, Gossip's Talent: CR 716.2a;
  Candela, Aegis of Adagia's 8+ charge counters), a Siege's chosen mode (Frostcliff Siege);
- "during your turn" (Quilled Greatwurm, The Aetherspark), "deals combat damage to an opponent" (Sleeper's Robe),
  "to a player who is afraid of you" (Creepy Crawler);
- Seifer, Balamb Rival's "whenever a creature attacking one of your opponents becomes blocked by two or more
  creatures": the attack target joins the event condition the block count already was, as one `AllOf`.

Each intervening "if" of the named condition on the named trigger becomes the event condition. Idempotent.

Usage: step15-run17l-cards.py <letter module dir>...
"""
import pathlib
import re
import sys

CONVERSIONS = {
    "MasterOfCruelties": ("attacksUnblocked", "SourceAttackingPlayer"),
    "FighterClass": ("creatureYouControlAttacks", "SourceCounterThreshold"),
    "QuilledGreatwurm": ("creatureYouControlDealsCombatDamage", "ControllerTurn"),
    "GossipsTalent": ("creatureYouControlDealsCombatDamageToPlayer", "SourceClassLevelAtLeast"),
    "FrostcliffSiege": ("creatureYouControlDealsCombatDamageToPlayer", "SourceHasChosenMode"),
    "SleepersRobe": ("dealsCombatDamageToPlayer", "AttackedTargetIsOpponent"),
    "CandelaAegisOfAdagia": ("dealsCombatDamageToPlayer", "SourceCounterThreshold"),
    "CreepyCrawler": ("dealsCombatDamageToPlayer", "TargetPlayerIsAfraidOfController"),
    "TheAetherspark": ("equippedCreatureDealsCombatDamage", "ControllerTurn"),
}

SEIFER_BEFORE = ("TriggerOptions.DEFAULT.withInterveningIf(new AttackedTargetIsOpponent())"
                 ".withEventCondition(new BlockedByMinCreatures(2)),")
SEIFER_AFTER = ("TriggerOptions.DEFAULT.withEventCondition(new AllOf(List.of(\n"
                "                        new AttackedTargetIsOpponent(), new BlockedByMinCreatures(2)))),")


def closing(source, index):
    depth = 0
    for j in range(index, len(source)):
        if source[j] == "(":
            depth += 1
        elif source[j] == ")":
            depth -= 1
            if depth == 0:
                return j
    raise ValueError("unbalanced")


def convert_statements(source, factory, condition):
    out, pos = [], 0
    for match in re.finditer(r"(addTrigger|new TriggeredAbility)\(", source):
        if match.start() < pos:
            continue
        end = closing(source, match.end() - 1) + 1
        statement = source[match.start():end]
        if re.search(r"Triggers\.%s\(" % factory, statement[:160]):
            statement = statement.replace(f".withInterveningIf(new {condition}(", f".withEventCondition(new {condition}(")
        out.append(source[pos:match.start()])
        out.append(statement)
        pos = end
    out.append(source[pos:])
    return "".join(out)


def add_import(source, qualified):
    if f"import {qualified};" in source:
        return source
    first = re.search(r"^import ", source, flags=re.M)
    return source[:first.start()] + f"import {qualified};\n" + source[first.start():]


def convert(name, source):
    if name in CONVERSIONS:
        return convert_statements(source, *CONVERSIONS[name])
    if name == "SeiferBalambRival" and SEIFER_BEFORE in source:
        source = source.replace(SEIFER_BEFORE, SEIFER_AFTER)
        source = add_import(source, "com.github.laxika.magicalvibes.model.condition.AllOf")
        return add_import(source, "java.util.List")
    return source


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/main/java/**/*.java")):
            source = path.read_text()
            result = convert(path.stem, source)
            if result != source:
                path.write_text(result)
                changed.append(path.stem)
    print(f"{len(changed)} card classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
