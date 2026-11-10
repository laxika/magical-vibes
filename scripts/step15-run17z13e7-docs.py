#!/usr/bin/env python3
"""Step 15, run 17z13e7: agent-docs follow the new defaults.

- Relic Bind's "choose one" goes on the stack through the enchanted-tap default, its mode and that mode's target
  announced there (`StackedModalTriggerService`), not through a class-keyed `TriggeredModalTrigger` route
  (`CARD_PATTERNS_PERMANENTS_STATIC.md`, `ORACLE_TEXT_EFFECT_MAP.md`, `EFFECTS_INDEX.md`).
- The investigate watches' collector is `MiscTriggerCollectorService`'s player-action default
  (`TRIGGER_SLOT_TARGETING.md`; `InvestigateTriggerCollectorService` is gone), and the table gains the other watches
  whose defaults run 17z13e7 wrote.
- A cast trigger without a filter is "any spell" (`CARD_PATTERNS_CREATURES_TRIGGERED.md`, `CARD_PATTERN_INDEX.md`).

Fails if a doc still names a deleted collector or route. Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("The card's modal goes to the trigger-time mode choice (`TriggeredModalTrigger`, CR 603.3c) through the "
         "enchanted-tap watch's `ChooseOneEffect` route |",
         "The enchanted-tap default puts the modal on the stack with the tapped permanent and its controller recorded; "
         "its mode and that mode's target are announced there before priority (`StackedModalTriggerService`, "
         "CR 603.3c) |"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("Relic Bind. The card's modal goes to the trigger-time mode choice as written (`TriggeredModalTrigger`, "
         "CR 603.3c), then the chosen mode's target |",
         "Relic Bind. The enchanted-tap default puts the modal on the stack as written; its mode, then that mode's "
         "target, is announced there (`StackedModalTriggerService`, CR 603.3c) |"),
    ],
    "EFFECTS_INDEX.md": [
        ("the enchanted-tap watch's `ChooseOneEffect` route queues the card's modal as a `TriggeredModalTrigger` (mode "
         "and target chosen as the ability goes on the stack, CR 603.3c), the tapped permanent as its triggering "
         "permanent. |",
         "the enchanted-tap default puts the card's modal on the stack with the tapped permanent and its controller "
         "recorded, and its mode and target are announced there as the ability goes on the stack "
         "(`StackedModalTriggerService`, CR 603.3c). |"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("| `Triggers.youInvestigateFirstTimeEachTurn()` | `InvestigateTriggerCollectorService` | The controller's first "
         "investigate event each turn; non-targeting effects enqueue directly |\n"
         "| `Triggers.youInvestigate()` | `InvestigateTriggerCollectorService` | Every controller investigate event; "
         "non-targeting effects enqueue directly |",
         "| `Triggers.youInvestigateFirstTimeEachTurn()` / `Triggers.youInvestigate()` / `Triggers.youCollectEvidence()` "
         "/ `Triggers.youForetell()` | `MiscTriggerCollectorService.handlePlayerActionDefault` | The controller's first "
         "(or every) investigation, evidence collected, a card foretold; every entry records the acting player (and the "
         "foretold card) whatever the effects are (`queueEventAbility`), a permanent or player target chosen through "
         "`SpellTargetTriggerAnyTarget` with the same record |\n"
         "| `Triggers.opponentPaysTax()`, `Triggers.youGetEnergy()`, `Triggers.youPutCountersOnPermanentOrPlayer()`, "
         "`Triggers.youPutLoreCounterOnSagaYouControl()`, `Triggers.loyaltyCountersRemovedFromThis()`, "
         "`Triggers.countersRemovedFromPermanentYouControl()`, `Triggers.opponentGainsControlOfPermanentYouControl()`, "
         "`Triggers.thisManaAbilityResolves()` | `MiscTriggerCollectorService` (one default each, "
         "`queueEventAbility`) | The player, the count (\"that much\", \"that many\": the event value) and the permanent "
         "the event was about with its controller, whatever the effects are; a target through `SpellTargetTriggerAnyTarget` "
         "with the card's target group filter (\"target opponent\" the opponents) and the same record; the paying "
         "opponent and the new controller the non-targeting \"that player\" |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("> (`.anySpell()` for a null filter), with",
         "> (`.anySpell()`, or no filter, for any spell), with"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("> (`.anySpell()` for a null filter), with",
         "> (`.anySpell()`, or no filter, for any spell), with"),
    ],
}
GONE = ["InvestigateTriggerCollectorService", "LoyaltyCounterTriggerCollectorService",
        "SagaCounterTriggerCollectorService", "CollectEvidenceTriggerCollectorService", "`ChooseOneEffect` route"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                sys.exit(f"{name}: pattern not found: {old[:80]!r}")
            text = text.replace(old, new, 1)
        path.write_text(text)
    left = [f"{path.name}: {gone}" for path in DOCS.glob("*.md") for gone in GONE if gone in path.read_text()]
    if left:
        sys.exit(f"still described: {left}")


if __name__ == "__main__":
    main()
