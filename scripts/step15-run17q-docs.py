#!/usr/bin/env python3
"""Step 15, run 17q: the agent docs show the run's forms.

- "If this card is in your graveyard" (`SourceCardInGraveyard`) reads the graveyard incarnation the stack entry keeps
  for the source card; no collector binds it into the condition (`ARCHITECTURE.md`, `CARD_IMPLEMENTATION_PLAYBOOK.md`).
- A Class's level, a Case's "Solved —", a harnessed "∞ —" and a Room's unlocked door are the ability's event condition,
  not an intervening "if" (`EFFECTS_QUICK_REFERENCE.md`, `EFFECTS_INDEX.md`, `ORACLE_TEXT_EFFECT_MAP.md`).

Exact edits; fails if a doc still shows one of the old forms. Idempotent.

Usage: step15-run17q-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path(__file__).resolve().parent.parent / "agent-docs"

EDITS = {
    "ARCHITECTURE.md": [(
        "`SourceCardInGraveyard` can retain an expected graveyard entry version. Graveyard creature-put triggers bind "
        "this version when collected, so leaving and reentering the graveyard cannot satisfy an older Bridge from "
        "Below ability.",
        "`SourceCardInGraveyard` (\"if this card is in your graveyard\") reads the graveyard incarnation the stack "
        "entry keeps for its source card (`StackEntry.sourceCardGraveyardEntryVersion()`: the one the collector "
        "records with `setSourceGraveyardEntryVersion`, carried by a target choice such as "
        "`AttackTriggerTarget.withSourceGraveyardEntryVersion`, else the triggering card's when the source is the "
        "triggering card, as for \"when this dies\"), so leaving and reentering the graveyard cannot satisfy an older "
        "Bridge from Below ability (CR 400.7). No collector binds the version into the condition.",
    )],
    "CARD_IMPLEMENTATION_PLAYBOOK.md": [(
        "true while the source card object is still in its controller's graveyard. Vengeful Pharaoh |",
        "true while the source card object is still in its controller's graveyard, in the incarnation it had as the "
        "ability triggered (the stack entry keeps it, CR 400.7). Vengeful Pharaoh |",
    )],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("pair with `SourceIsSolved` and an intervening-if condition for the Case's solved ability",
         "a Case's \"Solved — [ability]\" has `SourceIsSolved` as its event condition "
         "(`TriggerOptions.DEFAULT.withEventCondition(new SourceIsSolved())`, CR 719.3c); \"To solve\" itself is an "
         "intervening \"if\" (CR 719.3a)"),
        ("use `SourceIsHarnessed` to gate abilities that become active after harnessing.",
         "a \"∞ — [ability]\" triggered ability has `SourceIsHarnessed` as its event condition "
         "(`withEventCondition(new SourceIsHarnessed())`, CR 702.186b)."),
        ("use it to gate Class abilities and level advancement.",
         "use it to gate Class abilities (a triggered one's event condition, `withEventCondition`, CR 716.2a) and "
         "level advancement."),
    ],
    "EFFECTS_INDEX.md": [(
        "for Class ability conditions and advancement.",
        "for Class ability conditions (a triggered one's event condition, CR 716.2a) and advancement.",
    )],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `ConditionalEffect(SourceRoomDoorUnlocked(0), AwardRestrictedManaEffect(ManaColor.RED, 2, "
         "ManaRestriction.RoomSpellsOrUnlocks))` | `Triggers.precombatMainPhase(ActivePlayer.YOU)` |",
         "| `AwardRestrictedManaEffect(ManaColor.RED, 2, ManaRestriction.RoomSpellsOrUnlocks)` | "
         "`Triggers.precombatMainPhase(ActivePlayer.YOU)` with `TriggerOptions.DEFAULT.withEventCondition(new "
         "SourceRoomDoorUnlocked(0))` (a locked half's ability doesn't exist, CR 709.5) |"),
        ("| `ConditionalEffect(SourceRoomDoorUnlocked(1), SequenceEffect.of(SurveilEffect(1), "
         "ConditionalEffect(ControlsDistinctUnlockedRoomNamesCount(8), WinGameEffect())))` | "
         "`Triggers.upkeep(ActivePlayer.YOU)` |",
         "| `SequenceEffect.of(SurveilEffect(1), ConditionalEffect(ControlsDistinctUnlockedRoomNamesCount(8), "
         "WinGameEffect()))` | `Triggers.upkeep(ActivePlayer.YOU)` with `TriggerOptions.DEFAULT.withEventCondition(new "
         "SourceRoomDoorUnlocked(1))` |"),
        ("the solved ability uses `ConditionalEffect(new AllOf(List.of(new SourceIsSolved())), ...)` in "
         "`Triggers.beginningOfCombat(ActivePlayer.YOU)` so the solved gate is checked both when the trigger is "
         "created and when it resolves",
         "the solve trigger is an intervening \"if\" (CR 719.3a); the solved ability "
         "(`Triggers.beginningOfCombat(ActivePlayer.YOU)`) has `SourceIsSolved` as its event condition "
         "(`TriggerOptions.DEFAULT.withEventCondition(new SourceIsSolved())`, CR 719.3c): it exists only once the Case "
         "is solved, and isn't checked again as it resolves"),
        ("gate the ability granted by the harness designation with `ConditionalEffect(new SourceIsHarnessed(), ...)`.",
         "a \"∞ — [ability]\" triggered ability has `SourceIsHarnessed` as its event condition "
         "(`TriggerOptions.DEFAULT.withEventCondition(new SourceIsHarnessed())`, CR 702.186b)."),
    ],
}

OLD_FORMS = {
    "ARCHITECTURE.md": ["can retain an expected graveyard entry version"],
    "ORACLE_TEXT_EFFECT_MAP.md": ["ConditionalEffect(SourceRoomDoorUnlocked(", "ConditionalEffect(new SourceIsHarnessed()",
                                  "ConditionalEffect(new AllOf(List.of(new SourceIsSolved()))"],
    "EFFECTS_QUICK_REFERENCE.md": ["an intervening-if condition for the Case's solved ability"],
}


def main():
    changed = []
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        original = text
        for old, new in edits:
            if old in text:
                if text.count(old) != 1:
                    sys.exit(f"{name}: {text.count(old)} matches of {old[:60]!r}")
                text = text.replace(old, new)
            elif new not in text:
                sys.exit(f"{name}: neither the old nor the new text of {old[:60]!r}")
        for form in OLD_FORMS.get(name, []):
            if form in text:
                sys.exit(f"{name} still shows {form!r}")
        if text != original:
            path.write_text(text)
            changed.append(name)
    print(f"{len(changed)} docs changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
