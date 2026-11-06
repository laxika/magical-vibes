#!/usr/bin/env python3
"""Step 15 run 17c of effect-slot-refactor.md: exact doc edits for graveyard targets restricted by the trigger event.

Idempotent: an edit whose new text is already present is skipped; it fails if an old text is missing or if a removed
form is still named.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [(
        "`ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(CREATURE).targetGraveyard(true)"
        ".upTo(true).dynamicMaxManaValue(new EventValue()).build()` — combat trigger collection snapshots the actual "
        "damage dealt into a static mana-value cap before the graveyard target is chosen |",
        "`ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(CREATURE).targetGraveyard(true)"
        ".requiresManaValueAtMostX(true).build()` — a combat damage trigger's graveyard choice has the damage dealt "
        "as its X, which the stack entry keeps |",
    )],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [(
        "its combat-damage-aware graveyard-copy effect matches spell mana value to damage and converts after a "
        "successful copy cast.",
        "its graveyard-copy effect's filter has `CardManaValueEqualsXPredicate` (\"mana value equal to the damage "
        "dealt\", the choice's X) and it converts after a successful copy cast.",
    )],
    "EFFECTS_INDEX.md": [
        ("    .dynamicMaxManaValue(DynamicAmount)        // death-trigger limit evaluated when the trigger is collected; "
         "e.g. soulshift X",
         "    .dynamicMaxManaValue(DynamicAmount)        // mana value limit; a death trigger's is evaluated as its target "
         "is chosen, with the dying object as it last existed as the amount's source (soulshift X, \"lesser mana "
         "value\"), and kept on the stack entry"),
        ("The `(filter, scope, matchManaValueToCombatDamage, afterSuccessfulCastEffect)` overload snapshots an exact "
         "combat-damage mana-value match and queues its follow-up after a successful copy cast.",
         "The `(filter, scope, afterSuccessfulCastEffect)` overload offers a free cast and queues its follow-up after "
         "a successful copy cast; \"mana value equal to the damage dealt\" is `CardManaValueEqualsXPredicate` in the "
         "filter, a combat damage trigger's graveyard choice having the damage as its X."),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [(
        "The `(filter, scope, matchManaValueToCombatDamage, afterSuccessfulCastEffect)` form snapshots an exact "
        "mana-value match from combat damage and queues the follow-up only after the copy is successfully cast",
        "The `(filter, scope, afterSuccessfulCastEffect)` form offers a free cast and queues the follow-up only after "
        "the copy is successfully cast (Soundwave's \"mana value equal to the damage dealt\" is "
        "`CardManaValueEqualsXPredicate` in the filter, the combat damage trigger's graveyard choice having the "
        "damage as its X)",
    ), (
        "A graveyard target restricted by the damage (Venerable Warsinger's "
        "`ReturnCardFromGraveyardEffect.dynamicMaxManaValue(new EventValue())`, Soundwave's "
        "`matchManaValueToCombatDamage`) is fixed at the damage as it is chosen, before the entry exists.",
        "A graveyard target restricted by the damage is chosen with the damage as the choice's X, which the stack "
        "entry keeps: Venerable Warsinger's \"mana value X or less\" is `ReturnCardFromGraveyardEffect"
        ".requiresManaValueAtMostX(true)`, Soundwave's \"mana value equal to the damage dealt\" "
        "`CardManaValueEqualsXPredicate` in the filter. Likewise a sacrifice trigger's graveyard choice has the "
        "sacrificed permanent's mana value as its X (Riveteers Ascendancy's \"lesser mana value\" is "
        "`CardManaValueLessThanXPredicate`), and a death trigger's `dynamicMaxManaValue` is evaluated as its target "
        "is chosen, with the dying object as it last existed as the amount's source "
        "(`StackEntry.graveyardTargetMaxManaValue`).",
    )],
}

REMOVED = ["matchManaValueToCombatDamage", "withManaValueEqualToDamage", "withMaxManaValue(",
           "withManaValueLessThan(", "withGraveyardTargetFixedToDamage"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                sys.exit(f"{name}: expected one occurrence of {old[:80]!r}")
            text = text.replace(old, new)
        path.write_text(text)
    for path in DOCS.glob("*.md"):
        for removed in REMOVED:
            if removed in path.read_text():
                sys.exit(f"{path.name} still names {removed}")
    print("docs updated")


if __name__ == "__main__":
    main()
