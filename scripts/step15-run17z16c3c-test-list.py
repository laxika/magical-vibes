#!/usr/bin/env python3
"""Step 15, run 17z16c3c (read-only): the card test classes of the cards on the changed handlers and effects.

A card is listed when its class writes

  - `QueueReflexiveAbilityEffect`, or an effect whose handler names it (hands it its continuation): the reflexive
    target choice is judged against the record's triggering permanent and player, and the record takes what a
    handler sets on it before it is routed;
  - an effect of a handler or record the run changed (`CHANGED`): the library-to-battlefield follow-ups
    (`LibrarySelectionFollowUp`, the effects that are one, the drafts and looks that take one), "create a token. When
    you do", Earth Rumble's and the other `FightTargetsEffect` fights, Nihiloor, Kaboom! and the other reveals of
    its effect, Strax, Hidetsugu, Lazav's exile and the other become-a-copy questions, the searches of a library
    and/or graveyard, and the effects that make a permanent attacking.

Usage: python3 scripts/step15-run17z16c3c-test-list.py [--gradle] [--letters abc] [--effects]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ENGINE = ROOT / "magical-vibes-engine/src/main/java"
CHANGED = {
    "QueueReflexiveAbilityEffect", "CreateTokenThenEffect", "CreateTokenAttachedToTargetThenEffect",
    "LibrarySelectionFollowUp", "TransformSelectedPermanentFollowUp", "AttachSelectedAurasToLegalTargetsFollowUp",
    "AttachSelectedEquipmentToCreatureEffect", "AttachOneOfEquipmentToSamuraiEffect",
    "AttachOneOfEquipmentToCreatureEffect", "DraftCardFromSpellbookEffect", "TransformChosenPermanentEffect",
    "MakeChosenPermanentAttackingEffect", "MakeCreatedPermanentsAttackingEffect",
    "ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect", "ReturnSelectedPermanentToHandAtEndOfCombatEffect",
    "AttachCreatedEquipmentToTargetCreatureEffect", "SourcePermanentDealsPowerDamageToTargetCreatureEffect",
    "FightTargetsEffect", "EarthbendTargetLandThenFightEffect", "NihiloorEnterEffect",
    "RevealUntilNonlandBottomThenDealManaValueDamageEffect", "RandomPlayerThenFightsTargetCreatureEffect",
    "ExileTopCardMayPlayThisTurnThenDealManaValueDamageEffect", "ExileCardFromAnyGraveyardThenMayBecomeCopyEffect",
    "PutCounterThenMayBecomeCopyOfCardsEffect", "BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect",
    "RevealTargetPlayerLibraryUntilCreatureAndBecomeCopyUntilEndOfTurnEffect",
    "SearchLibraryAndOrGraveyardEffect", "EnteringCreatureFightsTargetCreatureEffect",
}
READERS = set()
CARD_MARKERS = re.compile(r"enterAttacking|SearchLibraryAndOrGraveyard|FollowUp\b")
NAMED = set()

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
