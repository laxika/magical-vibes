#!/usr/bin/env python3
"""Step 15, run 17z13e: agent-docs follow the life and enchanted-tap defaults and the converted cards.

"Its controller" of an enchanted-tap ability reads the tapped permanent the entry records
(`TRIGGERING_PERMANENT_CONTROLLER`), never a baked `targetId`; "whenever an opponent loses exactly 1 life" is an event
condition on the event's value; Haphazard Bombardment's "if" is its intervening "if"; every life ability records the
life event. Fails if a doc still describes the baked targets. Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ('| "Whenever enchanted land becomes tapped, its controller mills N cards" | `MillEffect(N, MillRecipient.TARGET_PLAYER)` | `Triggers.enchantedPermanentBecomesTapped()` | Chronic Flooding. The collector bakes the tapped land\'s controller as `targetId`, so no player target is chosen.',
         '| "Whenever enchanted land becomes tapped, its controller mills N cards" | `MillEffect(N, MillRecipient.TRIGGERING_PERMANENT_CONTROLLER)` | `Triggers.enchantedPermanentBecomesTapped()` | Chronic Flooding. The enchanted-tap collector records the tapped land as the entry\'s triggering permanent whatever the effect is; its controller is read as the ability resolves (CR 608.2h), else its last known.'),
        ('Psychic Venom. The `Triggers.enchantedPermanentBecomesTapped()` collector bakes the damage target to the tapped permanent\'s controller.',
         'Psychic Venom. The `Triggers.enchantedPermanentBecomesTapped()` collector records the tapped permanent and its controller on every entry; the recipient reads them as the ability resolves.'),
        ('| "Whenever enchanted land becomes tapped, its controller loses N life" | `LoseLifeEffect(N, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER)` | `Triggers.enchantedPermanentBecomesTapped()` | Corrupted Roots. Life loss, **not** damage (CR 118.2) — fires "loses life" triggers and can\'t be prevented. The collector bakes the tapped land into the entry\'s `targetId` so `TARGET_PERMANENT_CONTROLLER` re-derives its controller.',
         '| "Whenever enchanted land becomes tapped, its controller loses N life" | `LoseLifeEffect(N, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)` | `Triggers.enchantedPermanentBecomesTapped()` | Corrupted Roots. Life loss, **not** damage (CR 118.2) — fires "loses life" triggers and can\'t be prevented. The recipient reads the tapped land the entry records (its controller as the ability resolves, else its last known).'),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("Triggers.enchantedPermanentBecomesTapped() MillEffect(3, MillRecipient.TARGET_PLAYER). \"Whenever enchanted land becomes tapped, its controller mills three cards\" — the collector bakes the tapped land's controller as `targetId`, so no player target is chosen |",
         "Triggers.enchantedPermanentBecomesTapped() MillEffect(3, MillRecipient.TRIGGERING_PERMANENT_CONTROLLER). \"Whenever enchanted land becomes tapped, its controller mills three cards\" — the recipient reads the tapped land the entry records (its controller as the ability resolves), so no player target is chosen |"),
    ],
    "EFFECTS_INDEX.md": [
        ('Place in `Triggers.endStep(ActivePlayer.YOU)`. Intervening-if checked in `StepTriggerService`; resolved by `DestructionResolutionService`. Used by Haphazard Bombardment (AIM, 2) |',
         'Place in `Triggers.endStep(ActivePlayer.YOU)` with the intervening "if" written as the ability\'s option (`TriggerOptions.DEFAULT.withInterveningIf(new AnyPlayerControlsPermanentCount(2, PermanentAllOf(PermanentHasCounters(AIM), PermanentNot(PermanentControlledBySourceController))))`, CR 603.4); the effect checks the count again as it resolves. Used by Haphazard Bombardment (AIM, 2) |'),
    ],
}
APPEND = {
    "ORACLE_TEXT_EFFECT_MAP.md": (
        '| "Whenever one or more opponents each lose exactly 1 life, [effect]" | the effect, with `TriggerOptions.DEFAULT.withEventCondition(new AllOf(List.of(new EventValueAtLeast(1), new NotCondition(new EventValueAtLeast(2)))))` | `Triggers.opponentLosesLife()` | Ob Nixilis, Captive Kingpin. The life lost is the event\'s value, which the event condition reads as the ability triggers; every life ability records the life gained, lost or paid as its entry\'s event value, the player as its triggering player and non-targeting "that player", whatever its effects |\n'),
}
STALE = ["bakes the tapped land", "bakes the tapped land's controller", "Intervening-if checked in `StepTriggerService`"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            assert old in text, f"{name}: {old[:80]}"
            text = text.replace(old, new, 1)
        if name in APPEND and APPEND[name] not in text:
            anchor = '| "Whenever enchanted land becomes tapped, its controller loses N life"'
            line_end = text.index("\n", text.index(anchor)) + 1
            text = text[:line_end] + APPEND[name] + text[line_end:]
        path.write_text(text)
    stale = [(p.name, s) for p in DOCS.glob("*.md") for s in STALE if s in p.read_text()]
    if stale:
        sys.exit(f"stale: {stale}")


if __name__ == "__main__":
    main()
