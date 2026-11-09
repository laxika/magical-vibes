#!/usr/bin/env python3
"""Step 15, run 17z12f: `PermanentChoiceContext.EnteringPermanentAnyTargetTrigger` goes.

The creature-enters-from-graveyard default queued it to make the entering permanent the source of every targeted
ability on its watch (Flayer of the Hatebound); the default now queues the enters target choice with the watcher as
the ability's source, and Flayer's effect reads the entry's triggering permanent. This deletes the record, its queue
processing (`TriggeredAbilityQueueService`, `TriggerCollectionService`, `StackResolutionService`, `AutoPassService`)
and its answer (`PermanentChoiceHandlerService`, `PermanentChoiceTriggerHandlerService`).
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
ENGINE = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service"
DOMAIN = ROOT / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model"


def cut_block(text, start_marker, end_marker, label):
    """Removes text from start_marker up to (not including) end_marker; both must occur once."""
    assert text.count(start_marker) == 1, (label, start_marker)
    start = text.index(start_marker)
    end = text.index(end_marker, start)
    return text[:start] + text[end:]


def edit(path, fn):
    text = path.read_text()
    new = fn(text)
    assert new != text, path
    path.write_text(new)
    print("edited", path.relative_to(ROOT))


edit(DOMAIN / "PermanentChoiceContext.java", lambda t: cut_block(
    t, "    /** An enters-the-battlefield trigger that needs an \"any target\" choice and whose effect resolves\n",
    "    /**\n     * ETB trigger that needs to target a spell on the stack", "record"))

edit(ENGINE / "TriggeredAbilityQueueService.java", lambda t: cut_block(
    t, "    public void processNextEnteringPermanentAnyTarget(GameData gameData) {\n",
    "    /**\n     * Every permanent on every battlefield that CR 115.4 admits", "queue"))

edit(ENGINE / "trigger/TriggerCollectionService.java", lambda t: cut_block(
    t, "    public void processNextEnteringPermanentAnyTarget(GameData gameData) {\n",
    "    public void processNextSagaChapterTarget(GameData gameData) {\n", "collection"))

edit(ENGINE / "StackResolutionService.java", lambda t: cut_block(
    t, "        if (gameData.hasPendingInteraction(PermanentChoiceContext.EnteringPermanentAnyTargetTrigger.class)) {\n",
    "        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {\n", "stack"))

edit(ENGINE / "turn/AutoPassService.java", lambda t: cut_block(
    t, "        // Process any pending enters-from-graveyard targeted triggers\n",
    "        // Process any pending saga chapter targeted triggers\n", "autopass"))

edit(ENGINE / "input/PermanentChoiceHandlerService.java", lambda t: cut_block(
    t, "        } else if (context instanceof PermanentChoiceContext.EnteringPermanentAnyTargetTrigger efg) {\n",
    "        } else if (context instanceof PermanentChoiceContext.ETBSpellTargetTrigger etbStt) {\n", "handler"))


def remove_trigger_handler(text):
    start_marker = "    public void handleEnteringPermanentAnyTarget(GameData gameData, UUID targetId,\n"
    assert text.count(start_marker) == 1
    start = text.index(start_marker)
    end = text.index("\n    }\n", start) + len("\n    }\n")
    # The blank line after the method goes with it.
    if text[end:end + 1] == "\n":
        end += 1
    return text[:start] + text[end:]


edit(ENGINE / "input/PermanentChoiceTriggerHandlerService.java", remove_trigger_handler)
