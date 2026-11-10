#!/usr/bin/env python3
"""Step 15, run 17z14e: lists the card test classes of the cards that touch what the run changed (read-only).

A card is listed when its source names an attack trigger (`Triggers.attacks()`, `youAttack()`,
`creatureYouControlAttacks()`, `creatureAttacks()`, `playerAttacks()`, `enlists()`, ... : any `Triggers` factory with
"attack" in its name, on the card itself, in an ability it grants or in a delayed ability it registers), or speaks of
attacking at all in an effect, condition or filter name (exert, myriad, "attacking", "attacked", "attacker",
"defending player"): the abilities `CombatAttackService`'s attack collectors put on the stack and the effects that read
what those entries record. A test class is listed when it is such a card's (`<Card>Test`, in whichever letter module it
lives) or when its source names a card with an attack trigger (a back face, a token, a card another card's test
attacks with).

Usage: step15-run17z14e-test-list.py [--gradle]  (--gradle prints the per-module `:test --tests` arguments).
"""
import pathlib
import re
import sys

REPO = pathlib.Path(__file__).resolve().parent.parent
CARDS = REPO / "magical-vibes-card"
TESTS = REPO / "magical-vibes-card-test"

RULE = re.compile(r"Triggers\.\w*[aA]ttack\w*\(|Triggers\.enlists\(|[aA]ttack|ATTACK|[eE]xert|[mM]yriad|Defending|DEFENDING"
                  r"|[eE]nlist|[eE]xalted")
ABILITY = re.compile(r"Triggers\.\w*[aA]ttack\w*\(|Triggers\.enlists\(")


def main():
    listed = set()
    with_ability = set()
    for path in sorted(CARDS.glob("*/src/main/java/com/github/laxika/magicalvibes/cards/*/*.java")):
        text = path.read_text()
        if RULE.search(text):
            listed.add(path.stem)
        if ABILITY.search(text):
            with_ability.add(path.stem)
    names = re.compile(r"\b[A-Z]\w*\b")
    tests = {}
    for test in sorted(TESTS.glob("*/src/test/java/com/github/laxika/magicalvibes/cards/*/*Test.java")):
        letter = test.relative_to(TESTS).parts[0]
        if test.stem[:-len("Test")] in listed or not with_ability.isdisjoint(names.findall(test.read_text())):
            tests.setdefault(letter, set()).add(test.stem)
    if "--gradle" in sys.argv:
        print(" ".join(f":magical-vibes-card-test:{letter}:test " + " ".join(f"--tests '*.{name}'" for name in sorted(names))
                       for letter, names in sorted(tests.items())))
    else:
        for letter, names in sorted(tests.items()):
            for name in sorted(names):
                print(f"{letter}/{name}")
    print(f"{sum(len(names) for names in tests.values())} test classes in {len(tests)} modules", file=sys.stderr)


if __name__ == "__main__":
    main()
