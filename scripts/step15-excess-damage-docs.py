#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 11: the agent-docs lines for a combat damage to a creature trigger's excess damage.

Idempotent; run once from the repository root:

    python3 scripts/step15-excess-damage-docs.py

Exact substring edits. Each old text is unique in its file; it becomes the new text, or its whole line goes when the
new text is None. A file whose old text is gone and whose new text is present is left alone.
"""
import pathlib

DOCS = pathlib.Path("agent-docs")

EXCESS = (
    "- A `Triggers.dealsCombatDamageToCreature()` trigger's `EventValue` (\"that excess damage\") is the excess damage "
    "dealt to that creature in that combat damage step (marked damage, damage from other sources at the same time and "
    "deathtouch taken into account): the collector sets it as every entry's event value and judges the ability's "
    "intervening \"if\" against it as the ability triggers. \"If that creature was dealt excess damage this turn, you "
    "get X {E}, where X is that excess damage\" is `TriggerOptions.DEFAULT.withInterveningIf(new EventValueAtLeast(1))` "
    "+ `EnergyCountersEffect(new EventValue())` (Overclocked Electromancer).")

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `GainEnergyEqualToExcessCombatDamageEffect()` | `Triggers.dealsCombatDamageToCreature()` | Overclocked "
         "Electromancer (M3C 58). The combat trigger uses the damage event's combined damage to that creature and "
         "accounts for marked damage already on it. |",
         "| `EnergyCountersEffect(new EventValue())` | `Triggers.dealsCombatDamageToCreature()`, "
         "`TriggerOptions.DEFAULT.withInterveningIf(new EventValueAtLeast(1))` | Overclocked Electromancer (M3C 58). "
         "The entry's event value is the excess of the step's combined damage to that creature, marked damage already "
         "on it taken into account; no excess, no trigger. |"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `CombatExcessDamageAwareEffect` | `CardEffect` |", None),
        ("| `GainEnergyEqualToExcessCombatDamageEffect` | `()` |", None),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `CombatExcessDamageAwareEffect` — marks an `Triggers.dealsCombatDamageToCreature()` trigger whose event "
         "value is the excess damage dealt to the damaged creature during that combat damage event.",
         EXCESS),
        ("- `GainEnergyEqualToExcessCombatDamageEffect()` — gives the trigger controller energy", None),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("act on \"that creature\"; granted, temporary, and persistent abilities are captured before combat deaths, so "
         "they still trigger when the granting permanent dies in the same damage event |",
         "act on \"that creature\"; the entry's event value is the excess damage dealt to that creature (`EventValue`, "
         "\"that excess damage\"), and an intervening \"if\" is judged against it as the ability triggers; granted, "
         "temporary, and persistent abilities are captured before combat deaths, so they still trigger when the "
         "granting permanent dies in the same damage event |"),
    ],
}


def apply(path: pathlib.Path, old: str, new: str | None) -> None:
    text = path.read_text()
    if old not in text:
        if new is None or new in text:
            return
        raise SystemExit(f"{path.name}: neither old nor new text found: {old[:70]}")
    if text.count(old) != 1:
        raise SystemExit(f"{path.name}: old text is not unique: {old[:70]}")
    if new is None:
        start = text.rfind("\n", 0, text.index(old)) + 1
        end = text.index("\n", text.index(old)) + 1
        text = text[:start] + text[end:]
    else:
        text = text.replace(old, new)
    path.write_text(text)


def main() -> None:
    for name, edits in EDITS.items():
        for old, new in edits:
            apply(DOCS / name, old, new)
    print("done")


if __name__ == "__main__":
    main()
