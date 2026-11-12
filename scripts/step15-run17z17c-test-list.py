#!/usr/bin/env python3
"""Lists the cards on the activation collector and their card test classes (effect-slot-refactor.md step 15, run 17z17c).

Read-only. A card is on the collector when its class writes one of the "whenever [a player] activates an ability"
trigger factories below; the ACTIVATIONS list adds the cards whose activation goes through a call site the run
changed for every ability (a ninjutsu, exhaust, embalm or eternalize ability, the special-action discard). A test
class is listed when it is the card's own (<Card>Test) or names the card class in its body (@CardUsed or a
constructor call).

    scripts/step15-run17z17c-test-list.py            the test classes, one per line, with their letter module
    scripts/step15-run17z17c-test-list.py --gradle   the Gradle arguments, a --tests filter after each module's task
    scripts/step15-run17z17c-test-list.py --cards    the card classes: registration, factory, filters, whether an
                                                     effect copies the activated ability
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
TESTS = ROOT / "magical-vibes-card-test"

FACTORIES = [
    "youActivateAbility",
    "opponentActivatesAbility",
    "youActivateNonManaAbility",
    "playerActivatesNonManaAbility",
    "opponentActivatesNonManaAbility",
    "youActivateExhaustAbility",
    "youActivateEternalizeOrEmbalm",
]
FACTORY = re.compile(r"Triggers\.(" + "|".join(FACTORIES) + r")\(")
FILTERS = re.compile(r"\.(withSource|withKind|withStackEntryFilter|withPayment)\(")
ACTIVATIONS = re.compile(r"ninjutsuAbility\(|addNinjutsu\(|withNinjutsu\(|withExhaust\(|addEmbalm\(|addEternalize\("
                         r"|GrantEmbalm|withDiscardSourceAsSpecialAction\("
                         r"|CopyNextActivatedAbilityWithXThisTurn|CopyNextLoyaltyAbility|CopyNextExhaustAbility")
REGISTRATION = re.compile(r'@CardRegistration\(set = "([^"]+)", collectorNumber = "([^"]+)"\)')


def card_classes():
    """Card class name -> (registration, why it is listed, filters, copies)."""
    listed = {}
    for path in sorted(CARDS.glob("*/src/main/java/**/*.java")):
        text = path.read_text(encoding="utf-8")
        registration = REGISTRATION.search(text)
        printing = f"{registration.group(1)} {registration.group(2)}" if registration else "-"
        factories = sorted(set(FACTORY.findall(text)))
        if factories:
            filters = sorted(set(FILTERS.findall(text)))
            copies = "CopyTriggeringSpellOrAbilityEffect" in text
            listed[path.stem] = (printing, ",".join(factories), ",".join(filters) or "-", "copies" if copies else "-")
        elif ACTIVATIONS.search(text):
            listed[path.stem] = (printing, "activation site", "-", "-")
    return listed


def test_classes(cards):
    """(letter, fully qualified test class) of every test class that is a listed card's or names one."""
    names = sorted(cards, key=len, reverse=True)
    word = re.compile(r"\b(" + "|".join(re.escape(name) for name in names) + r")\b") if names else None
    found = set()
    for path in sorted(TESTS.glob("*/src/test/java/**/*Test.java")):
        letter = path.relative_to(TESTS).parts[0]
        own = path.stem[:-4] in cards
        if own or (word and word.search(path.read_text(encoding="utf-8"))):
            relative = path.relative_to(TESTS / letter / "src" / "test" / "java")
            found.add((letter, ".".join(relative.with_suffix("").parts)))
    return sorted(found)


def main():
    cards = card_classes()
    if "--cards" in sys.argv:
        for name in sorted(cards):
            print(name + "\t" + "\t".join(cards[name]))
        on_factory = sum(1 for value in cards.values() if value[1] != "activation site")
        print(f"{len(cards)} card classes, {on_factory} on an activation trigger factory, "
              f"{sum(1 for value in cards.values() if value[3] == 'copies')} that copy the activated ability",
              file=sys.stderr)
        return
    classes = test_classes(cards)
    if "--gradle" in sys.argv:
        arguments = []
        for letter in sorted({letter for letter, _ in classes}):
            arguments.append(f":magical-vibes-card-test:{letter}:test")
            for other, name in classes:
                if other == letter:
                    arguments += ["--tests", name]
        print(" ".join(arguments))
    else:
        for letter, name in classes:
            print(f"{letter}\t{name}")
    print(f"{len(cards)} card classes, {len(classes)} test classes in "
          f"{len({letter for letter, _ in classes})} letter modules", file=sys.stderr)


if __name__ == "__main__":
    main()
