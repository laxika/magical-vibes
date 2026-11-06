#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 12: exact agent-docs edits for "those cards" read from the stack entry.

Idempotent: each edit is skipped when its new text is already there. Run from the repository root:

    python3 scripts/step15-triggering-cards-docs.py
"""
import pathlib

OLD_CISTERN = ("`EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect()` | "
               "`Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` | "
               "Polluted Cistern // Dim Oubliette (`DSC` 23); the trigger collector preserves the cards from that one "
               "event so distinct printed card types are counted once |")
NEW_CISTERN = ("`LoseLifeEffect(new CardTypesAmongTriggeringCards(), LoseLifeRecipient.EACH_OPPONENT)` with "
               "`TriggerOptions.ONE_OR_MORE` | "
               "`Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` | "
               "Polluted Cistern // Dim Oubliette (`DSC` 23); the collector sets the cards of that one event as the "
               "stack entry's triggering cards, and the amount counts each card type among them once |")

EDITS = {
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        (OLD_CISTERN, NEW_CISTERN),
        ("Spirit of Resilience (SOC 37). The collector snapshots all cards in the departure event; the counter resolves "
         "before the optional copy choice, which filters the captured cards to non-token artifact or creature cards. |",
         "Spirit of Resilience (SOC 37). The collector sets the cards of the departure event as the stack entry's "
         "triggering cards; the counter resolves before the optional copy choice, which filters them to non-token "
         "artifact or creature cards. |"),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("| `SeekCardSharingCardTypeWithDiscardedCardsEffect` | `()` | `Triggers.youDiscardOneOrMoreCards()`: randomly "
         "seeks one card sharing a card type with any card discarded in the event into the controller's hand. Used by "
         "Diviner of Fates (`YSNC` 22) |",
         "| `SeekCardSharingCardTypeWithDiscardedCardsEffect` | `()` | `Triggers.youDiscardOneOrMoreCards()`: randomly "
         "seeks one card sharing a card type with any card discarded in the event (the stack entry's triggering "
         "cards) into the controller's hand. Used by Diviner of Fates (`YSNC` 22) |"),
        ("| `EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect` | `()` | "
         "`Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` trigger; each "
         "opponent loses life equal to the number of distinct printed card types among the cards in that one "
         "library-to-graveyard event. Used by Polluted Cistern // Dim Oubliette (`DSC` 23) |\n",
         ""),
        ("- `LifeLostWhenTriggeringPermanentEntered()` - \"the life you lost when it entered\"",
         "- `CardTypesAmongTriggeringCards()` - \"for each card type among those cards\": the card types among the\n"
         "  stack entry's triggering cards, the cards a \"one or more cards\" event was about (discarded, put into a\n"
         "  graveyard from a library, left a graveyard), each type once; 0 when the entry has none. Polluted Cistern's\n"
         "  `LoseLifeEffect(new CardTypesAmongTriggeringCards(), LoseLifeRecipient.EACH_OPPONENT)`\n"
         "- `LifeLostWhenTriggeringPermanentEntered()` - \"the life you lost when it entered\""),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("- `PutCounterThenMayBecomeCopyOfCardsEffect()` — event-bound trigger effect for Spirit of Resilience: put a "
         "+1/+1 counter on the source, then offer a may choice to copy one captured artifact or creature card that "
         "left its controller's graveyard until end of turn",
         "- `PutCounterThenMayBecomeCopyOfCardsEffect()` — Spirit of Resilience's trigger effect: put a +1/+1 counter "
         "on the source, then offer a may choice to copy, until end of turn, one artifact or creature card among the "
         "stack entry's triggering cards (the cards that left its controller's graveyard)"),
        ("- `EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect()` — "
         "`Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` trigger; each "
         "opponent loses life equal to the number of distinct card types among the cards in that one "
         "library-to-graveyard event. Used by Polluted Cistern // Dim Oubliette (`DSC` 23).",
         "- `LoseLifeEffect(new CardTypesAmongTriggeringCards(), LoseLifeRecipient.EACH_OPPONENT)` — "
         "`Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` with "
         "`TriggerOptions.ONE_OR_MORE`: each opponent loses 1 life for each card type among the cards of that one "
         "library-to-graveyard event, the stack entry's triggering cards. Used by Polluted Cistern // Dim Oubliette "
         "(`DSC` 23)."),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("`PutCounterThenMayBecomeCopyOfCardsEffect()` — the departure collector captures the event's cards, resolves "
         "the +1/+1 counter first,",
         "`PutCounterThenMayBecomeCopyOfCardsEffect()` — the departure collector sets the event's cards as the stack "
         "entry's triggering cards; the effect resolves the +1/+1 counter first,"),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = pathlib.Path(name)
        text = path.read_text()
        for old, new in edits:
            if new and new in text:
                continue
            if old not in text:
                if not new:
                    continue
                raise SystemExit(f"{name}: neither the old nor the new form of {old[:80]!r}")
            if text.count(old) != 1:
                raise SystemExit(f"{name}: {old[:80]!r} is not unique")
            text = text.replace(old, new)
        path.write_text(text)


if __name__ == "__main__":
    main()
