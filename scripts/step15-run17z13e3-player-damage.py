#!/usr/bin/env python3
"""Step 15, run 17z13e3: the damage-to-a-player watches have one default each that records the event.

"Whenever you're dealt damage", "whenever a source an opponent controls deals damage to you", "whenever an opponent is
dealt damage", "whenever a source you control deals damage to an opponent / a player", the plane's "a creature deals
damage to its controller", "whenever a source you control deals noncombat damage to an opponent", "whenever an
opponent is dealt noncombat damage" and "whenever a source an opponent controls deals damage to you or a permanent you
control" each had class-keyed handlers that recorded different parts of the event (the damage only for some effects,
"that player" for some, the source for one) and judged trigger conditions by an effect's class or fields (Talon of
Pain's `excludeDamageSource`, Flesh Reaver's "this creature", Dragonborn Champion's "5 or more", Niv-Mizzet's and
Chandra's Incinerator's "a source you control"). `DamageTriggerCollectorService.handlePlayerDamageDefault` and
`handleOpponentSourceDamageDefault` record the event whatever the effects are; the conditions are the cards' triggers,
filters and event conditions (`step15-run17z13e3-cards.py`). The handlers go with their annotations and Javadoc, the
old defaults of the watches with them, and the banner comments of the damage sections. Idempotent.
"""
import pathlib
import re
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from step15_handler_removal import has_method, remove_method  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parent.parent
TRIGGER = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger"
REMOVE = {
    "DamageTriggerCollectorService.java": [
        "handleAllySourceDealtDamageToPlayer", "handleAllySourceDealsDamageToOpponentPutCounter",
        "handleControllerDealtDamageDefault", "handleControllerDealtDamageSacrificeOrLose",
        "handleControllerDealtDamageMay", "handleControllerDealtDamageRemoveCounter",
        "handleAllySourceDealtDamageToOpponentPutCounters", "handleAllySourceDealtDamageToOpponentMay",
        "handleAllySourceDealtDamageToOpponentThresholdDraw", "queueAllySourceDealtDamageToOpponentTrigger",
        "handleSelfDealsDamageToOpponent", "handleOpponentDealtDamagePutCounters", "handleOpponentDealtDamageDefault",
        "handleControllerDealtDamageByOpponentSacrifice", "handleControllerDealtDamageByOpponentMay",
        "handleControllerDealtDamageByOpponentDefault", "queueControllerDealtDamageByOpponentTrigger",
        "handleOpponentSourceDamageToYouOrYourPermanent", "handleOpponentSourceDamageLifeUnlessPays",
        "handleConditionalAllySourceDealtNoncombatDamageToOpponent",
        "queueAllySourceDealtNoncombatDamageToOpponentTrigger",
    ],
    "MiscTriggerCollectorService.java": [
        "handleNoncombatDamageBoostSelf", "handleNoncombatDamageBoostAllOwnCreatures", "enqueueNoncombatDamageTrigger",
        "handleNoncombatDamageDraw", "handleAllySourceDealtNoncombatDamageToOpponent",
        "handleAllySourceDealtNoncombatDamageToOpponentToken", "handleAllySourceDealtNoncombatDamageToOpponentMay",
        "handleNoncombatDamageToDamagedPlayerPermanent",
    ],
}
# The banner comments of the two files' damage sections (AGENTS.md: no banner comments): every one of the damage
# collector, and the noncombat-damage one of the misc collector.
BANNERS = {
    "DamageTriggerCollectorService.java": re.compile(r"\n    // ── [^\n]*─\n"),
    "MiscTriggerCollectorService.java": re.compile(r"\n    // ── ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE ─+\n"),
}


def main():
    for name, methods in REMOVE.items():
        path = TRIGGER / name
        text = path.read_text()
        for method in methods:
            if has_method(text, method):
                text = remove_method(text, method)
        text = BANNERS[name].sub("\n", text)
        while "\n\n\n" in text:
            text = text.replace("\n\n\n", "\n\n")
        if text.endswith("\n\n}\n"):
            text = text[:-len("\n\n}\n")] + "\n}\n"
        path.write_text(text)
        print(f"{name}: {sum(1 for m in methods if not has_method(text, m))} of {len(methods)} gone")


if __name__ == "__main__":
    main()
