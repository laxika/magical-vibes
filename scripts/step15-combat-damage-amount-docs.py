#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 10: the agent-docs lines for a combat damage trigger's "that many".

Idempotent; run once from the repository root:

    python3 scripts/step15-combat-damage-amount-docs.py

Exact substring edits. Each old text is unique in its file; it becomes the new text, or its whole line goes when the
new text is None. A file whose old text is gone and whose new text is present is left alone.
"""
import pathlib

DOCS = pathlib.Path("agent-docs")

EVENT_VALUE = (
    "- A combat damage trigger's `EventValue` (\"that many\", \"that much\") is the damage dealt: every \"deals combat "
    "damage to a player\" collector sets it as the stack entry's event value, whatever the effect, including when the "
    "ability comes from an attached Aura or Equipment, and a target chosen as the ability goes on the stack carries it "
    "into the entry (`PermanentChoiceContext.AttackTriggerTarget.withEventValue`). A graveyard target restricted by "
    "the damage (Venerable Warsinger's `ReturnCardFromGraveyardEffect.dynamicMaxManaValue(new EventValue())`, "
    "Soundwave's `matchManaValueToCombatDamage`) is fixed at the damage as it is chosen, before the entry exists.")

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("Empyreal Voyager (KLD). `CombatDamageAmountAwareEffect` snapshots the damage that actually reached the "
         "player, so prevention and trample assignment are respected.",
         "Empyreal Voyager (KLD). The combat collector sets the damage that actually reached the player as the stack "
         "entry's event value, so prevention and trample assignment are respected."),
        ("Froghemoth (AFR). The combat-damage snapshot fixes the target limit before the damaged player's graveyard "
         "choice;",
         "Froghemoth (AFR). The collector passes the damage to `graveyardChoiceMaxTargets(int)` as the targets are "
         "chosen from the damaged player's graveyard;"),
        ("Mark of Sakiko (BOK). `CombatDamageAmountAwareEffect` makes the attached-trigger collector snapshot combat "
         "damage for `EventValue`;",
         "Mark of Sakiko (BOK). The attached-trigger collector sets the combat damage as the entry's event value;"),
    ],
    "EFFECTS_INDEX.md": [
        ("`Triggers.dealsCombatDamageToPlayer()` trigger descriptor that snapshots combat damage as the graveyard "
         "selection limit,",
         "`Triggers.dealsCombatDamageToPlayer()` trigger whose graveyard selection limit is the combat damage dealt "
         "(`graveyardChoiceMaxTargets(int)`),"),
        ("| `CombatDamageAmountAwareEffect` | `CardEffect` |", None),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `ExileUpToCombatDamageCardsFromGraveyardEffect()` — combat-damage trigger that snapshots damage as the "
         "graveyard target limit,",
         "- `ExileUpToCombatDamageCardsFromGraveyardEffect()` — combat-damage trigger whose graveyard target limit is "
         "the damage dealt (`graveyardChoiceMaxTargets(int)`),"),
        ("  `graveyardChoiceMaxTargets()` + `graveyardChoiceFilter()`.",
         "  `graveyardChoiceMaxTargets()` (`graveyardChoiceMaxTargets(int eventValue)` for \"up to that many\",\n"
         "  Froghemoth) + `graveyardChoiceFilter()`."),
        ("- `CombatDamageAmountAwareEffect` — exposes the dynamic amount that should receive the combat damage event "
         "value when a trigger is collected. Used by `AwardPersistentManaEffect`, `EnergyCountersEffect`, and "
         "`DrawDiscardAndConniveEffect`, including when the trigger comes from an attached Aura.",
         EVENT_VALUE),
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
