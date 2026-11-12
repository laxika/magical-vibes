#!/usr/bin/env python3
"""Step 15, run 17z16c4b (read-only): the card test classes of the cards on the changed routes.

A card test class is listed when

  - it answers a "you may" question (`handleMayAbilityChosen`): every such answer goes through
    `MayAbilityHandlerService.handleMayAbilityChosen`, whose exert and text-box branches and whose generic route
    changed (`--answers`, the wide list);
  - its card writes an effect whose handler asks a question on the generic route, or one of the row's other sites
    (`CHANGED`), exert or any other `SkipNextUntapEffect`, or is one of the named cards (`NAMED`: the question is
    asked by a choice handler, not by the card's effect).

Usage: python3 scripts/step15-run17z16c4b-test-list.py [--gradle] [--letters abc] [--cards-only]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CHANGED = {
    "ChooseUpToNMatchingCreaturesThenMayExileRestEffect", "CopySpellForEachOtherPlayerEffect",
    "DamageSourceControllerExilesRandomHandCardEffect", "EachOpponentOfTriggeringPlayerMayDrawCardEffect",
    "MayCastPermanentSpellFromHandOrPutLandEffect",
    "SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffect",
    "DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect",
    "MayCastLesserSpellWithSharedTypeOrCreateTokenEffect", "AttachTargetEquipmentToTargetCreatureEffect",
    "AttachTargetAuraOrEquipmentToTargetCreatureEffect", "ExchangeControlOfSourceAndTriggeringSpellEffect",
    "ExchangeTextBoxesEffect", "SkipNextUntapEffect", "TradeSecretsRepeatEffect",
}
CARD_MARKERS = re.compile(r"[Ee]xert|youExertCreature")
NAMED = {"LiarsPendulum", "RonaHeraldOfInvasion", "TibaltWickedTormentor"}
WRITES = re.compile(r"\b(?:" + "|".join(sorted(CHANGED)) + r")\b")

letters = None
if "--letters" in sys.argv:
    letters = set(sys.argv[sys.argv.index("--letters") + 1])
cards_only = "--cards-only" in sys.argv

by_letter = {}
cards = 0
for path in sorted((ROOT / "magical-vibes-card").glob("*/src/main/java/**/*.java")):
    text = path.read_text()
    if not (WRITES.search(text) or CARD_MARKERS.search(text) or path.stem in NAMED):
        continue
    cards += 1
    letter = path.stem[0].lower()
    test = (ROOT / "magical-vibes-card-test" / letter / "src/test/java/com/github/laxika/magicalvibes/cards"
            / letter / (path.stem + "Test.java"))
    if test.exists() and (letters is None or letter in letters):
        by_letter.setdefault(letter, set()).add(path.stem + "Test")
on_routes = sum(len(names) for names in by_letter.values())

answering = 0
if not cards_only:
    for path in sorted((ROOT / "magical-vibes-card-test").glob("*/src/test/java/**/*Test.java")):
        letter = path.relative_to(ROOT / "magical-vibes-card-test").parts[0]
        if letters is not None and letter not in letters:
            continue
        if "handleMayAbilityChosen" in path.read_text():
            answering += 1
            by_letter.setdefault(letter, set()).add(path.stem)

if "--gradle" in sys.argv:
    print(" ".join(f":magical-vibes-card-test:{letter}:test " + " ".join(f"--tests *.{name}" for name in sorted(names))
                   for letter, names in sorted(by_letter.items())))
else:
    for letter, names in sorted(by_letter.items()):
        for name in sorted(names):
            print(letter, name)
    print(f"{cards} card classes on the changed routes ({on_routes} test classes); {answering} test classes answer "
          f"a \"you may\"; {sum(len(names) for names in by_letter.values())} test classes in {len(by_letter)} modules",
          file=sys.stderr)
