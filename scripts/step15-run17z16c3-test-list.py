#!/usr/bin/env python3
"""Step 15, run 17z16c3 (read-only): the card test classes of the cards on the handlers and effects the run changed.

A card is listed when its class writes

  - `QueueReflexiveAbilityEffect`, or an effect whose handler names it (hands it its continuation);
  - an effect of a handler the run changed (`CHANGED`): the pay-X and pay-any-number handlers, the forced cost and
    cumulative upkeep, Cemetery Desecrator, the reveal-until-nonland damage, Vial Smasher's random opponent, the
    sacrifice-for-power-damage and sized-token follow-ups, the counters-then-reflexive effects, the move-counter
    effect, Takklemaggot's return;
  - an effect whose handler names `DiscardFollowUp` (the discard follow-up of `CardChoiceHandlerService`), or one of
    the `LastDiscardedCard...` amounts;
  - an X or event-value predicate or amount a changed reader evaluates (`READERS`);
  - "at the beginning of each upkeep" (`Triggers.upkeep(ActivePlayer.ANY)`, whose event conditions now see the
    player whose upkeep it is), or is one of the cards of the two prevention riders.

Usage: python3 scripts/step15-run17z16c3-test-list.py [--gradle] [--letters abc] [--effects]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ENGINE = ROOT / "magical-vibes-engine/src/main/java"
CHANGED = {
    "QueueReflexiveAbilityEffect", "PayManaAnyNumberOfTimesPutCountersOnSelfEffect",
    "PayXManaPutCountersOnSelfEffect", "PayXManaMoveCountersFromSourceToTargetCreatureEffect",
    "PayXManaReanimateCreatureWithManaValueXEffect", "PayXManaReanimateCreaturesWithTotalManaValueXEffect",
    "PayXManaCastTargetInstantOrSorceryFromGraveyardEffect", "PayXManaDealXDamageToAnyTargetEffect",
    "PayAnyAmountOfColorManaToDealDamageToAnyTargetEffect", "PayManaUpToNTimesEffect", "PayXManaEffect",
    "ForcedCostOrElseEffect", "CumulativeUpkeepEffect", "CemeteryDesecratorEffect",
    "RevealUntilNonlandBottomThenDealManaValueDamageEffect", "DealDamageToRandomOpponentOrTheirPlaneswalkerEffect",
    "SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect", "SacrificeCreatureCreateSizedTokenEqualToPowerEffect",
    "SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffect",
    "PutCountersOnTargetPermanentThenReflexiveEffect", "EachPlayerMayPutCountersOnCreatureEffect",
    "EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect",
    "MoveCounterFromSourceToTargetCreatureEffect", "ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect",
    "NihiloorEnterEffect", "NihiloorTapAndStealEffect",
    "ExileTopCardMayPlayThisTurnAndBoostTargetCreatureByManaValueEffect",
    "ExileTargetCreatureCardFromGraveyardThenReflexiveEffect", "EachPlayerExilesCardFromGraveyardThenEffect",
    "CollectEvidenceEffect", "RollD20Effect", "MillEachOpponentThenIfMilledEffect",
}
READERS = {
    "LastDiscardedCardManaValue", "LastDiscardedCardTypeCount", "CardManaValueAtMostEventValuePredicate",
    "CardManaValueEqualsEventValuePredicate", "PermanentManaValueAtMostEventValuePredicate",
    "CardManaValueEqualsXPredicate", "PermanentMaxManaValueXPredicate",
}
CARD_MARKERS = re.compile(r"Triggers\.upkeep\(ActivePlayer\.ANY\)|TargetPlayerIs\b|CumulativeUpkeep")
NAMED = {"SamiteMinistration", "NewWayForward"}

effects = set(CHANGED)
for path in sorted(ENGINE.glob("**/service/effect/**/*Handler.java")):
    text = path.read_text()
    if "DiscardFollowUp" in text or "QueueReflexiveAbilityEffect" in text:
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
