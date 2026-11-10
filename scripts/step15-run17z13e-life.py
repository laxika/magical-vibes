#!/usr/bin/env python3
"""Step 15, run 17z13e: the life collectors' class-keyed handlers go.

The life watches' handlers recorded the event for some effect classes only: the life amount as the event's value only
when the effect's amount named it (or never: Kavu Predator's, the token, counter, surveil, untap and prepared handlers),
the losing player as "that player" only for a mill, the source snapshot only for a sequence or a "you may", the "do this
only once each turn" mark only for a "you may"; "whenever you lose life" had no default (an effect no handler named
didn't trigger), two `SequenceEffect` keys were registered twice on one watch, and Ob Nixilis, Captive Kingpin's
"exactly 1 life" was judged by its sequence's class. `LifeGainTriggerCollectorService.handleLifeDefault` records the
event for every ability of every life watch (effect-slot-refactor.md, run 17z13e's Findings), so these handlers go with
their annotations and Javadoc. Idempotent.
"""
import pathlib
import re
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from step15_handler_removal import has_method, remove_method  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parent.parent
T = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger"
REMOVE = {
    "MiscTriggerCollectorService.java": [
        "handleOpponentLifeLossCastFromGraveyard", "handleMillOnLifeLoss", "handleGainLifeOnOpponentLifeLoss",
        "handleSequenceOnOpponentLifeLoss", "handleOnOpponentLifeLoss", "handleOnOpponentLifeGain",
        "handleSequenceOnControllerLifeLoss", "handleLifePaymentPutCountersOnSelf",
        "handleGainLifeOnControllerLifeLoss", "handleLifeGainPutCounters", "handleLifeGainGrantKeyword",
        "handleLifeGainSequence", "handleConditionalOnControllerLifeChange", "handleLifeGainPutCounterOnTarget",
        "handleLifeGainSurveil", "handleLifeGainCreateToken", "handleLifeGainPutCountersOnSelf",
        "handleLifeChangeBoostSelf", "handleLifeGainUntapSelf", "handleLifeGainEnergyCounters",
        "handleLifeGainDrawCard", "handleLifeGainPutCountersOnMatching", "handleLifeGainPayForDraw",
        "handleSacrificePermanentsOnLifeLoss", "handleSacrificeOtherUnlessDiscardForEachLifeLost",
        "handleLifeLossDrawCards", "handleExileForEachLifeLost", "handleLifeLossEnergyCounters",
        "handleLifeLossPutCounters", "handleLifeLossPutCounterOnTarget", "handleGenericControllerLifeLoss",
        "handleLifeGainBecomePrepared"],
}

for file, names in REMOVE.items():
    path = T / file
    text = path.read_text()
    removed = 0
    for name in names:
        if has_method(text, name):
            text = remove_method(text, name)
            removed += 1
    # The life handlers' divider comments and an orphaned Javadoc of a long-gone Lich's Mastery helper go too.
    text = re.sub(r"\n    // ── ON_(?:CONTROLLER|OPPONENT)_(?:GAINS|LOSES)_LIFE[^\n]*\n", "\n", text)
    text = re.sub(r"\n    /\*\*\n     \* Exiles cards/permanents for Lich's Mastery\.[^/]*?\*/\n", "\n", text)
    text = re.sub(r"\n{3,}", "\n\n", text)
    path.write_text(text)
    print(file, removed, "removed")
