#!/usr/bin/env python3
"""Step 15, runs 17z16c4 and 17z16c3b (read-only): the card test classes of the cards on the changed handlers.

A card is listed when its class writes

  - `QueueReflexiveAbilityEffect`, or an effect whose handler names it (hands it its continuation): every route of
    `QueueReflexiveAbilityEffectHandler` now reads the record's source, and an "up to one target" effect with
    nothing to target goes on the stack without a prompt;
  - an effect of a handler the runs changed (`CHANGED`): "create a token. When you do", "exile a card from your
    graveyard. When you do / if you do", the remove-a-counter and sacrifice follow-ups' mode route, pay up to N
    times, the after-cast effects, Vial Smasher's random opponent, the become-a-copy choices, the sized token;
  - exert or any other `SkipNextUntapEffect` (the exertion fires "whenever you exert a creature" itself), a modal
    reflexive follow-up (`ChooseOneAtTriggerTimeEffect`), or `MillEffect` / `AwardManaEffect` in an activated
    ability that also writes a reflexive rider (`NAMED`).

Usage: python3 scripts/step15-run17z16c4-test-list.py [--gradle] [--letters abc] [--effects]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ENGINE = ROOT / "magical-vibes-engine/src/main/java"
CHANGED = {
    "QueueReflexiveAbilityEffect", "CreateTokenThenEffect", "CreateTokenAttachedToTargetThenEffect",
    "ExileOwnGraveyardCardThenEffect", "RemoveCounterFromSourceThenEffect", "SacrificePermanentThenEffect",
    "PayManaUpToNTimesEffect", "ExileTargetCardFromGraveyardAndMayCastCopyEffect",
    "MayCastFromHandWithoutPayingManaCostEffect", "MayCastAnySpellFromHandWithoutPayingManaCostEffect",
    "CastTargetInstantOrSorceryFromGraveyardEffect", "CastCardFromGraveyardEffect",
    "DealDamageToRandomOpponentOrPlaneswalkerEffect", "PutCounterThenMayBecomeCopyOfCardsEffect",
    "KayaSpiritsJusticeEffect", "ExileCardFromAnyGraveyardThenMayBecomeCopyEffect",
    "BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect",
    "RevealTargetPlayerLibraryUntilCreatureAndBecomeCopyUntilEndOfTurnEffect",
    "SacrificeCreatureCreateSizedTokenEqualToPowerEffect", "SkipNextUntapEffect", "ChooseOneAtTriggerTimeEffect",
    "EnteringCreatureFightsTargetCreatureEffect",
}
READERS = set()
CARD_MARKERS = re.compile(r"[Ee]xert|youExertCreature|SplitCardCast")
NAMED = {"RubbleRouser", "ShaunRebeccaAgents", "HostileHostel", "PassTheTorch"}

effects = set(CHANGED)
for path in sorted(ENGINE.glob("**/service/effect/**/*Handler.java")):
    text = path.read_text()
    if "QueueReflexiveAbilityEffect" in text or "queueReflexiveAbilityEffectHandler" in text:
        effects.add(path.stem[:-len("Handler")])
effects = {name for name in effects if name.endswith("Effect")}

if "--effects" in sys.argv:
    for name in sorted(effects):
        print(name)
    print(len(effects), "effects", file=sys.stderr)
    sys.exit(0)

WRITES = re.compile(r"\b(?:" + "|".join(sorted(re.escape(name) for name in effects | READERS)) + r")\b")
letters = None
if "--letters" in sys.argv:
    letters = set(sys.argv[sys.argv.index("--letters") + 1])

by_letter = {}
cards = 0
for path in sorted((ROOT / "magical-vibes-card").glob("*/src/main/java/**/*.java")):
    text = path.read_text()
    if not (WRITES.search(text) or CARD_MARKERS.search(text) or path.stem in NAMED):
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
