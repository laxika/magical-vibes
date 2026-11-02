#!/usr/bin/env python3
"""Makes each card an "exile until" loop exiles its own rules event batch (effect-slot-refactor.md step 13, run 1
follow-up).

Step 12a opened one rules event batch per resolving effect, so the cards a cascade, discover or "exile cards from the
top of your library until …" loop exiled became one occurrence and a "whenever one or more cards are put into exile"
ability triggered once. Such cards are exiled one at a time (ruling of 2024-06-07). In every loop of the handlers in
FILES whose body exiles a card from a library, the exile statement is wrapped in
gameData.rulesEvents.separately(() -> ...).

Usage: python3 scripts/step13-exile-until-separately.py
"""
import os
import re
import sys

ROOT = "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service"
FILES = [
    "CascadeEffectHandler", "DiscoverEffectHandler", "DrawService",
    "EachOpponentExilesTopUntilTotalManaValueEffectHandler",
    "EachOpponentExilesTopUntilTotalManaValueMayCastThisTurnEffectHandler",
    "EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffectHandler", "EnsnaredByTheMaraVillainousChoiceEffectHandler",
    "ExileTopCardsUntilPermanentCountToBattlefieldEffectHandler", "ExileTopUntilLandOfTargetOpponentEffectHandler",
    "ExileTopUntilNonlandAndSuspendEffectHandler", "ExileTopUntilNonlandDealManaValueDamageToAnyTargetEffectHandler",
    "ExileTopUntilNonlandMayCastByDiscardEffectHandler", "ExileTopUntilNonlandMayCastOrPutIntoHandEffectHandler",
    "ExileTopUntilNonlandMayCastThisTurnEffectHandler", "ExileTopUntilNonlandMayCastWithoutPayingManaCostEffectHandler",
    "ExileTopUntilNonlandMayCastWithoutPayingManaEffectHandler",
    "ExileTopUntilNonlandOfDamagedPlayerMayCastForLifeThisTurnEffectHandler",
    "ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffectHandler",
    "ExileTopUntilNonlandOfEachOpponentWithSourceEffectHandler",
    "ExileTopUntilNonlandOfTargetOpponentMayCastThisTurnEffectHandler",
    "ExileTopUntilNonlandPermanentToBattlefieldOrHandEffectHandler", "ExileTopUntilNonlandWithSuspendEffectHandler",
    "ExileTriggeringSpellUntilNonlandDealManaValueDifferenceAndMayCastEffectHandler",
    "ExileUntilCardPredicateMayCastWithoutPayingManaEffectHandler",
    "ExileUntilCardPredicateMayPlayWhileSourceControlledEffectHandler",
    "ExileUntilNonlandCardMayCastIfManaValueLessThanMountainsEffectHandler",
    "ExileUntilNonlandToHandRepeatIfHighMVEffectHandler", "ImprovisationCapstoneEffectHandler",
    "PossibilityStormExileAndCastEffectHandler", "RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffectHandler",
    "RevealUntilCardPredicateMayCastWithoutPayingManaEffectHandler",
    "RevealUntilCountMatchingCardsForDestroyedPermanentControllersEffectHandler",
    "ShuffleTargetPermanentsThenEachControllerMayCastEffectHandler", "TibaltTrickeryEffectHandler",
]
EXILE = re.compile(r"^(\s*)((?:exileService\.exileCard|gameData\.addToExile)\()", re.M)


def loop_bodies(src):
    for m in re.finditer(r"while \(([^\n]*)\) \{", src):
        i, depth = m.end(), 1
        while depth:
            depth += {"{": 1, "}": -1}.get(src[i], 0)
            i += 1
        yield m.end(), i


def statement_end(src, start):
    depth, i = 0, start
    while True:
        c = src[i]
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
        elif c == ";" and depth == 0:
            return i
        i += 1


def convert(src):
    edits = []
    for start, end in loop_bodies(src):
        body = src[start:end]
        if "removeFirst" not in body and "library" not in body.lower() and "deck" not in body.lower():
            continue
        for m in EXILE.finditer(src, start, end):
            stmt_end = statement_end(src, m.start(2))
            edits.append((m.start(2), stmt_end))
    for s, e in sorted(set(edits), reverse=True):
        if src[:s].endswith("separately(() -> "):
            continue
        src = src[:s] + "gameData.rulesEvents.separately(() -> " + src[s:e] + ")" + src[e:]
    return src


def main():
    paths = {}
    for root, _, files in os.walk(ROOT):
        for name in files:
            if name[:-5] in FILES:
                paths.setdefault(name[:-5], []).append(os.path.join(root, name))
    missing = set(FILES) - set(paths)
    if missing:
        sys.exit("missing: %s" % sorted(missing))
    for name in FILES:
        for path in sorted(paths[name]):
            src = open(path).read()
            out = convert(src)
            if out != src:
                open(path, "w").write(out)
                print("converted:", path)
            elif "separately(() -> " not in src:
                print("no exile-until loop:", path)


if __name__ == "__main__":
    main()
