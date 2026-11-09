#!/usr/bin/env python3
"""Step 15, run 17z12d: agent-docs follow the run (the trigger filters of Hedge Shredder and Oskar, the triggering cards
the cards-put-into-a-graveyard and discard collectors record, the source card's graveyard stay apart from the
triggering card's)."""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"


def edit(name, pairs):
    path = DOCS / name
    src = path.read_text()
    for old, new in pairs:
        if src.count(old) != 1:
            sys.exit(f"{name}: expected one match of\n{old}")
        src = src.replace(old, new)
    path.write_text(src)
    print("updated", name)


edit("EFFECTS_INDEX.md", [
    ("returns the land cards the trigger event was about — the stack entry's event cards, else its triggering card, each "
     "only if it is still the graveyard object the event named — to the battlefield under the ability controller's (= "
     "graveyard owner's) control; optionally tapped. On `Triggers.dies(Triggers.LAND, ObjectRelation.YOU_OWN).causedBy("
     "PlayerRelation.OPPONENT)` or `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)`.",
     "returns the land cards the trigger event was about — the stack entry's triggering cards, else its triggering card, "
     "each only if it is still the graveyard object the event named — to the battlefield under the ability controller's "
     "(= graveyard owner's) control; optionally tapped. On `Triggers.dies(Triggers.LAND, ObjectRelation.YOU_OWN).causedBy("
     "PlayerRelation.OPPONENT)` or, \"one or more land cards\", `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, "
     "Triggers.LAND_CARD, ObjectRelation.YOU_OWN)` with `TriggerOptions.ONE_OR_MORE` (the event's lands only)."),
    ("    .eventCardIdsOnly(boolean)                 // true = only resolution-time choices whose ids were recorded by a preceding event on this stack entry",
     "    .eventCardIdsOnly(boolean)                 // true = only the cards a preceding event on this stack entry recorded, else the entry's triggering cards (\"one of them\")"),
    ("discard-trigger marker: remembers the exact discarded nonland card and offers it from its controller's graveyard for "
     "a normal-cost cast. `DiscardTriggerCollectorService` carries the card ID and graveyard-entry version;",
     "discard-trigger effect: offers the exact discarded card (the stack entry's triggering card, in the graveyard stay "
     "the discard collector recorded) from its controller's graveyard for a normal-cost cast; \"a nonland card\" is the "
     "trigger's filter (`Triggers.youDiscardCard().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.LAND)))`);"),
])
edit("EFFECTS_QUICK_REFERENCE.md", [
    ("(the stack entry's event cards, else its triggering card, each only if still the same graveyard object) to the "
     "battlefield under their owner's control, optionally tapped. On `Triggers.dies(Triggers.LAND, ObjectRelation.YOU_OWN)"
     ".causedBy(PlayerRelation.OPPONENT)` (Sacred Ground) or `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, "
     "ObjectRelation.YOU_OWN)` (Hedge Shredder).",
     "(the stack entry's triggering cards, else its triggering card, each only if still the same graveyard object) to the "
     "battlefield under their owner's control, optionally tapped. On `Triggers.dies(Triggers.LAND, ObjectRelation.YOU_OWN)"
     ".causedBy(PlayerRelation.OPPONENT)` (Sacred Ground) or `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.LAND_CARD, "
     "ObjectRelation.YOU_OWN)` with `TriggerOptions.ONE_OR_MORE` (Hedge Shredder: \"one or more land cards\" is the filter)."),
    ("restricts a resolution-time graveyard choice to cards whose ids were recorded by a preceding event on the same stack "
     "entry;",
     "restricts a resolution-time graveyard choice to cards whose ids were recorded by a preceding event on the same stack "
     "entry, else to the entry's triggering cards (Colossal Grave-Reaver's \"one of them\", on a `Triggers.NONTOKEN_CREATURE` "
     "\"one or more creature cards\" trigger);"),
    ("discard-trigger marker that offers the exact discarded nonland card from its controller's graveyard for a "
     "normal-cost cast. The discard collector preserves the card ID and graveyard-entry version; Oskar, Rubbish Reclaimer.",
     "discard-trigger effect that offers the exact discarded card (the entry's triggering card, in the graveyard stay the "
     "discard collector recorded) from its controller's graveyard for a normal-cost cast; \"a nonland card\" is the "
     "trigger's filter. Oskar, Rubbish Reclaimer."),
])
edit("ORACLE_TEXT_EFFECT_MAP.md", [
    ("`ReturnTriggeringLandFromGraveyardToBattlefieldEffect(true)` on `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, "
     "Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` with `TriggerOptions.ONE_OR_MORE` | trigger | Hedge Shredder (DSK 183). "
     "The stack entry's event cards are the land cards of the one library-to-graveyard event; they return tapped |",
     "`ReturnTriggeringLandFromGraveyardToBattlefieldEffect(true)` on `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, "
     "Triggers.LAND_CARD, ObjectRelation.YOU_OWN)` with `TriggerOptions.ONE_OR_MORE` | trigger | Hedge Shredder (DSK 183). "
     "\"One or more land cards\" is the trigger's filter; the stack entry's triggering cards are the lands of the one "
     "library-to-graveyard event, with their graveyard stays; they return tapped |"),
    ("`Triggers.youDiscardCard() CastDiscardedCardFromGraveyardEffect()` | STATIC + Triggers.youDiscardCard() | Oskar, "
     "Rubbish Reclaimer (NCC 77); the discard trigger preserves the exact nonland card and offers a normal-cost cast from "
     "the controller's graveyard |",
     "`Triggers.youDiscardCard().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.LAND))) "
     "CastDiscardedCardFromGraveyardEffect()` | STATIC + Triggers.youDiscardCard() | Oskar, Rubbish Reclaimer (NCC 77); "
     "\"a nonland card\" is the trigger's filter; the discard collector records the exact card and its graveyard stay, "
     "offered for a normal-cost cast from the controller's graveyard |"),
])
edit("TRIGGER_SLOT_TARGETING.md", [
    ("mill batches are checked after replacement effects and preserve the cards in the event context for\n"
     "card-specific effects such as Hedge Shredder),",
     "mill batches are checked after replacement effects, and the entry records the cards as its triggering cards with\n"
     "their graveyard stays; a narrower filter such as Hedge Shredder's `Triggers.LAND_CARD` makes the event those cards),"),
])
