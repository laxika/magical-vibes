#!/usr/bin/env python3
"""Step 15, run 17z13e9: the permanent-damage and combat watches have one default each that records the event.

"Whenever this is dealt damage" (and "... noncombat damage") had class-keyed handlers for
`DealDamageToTargetPlayerOrPlaneswalkerEffect` and `DealDamageToAnyTargetEffect` (target routes recording no triggering
source), High Priest of Penance's `MayEffect` and Archfiend of Spite's effect (its "a source an opponent controls"
judged by the class); "whenever a permanent you control is dealt damage" had no default and two handlers (the Wrathful
pair and Howlpack Avenger, Donna Noble's `TARGET_PLAYER` check); "whenever a source you control deals noncombat damage to
a creature" had no default and three handlers (Taii Wakeen's "equal to that creature's toughness" judged by its
`DrawCardEffect` handler); "whenever a creature is dealt damage" had a `MayEffect` handler doing what the default does;
Rankle and Torbran's modal chosen at trigger time had a handler of its own. The defaults
(`DamageTriggerCollectorService.handleDealtDamageDefault`, `handleAnyPermanentDealtDamageDefault`,
`handleAllySourceDealsNoncombatDamageToCreatureDefault`, `handleAnyCreatureDealtDamageDefault`,
`handleSelfDealsCombatDamage`) were rewritten by hand; this removes the class-keyed handlers with their annotations and
Javadoc, and the banner comments left in the damage collector. Idempotent.
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
        "handleAllySourceDealsNoncombatDamageToCreatureDraw",
        "handleAllySourceDealsNoncombatDamageToCreatureConditional",
        "handleAllySourceDealsNoncombatDamageToCreatureBoost",
        "enqueueAllySourceDealsNoncombatDamageToCreature",
        "handleOpponentDamageSourceDealtDamage",
        "handleDealtDamageTargetPlayerOrPlaneswalker",
        "handleDealtDamageToAnyTarget",
        "handleAnyPermanentDealtDamageToAnyTarget",
        "queueAnyPermanentDealtDamageTrigger",
        "handleAnyPermanentDealtDamageToPlayer",
        "handleAnyCreatureDealtDamageMay",
        "handleSelfDealsCombatDamageModalAtTriggerTime",
        "handleDealtDamageMayEffect",
        "handleNoncombatDamageToSelfDefault",
    ],
}
BANNER = re.compile(r"\n    // (?:──|=====)[^\n]*\n")


def main():
    for name, methods in REMOVE.items():
        path = TRIGGER / name
        text = path.read_text()
        for method in methods:
            if has_method(text, method):
                text = remove_method(text, method)
        text = BANNER.sub("\n", text)
        while "\n\n\n" in text:
            text = text.replace("\n\n\n", "\n\n")
        if text.endswith("\n\n}\n"):
            text = text[:-len("\n\n}\n")] + "\n}\n"
        path.write_text(text)
        print(f"{name}: {sum(1 for m in methods if not has_method(text, m))} of {len(methods)} gone")


if __name__ == "__main__":
    main()
