#!/usr/bin/env python3
"""Step 15, run 17z16c4c (read-only): the card test classes of the cards on the changed routes.

A card test class is listed when

  - it answers a "you may" question (`handleMayAbilityChosen`): the row's seven sites are all behind the answer of a
    resolving entry's "you may" (`MayAbilityHandlerService.handleResolutionTimeMayChoice`, `continueAfterTapCost`);
  - its card writes an effect of a route the run touched (`CHANGED`): the two attach effects whose may handlers are
    deleted, the three effects whose handlers still record a chosen graveyard card on the waiting entry
    (`GameData.resolvedMayTargetingEntry`), and every effect whose handler begins a permanent choice with a
    `MayAbilityTriggerTarget` context or a multi-card graveyard choice (their completions lost a branch): the effect
    classes are read from the handlers' file names.

Usage: python3 scripts/step15-run17z16c4c-test-list.py [--gradle] [--letters abc] [--cards-only]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SERVICE = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service"
CHANGED = {
    "AttachTargetEquipmentToTargetCreatureEffect", "AttachTargetAuraOrEquipmentToTargetCreatureEffect",
    "ChooseCardFromGraveyardAndGrantCastPermissionEffect", "CastCardFromGraveyardEffect",
    "MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect",
}
MARKERS = ("MayAbilityTriggerTarget(", "beginMultiGraveyardChoice(")
for path in SERVICE.glob("**/*EffectHandler.java"):
    text = path.read_text()
    if any(marker in text for marker in MARKERS):
        CHANGED.add(path.stem[:-len("Handler")])
WRITES = re.compile(r"\b(?:" + "|".join(sorted(CHANGED)) + r")\b")

letters = None
if "--letters" in sys.argv:
    letters = set(sys.argv[sys.argv.index("--letters") + 1])
cards_only = "--cards-only" in sys.argv

by_letter = {}
cards = 0
for path in sorted((ROOT / "magical-vibes-card").glob("*/src/main/java/**/*.java")):
    if not WRITES.search(path.read_text()):
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
    print(f"{len(CHANGED)} effect classes, {cards} card classes on the changed routes ({on_routes} test classes); "
          f"{answering} test classes answer a \"you may\"; {sum(len(names) for names in by_letter.values())} test "
          f"classes in {len(by_letter)} modules", file=sys.stderr)
