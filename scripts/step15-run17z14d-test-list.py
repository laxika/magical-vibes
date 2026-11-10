#!/usr/bin/env python3
"""Step 15, run 17z14d: lists the card test classes of the cards that touch what the run changed (read-only).

A card is listed when its source matches one of `RULES`:

- an own "when this enters" ability (`Triggers.enters(ObjectRelation.THIS)`, `entersOrAttacks`, ...) on a card that
  names a modal, "you may" or conditional effect, evoke, or an effect class whose source speaks of a graveyard or of a
  spell on the stack (`ROUTED`: the classes `EtbTriggerService` may route to a choice of their own), or whose
  `addTrigger` statement has several effects;
- a trigger one of `TriggerCollectionService`'s hand-rolled zone-change collectors reads: leaves the battlefield,
  returned to hand, enters from a zone, phases in or out, leaves a graveyard, exiled from the battlefield, or any
  triggered ability that functions from the graveyard or from exile;
- one of `NAMED`: cards with a multi-effect enters ability the rules above miss.

Usage: step15-run17z14d-test-list.py [--gradle]  (--gradle prints the per-module `:test --tests` arguments).
"""
import pathlib
import re
import sys

REPO = pathlib.Path(__file__).resolve().parent.parent
CARDS = REPO / "magical-vibes-card"
TESTS = REPO / "magical-vibes-card-test"
EFFECTS = REPO / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect"

OWN_ENTERS = re.compile(r"Triggers\.enters\w*\(\s*(?:ObjectRelation\.THIS|null,\s*ObjectRelation\.THIS)")
STATEMENT = re.compile(r"add(?:Simultaneous)?Trigger\(\s*(?:new TriggerCondition\(\s*)?Triggers\.enters\w*\("
                       r"\s*(?:null,\s*)?ObjectRelation\.THIS.*?\);\n", re.S)
SHAPE = re.compile(r"ChooseOneEffect|[eE]voke|MayEffect|MayPay|ConditionalEffect|TriggerOptions")
ROUTED = re.compile(r"\b(" + "|".join(sorted(
    path.stem for path in EFFECTS.glob("*.java")
    if re.search(r"[gG]raveyard|spellOnStack|Kind\.SPELL|StackEntry", path.read_text()))) + r")\b")
NAMED = {"GrafRats", "RoostOfDrakes", "ThayanEvokers"}
ZONE_CHANGE = re.compile(
    r"Triggers\.(leavesBattlefield\w*|putIntoHandFrom|entersFrom|phasesIn|phasesOut|leavesGraveyard|exiledFrom"
    r"|putIntoGraveyardOrExileFromBattlefield|milled|turnedFaceUp)\(|AbilityZones\.(GRAVEYARD|EXILE)")


def own_enters_shape(text):
    if not OWN_ENTERS.search(text):
        return False
    if SHAPE.search(text) or ROUTED.search(text):
        return True
    return any(top_level_arguments(statement) > 2 for statement in STATEMENT.findall(text))


def top_level_arguments(statement):
    """How many arguments the call that `statement` starts with takes: its trigger, then its effects."""
    depth = 0
    arguments = 1
    for character in statement[statement.index("(") + 1:]:
        if character in "([{":
            depth += 1
        elif character in ")]}":
            if depth == 0:
                break
            depth -= 1
        elif character == "," and depth == 0:
            arguments += 1
    return arguments


RULES = [own_enters_shape, lambda text: ZONE_CHANGE.search(text)]


def main():
    listed = {}
    for path in sorted(CARDS.glob("*/src/main/java/com/github/laxika/magicalvibes/cards/*/*.java")):
        text = path.read_text()
        if path.stem in NAMED or any(rule(text) for rule in RULES):
            letter = path.parent.name
            test = TESTS / letter / "src/test/java/com/github/laxika/magicalvibes/cards" / letter / (path.stem + "Test.java")
            if test.exists():
                listed.setdefault(letter, []).append(path.stem + "Test")
    if "--gradle" in sys.argv:
        print(" ".join(f":magical-vibes-card-test:{letter}:test " + " ".join(f"--tests '*.{name}'" for name in names)
                       for letter, names in sorted(listed.items())))
    else:
        for letter, names in sorted(listed.items()):
            for name in names:
                print(f"{letter}/{name}")
    print(f"{sum(len(names) for names in listed.values())} test classes in {len(listed)} modules", file=sys.stderr)


if __name__ == "__main__":
    main()
