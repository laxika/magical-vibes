#!/usr/bin/env python3
"""Step 15, run 17z16c2 (read-only): the card test classes of the cards on the handlers the run changed.

A card is listed when its class writes

  - an effect whose handler the run put on `ReflexiveAbilitySupport` (`CHANGED`), or that reaches a choice handler
    the run changed: the bounce, sacrifice-for-power-damage and sized-token follow-ups, "you may" (the accepted
    ability of a question asked before it was on the stack), a forced cost;
  - an effect with an `afterSuccessfulCastEffect` component (`ExileCastTargetSupport.queueAfterSuccessfulCast`);
  - an effect whose handler names `DiscardFollowUp` (the discard follow-up of `CardChoiceHandlerService`);
  - an effect that names `LibrarySelectionFollowUp` (the library follow-up of `LibraryChoiceHandlerService`);
  - or mentions champion, exert, evolve, specialize, vanishing, the initiative, miracle or a Saga chapter, or is
    one of the five cards of the prevention riders, Capricious Efreet and Rubble Rouser (the abilities the run read
    and classified, and the chapter choice handlers).

Usage: python3 scripts/step15-run17z16c2-test-list.py [--gradle] [--letters abc] [--effects]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ENGINE = ROOT / "magical-vibes-engine/src/main/java"
EFFECTS = ROOT / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect"
CHANGED = {
    "EarthbendTargetLandThenFightEffect", "ExilePermanentThenExileMatchingPermanentsEffect",
    "ForcedCostOrElseEffect", "ReturnPermanentOwnedByPlayerToHandThenEffect",
    "ReturnPermanentControlledByPlayerToHandThenEffect", "ReturnOneOfCombatDamageDealersToHandThenEffect",
    "SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect", "SacrificeCreatureCreateSizedTokenEqualToPowerEffect",
    "AttachTargetAuraOrEquipmentToTargetCreatureEffect", "AttachTargetEquipmentToTargetCreatureEffect",
    "ExchangeControlOfSourceAndTriggeringSpellEffect", "MayCastLesserSpellWithSharedTypeOrCreateTokenEffect",
    "ReflectEffect", "PutCounterThenMayBecomeCopyOfCardsEffect", "MayEffect", "MayPayManaEffect",
    "SpecializeLaezelEffect",
}
CARD_MARKERS = re.compile(
    r"[cC]hampion|[eE]xert|[eE]volve|[sS]pecialize|Vanishing|Initiative|[mM]iracle|Saga|[cC]hapter|"
    r"SamiteMinistration|NewWayForward|OutfittedJouster|CapriciousEfreet|RubbleRouser")

effects = set(CHANGED)
for path in sorted(EFFECTS.glob("*.java")):
    text = path.read_text()
    if "afterSuccessfulCastEffect" in text or "LibrarySelectionFollowUp" in text:
        effects.add(path.stem)
for path in sorted(ENGINE.glob("**/service/effect/**/*Handler.java")):
    if "DiscardFollowUp" in path.read_text():
        effects.add(path.stem[:-len("Handler")])
effects = {name for name in effects if name.endswith("Effect")}

if "--effects" in sys.argv:
    for name in sorted(effects):
        print(name)
    print(len(effects), "effects", file=sys.stderr)
    sys.exit(0)

WRITES = re.compile(r"\b(?:" + "|".join(sorted(re.escape(name) for name in effects)) + r")\b")
letters = None
if "--letters" in sys.argv:
    letters = set(sys.argv[sys.argv.index("--letters") + 1])

by_letter = {}
cards = 0
for path in sorted((ROOT / "magical-vibes-card").glob("*/src/main/java/**/*.java")):
    text = path.read_text()
    if not (WRITES.search(text) or CARD_MARKERS.search(text)):
        continue
    cards += 1
    name = path.stem + "Test"
    test_letter = path.stem[0].lower()
    if letters is not None and test_letter not in letters:
        continue
    test = (ROOT / "magical-vibes-card-test" / test_letter / "src/test/java/com/github/laxika/magicalvibes/cards"
            / test_letter)
    if (test / (name + ".java")).exists():
        by_letter.setdefault(test_letter, []).append(name)

if "--gradle" in sys.argv:
    print(" ".join(f":magical-vibes-card-test:{letter}:test " + " ".join(f"--tests *.{name}" for name in names)
                   for letter, names in sorted(by_letter.items())))
else:
    for letter, names in sorted(by_letter.items()):
        for name in names:
            print(letter, name)
    print(cards, "card classes;", sum(len(names) for names in by_letter.values()), "test classes in",
          len(by_letter), "modules", file=sys.stderr)
