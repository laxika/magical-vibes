#!/usr/bin/env python3
"""Lists the card test classes of the cards on the collectors run 17z17b changed (effect-slot-refactor.md step 15).

Read-only. A card is on a changed collector when its class writes one of the trigger factories below, or is one of
the Auras whose granted "whenever enchanted creature attacks" ability went through the two-group or graveyard-card
route. A test class is listed when it is the card's own (<Card>Test) or names the card class in its body
(@CardUsed or a constructor call).

    scripts/step15-run17z17b-test-list.py            the classes, one per line, with their letter module
    scripts/step15-run17z17b-test-list.py --gradle   the Gradle arguments, a --tests filter after each module's task
    scripts/step15-run17z17b-test-list.py --cards    the card classes and the factory each was listed for
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
TESTS = ROOT / "magical-vibes-card-test"

FACTORIES = [
    # CombatDamageService: "whenever this is dealt combat damage" has the source's snapshot on its plain entry.
    "dealtCombatDamage",
    # StepTriggerService: "at the beginning of each end step".
    "endStep",
    # CombatBlockService: "whenever enchanted creature attacks and isn't blocked".
    "enchantedCreatureAttacksUnblocked",
    # The damaged or enchanted permanent as it last existed, stamped on a record that is not on the stack yet.
    "creatureOrPlaneswalkerOpponentControlsDealtExcessDamage",
    "creatureOrPlaneswalkerOpponentControlsDealtExcessNoncombatDamage",
    "creaturesOpponentsControlDealtExcessNoncombatDamage",
    "enchantedCreatureDealtDamage",
    # TriggerCollectionService.collectAllyCreatureBecomesTargetOfSpellTriggers.
    "creatureYouControlBecomesTargetOfSpell",
    # PermanentCounterSupport: the five counter-placement watchers on the shared record.
    "minusOneCounterPutOnCreature",
    "youPutMinusOneCounterOnCreature",
    "plusOneCountersPutOnHumanYouControl",
    "plusOneCountersPutOnAnotherCreatureYouControl",
    "plusOneCountersPutOnAnotherNonHydraCreatureYouControl",
    "plusOneCountersPutOnAnotherPermanentYouControlFirstTimeEachTurn",
    "youPutCountersOnAnotherCreature",
    "opponentPutsCountersOnCreatureTheyControl",
    # The collectors the new route-test watches showed recording differently by route.
    "youExertCreature",
    "becomesAttachedToCreature",
    "state",
    "sourceState",
    "battlefieldHasAtMost",
    "battlefieldHasAtLeast",
    "becomesCrewed",
    "becomesCrewedForFirstTimeEachTurn",
    "championsCreature",
    "becomesMonstrous",
    "becomesTargetOfOpponentSpellOrAbility",
    "creatureYouControlBecomesTargetOfOpponentSpellOrAbility",
    "entersFrom",
    "instantOrSorceryYouControlDealsDamageToOpponent",
    "instantOrSorceryYouControlDealsDamageToPlayer",
    "youOrYourPlaneswalkerDealtCombatDamage",
    # A temporary "whenever a creature deals combat damage to a player" ability records the damaged player.
    "creatureDealsCombatDamageToPlayer",
]
FACTORY = re.compile(r"Triggers\.(" + "|".join(FACTORIES) + r")\(")
ATTACKS = re.compile(r"Triggers\.attacks\(")
CAST = re.compile(r"Triggers\.(youCastSpell|opponentCastsSpell|playerCastsSpell)\(")


def card_classes():
    """Card class name -> the reason it is listed."""
    listed = {}
    for path in sorted(CARDS.glob("*/src/main/java/**/*.java")):
        text = path.read_text(encoding="utf-8")
        match = FACTORY.search(text)
        if match:
            listed[path.stem] = match.group(1)
            continue
        groups = len(re.findall(r"\btarget\(", text))
        graveyard = "targetGraveyard(true)" in text or "GraveyardCard" in text
        attached = "Aura" in text or "ENCHANTED_" in text or "Enchanted" in text
        if ATTACKS.search(text) and attached and (groups > 1 or graveyard):
            # An Aura's "enchanted creature attacks" ability with two target groups or a graveyard card.
            listed[path.stem] = "attacks (Aura, two groups or graveyard card)"
        elif CAST.search(text) and groups > 1:
            # The cast collector's multi-target walk is given the ability's X.
            listed[path.stem] = "cast trigger with more than one target group"
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
            print(f"{name}\t{cards[name]}")
        print(f"{len(cards)} card classes", file=sys.stderr)
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
