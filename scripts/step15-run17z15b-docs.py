#!/usr/bin/env python3
"""Step 15, run 17z15b: the agent docs name what the cards write for the two class-keyed sites row 17z15c removed.

`SacrificeOnUnattachEffect` / `DestroyOnUnattachEffect` (static markers) are `Triggers.becomesUnattachedFromPermanent()`
with `SacrificeTriggeringPermanentEffect` / `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)`, and
Boreal Outrider's "if {S} of any of that spell's colors was spent to cast it" is the stack-entry filter
`StackEntrySnowManaOfItsColorSpentPredicate`.

Idempotent; fails if an edit doesn't apply or a stale name is left.

Usage: python3 scripts/step15-run17z15b-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path(__file__).resolve().parent.parent / "agent-docs"

EDITS = {
    "EFFECTS_INDEX.md": [
        ("| `SacrificeOnUnattachEffect` | `()` | whenever this equipment becomes unattached, sacrifice the "
         "previously-equipped creature (static marker; enqueues a `SacrificeTriggeringPermanentEffect` trigger via "
         "`UnattachTriggerSupport`, so the creature is sacrificed only when that trigger resolves) |",
         "| `Triggers.becomesUnattachedFromPermanent()` | trigger | \"whenever this Equipment becomes unattached from "
         "a permanent\": the card's own triggered ability (`UnattachTriggerSupport` puts each such ability on the "
         "stack whole, with the permanent it was attached to as the entry's triggering permanent). \"Sacrifice that "
         "permanent\" is `SacrificeTriggeringPermanentEffect()`, \"destroy that permanent\" "
         "`DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)`; the permanent stays until the ability "
         "resolves |"),
    ],
    "EFFECT_MERGE_PLAN.md": [
        ("card carries `SacrificeOnUnattachEffect`.", "card has a `Triggers.becomesUnattachedFromPermanent()` ability."),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("+ STATIC SacrificeOnUnattachEffect + EquipActivatedAbility(\"{2}\")",
         "+ Triggers.becomesUnattachedFromPermanent() SacrificeTriggeringPermanentEffect() + "
         "EquipActivatedAbility(\"{2}\")"),
        ("+ STATIC DestroyOnUnattachEffect + EquipActivatedAbility(\"{1}\")",
         "+ Triggers.becomesUnattachedFromPermanent() DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING) "
         "+ EquipActivatedAbility(\"{1}\")"),
    ],
    "PREDICATES_REFERENCE.md": [
        ("| `StackEntryColoredManaSpentPredicate` | `()` |",
         "| `StackEntrySnowManaOfItsColorSpentPredicate` | `()` | spells that had snow mana of any of their colors "
         "spent to cast them (read by `TargetLegalityService`). A cast trigger's stack-entry filter: \"whenever you "
         "cast a creature spell, if {S} of any of that spell's colors was spent to cast it\" (Boreal Outrider) is "
         "`Triggers.youCastSpell().withFilter(...).withStackEntryFilter(new "
         "StackEntrySnowManaOfItsColorSpentPredicate())` |\n"
         "| `StackEntryColoredManaSpentPredicate` | `()` |"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"when this Equipment is put into a graveyard from the battlefield, exile equipped creature\" |",
         "| \"Whenever this Equipment becomes unattached from a permanent, sacrifice / destroy that permanent\" | "
         "`SacrificeTriggeringPermanentEffect()` / `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)` "
         "| `Triggers.becomesUnattachedFromPermanent()` | Grafted Wargear, Grafted Exoskeleton, Stitcher's Graft / "
         "Captain's Hook. \"That permanent\" is the entry's triggering permanent |\n"
         "| \"Whenever you cast a creature spell, if {S} of any of that spell's colors was spent to cast it, that "
         "creature enters with an additional +1/+1 counter on it\" | "
         "`GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(new Fixed(1))` | "
         "`Triggers.youCastSpell().withFilter(new CardTypePredicate(CREATURE)).withStackEntryFilter(new "
         "StackEntrySnowManaOfItsColorSpentPredicate())` | Boreal Outrider |\n"
         "| \"when this Equipment is put into a graveyard from the battlefield, exile equipped creature\" |"),
    ],
}
STALE = ["SacrificeOnUnattachEffect", "DestroyOnUnattachEffect",
         "GrantAdditionalPlusOnePlusOneCounterToSnowPaidCreatureSpellEffect"]


def main() -> int:
    failures = []
    for name, replacements in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in replacements:
            if new in text:
                continue
            if text.count(old) != 1:
                failures.append(f"{name}: {old[:60]!r} occurs {text.count(old)} times")
                continue
            text = text.replace(old, new)
        path.write_text(text)
    for path in sorted(DOCS.glob("*.md")):
        for stale in STALE:
            if stale in path.read_text():
                failures.append(f"{path.name}: stale {stale}")
    for failure in failures:
        print("FAILED", failure)
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
