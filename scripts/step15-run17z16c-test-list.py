#!/usr/bin/env python3
"""Step 15, run 17z16c (read-only): the card test classes of the cards on the handlers the run changed.

A card is listed when its class writes one of the effects whose handler builds a reflexive or delayed triggered
ability through `ReflexiveAbilitySupport` (the handlers the run changed, and the handlers that hand their
continuation to `QueueReflexiveAbilityEffectHandler`, `CreateTokenThenEffectHandler`, `CollectEvidenceEffectHandler`,
`ExileSourceCardFromGraveyardThenEffectHandler` or the sacrifice follow-up of
`PermanentChoiceBattlefieldHandlerService`), a Class level, a venture or initiative effect, or a specialize ability.

The effect names are read from the engine sources: a handler file that mentions `ReflexiveAbilitySupport` or one of
the delegate handlers above handles the effect it is named after.

Usage: python3 scripts/step15-run17z16c-test-list.py [--gradle] [--letters abc] [--effects]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ENGINE = ROOT / "magical-vibes-engine/src/main/java"
DELEGATES = re.compile(
    r"ReflexiveAbilitySupport|QueueReflexiveAbilityEffect|[cC]reateTokenThenEffectHandler|"
    r"[cC]ollectEvidenceEffectHandler|[eE]xileSourceCardFromGraveyardThenEffectHandler|"
    r"PermanentChoiceContext\.SacrificePermanentThen\(")
EXTRA = {
    "ClassLevelUpEffect", "TakeInitiativeEffect", "VentureIntoDungeonEffect", "SacrificePermanentThenEffect",
    "MayPayManaEffect", "ForcedCostOrElseEffect",
}
CARD_MARKERS = re.compile(r"addClassLevel|ClassLevel\(|[sS]pecialize|Venture|Initiative")

effects = set(EXTRA)
for path in sorted(ENGINE.glob("**/service/effect/**/*Handler.java")):
    if DELEGATES.search(path.read_text()):
        effects.add(path.stem[:-len("Handler")])
effects = {name for name in effects if name.endswith("Effect") or name in EXTRA}

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
