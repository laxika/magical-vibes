#!/usr/bin/env python3
"""Step 15, run 17z12b: removes the enter and turned-face-up collectors' class-keyed handlers.

Each handler recorded the entering (or turned-face-up) permanent's data only for its effect class, bound it as the
ability's source or `targetId`, or both. The defaults now record the event's permanent for every ability and the cards
say what their effects read (see effect-slot-refactor.md, run 17z12b's Findings), so the handlers go with their
`@Collects...Trigger` annotations and Javadoc.
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
T = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger"
REMOVE = {
    "EnterTriggerCollectorService.java": [
        "handleOpponentTokenEnterCreateCopies", "handleAnyCreatureDrawEnteringController", "handleAllyCreatureExplore",
        "handlePowerToughnessDifferenceLifeLoss", "handleLifeLossToEnteringController", "handleAnyCreatureSacrifice",
        "handleAllyCreatureEntersDealsPowerDamage", "handleAllyCreatureDealsPowerDamageToEachOpponent",
        "handleAllyNontokenCreatureCreateTokenCopy", "handleOpponentCreatureCreateTokenCopyThenExileOthers",
        "handleOpponentCreatureExileTrackedTokensAndCreateTokenCopy", "handleAnyCreatureSacrificeSelfThen",
        "handleAnyCreatureSacrificeSelfDealDamage"],
    "FaceUpTriggerCollectorService.java": ["handleTurnedCreatureDealsPowerDamageToEachOpponent"],
}


def remove_method(text, name):
    sig = re.search(r"\n    private boolean " + name + r"\(", text)
    assert sig, name
    # Walk back over the annotations, Javadoc and blank line that precede the method.
    start = sig.start()
    lines_before = text[:start].split("\n")
    while lines_before and (lines_before[-1].strip().startswith("@") or lines_before[-1].strip().startswith("watch")
                            or lines_before[-1].strip().startswith("*") or lines_before[-1].strip().startswith("/**")
                            or lines_before[-1].strip() == ""):
        lines_before.pop()
    start = len("\n".join(lines_before))
    brace = text.index("{", sig.end())
    depth, i = 1, brace + 1
    while depth:
        depth += {"{": 1, "}": -1}.get(text[i], 0)
        i += 1
    return text[:start] + "\n" + text[i:]


for file, names in REMOVE.items():
    path = T / file
    text = path.read_text()
    for name in names:
        if not re.search(r"\n    private boolean " + name + r"\(", text):
            continue
        text = remove_method(text, name)
    path.write_text(text)
    print(file, len(names), "removed")
