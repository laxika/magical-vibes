#!/usr/bin/env python3
"""Step 15, run 17z3b: the agent-docs lines on how a "whenever this creature attacks" ability chooses its targets, and
the new `CardEffect.actsOnlyOnItsTargets()`.

The self-attack collector routed an ability that attaches Equipment to the slot-by-slot walker by the effect's class;
it now routes by the target groups alone: a single static "up to N" group takes the multi-permanent
`AttackTriggerTargets` choice. `MayEffectHandler`'s skip of an attach with no target is judged by the effect's
`actsOnlyOnItsTargets()` instead of its class.

Fragment edits inside a line and a bullet inserted after an anchor line, so a second run changes nothing.

Usage: step15-run17z3b-docs.py
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
TS = "agent-docs/TRIGGER_SLOT_TARGETING.md"
QR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
IX = "agent-docs/EFFECTS_INDEX.md"

ATTACH_USERS_QR = "Used by Kemba, Kha Enduring and Super-Soldier Serum."
ATTACH_USERS_IX = "Used by Kemba, Kha Enduring and Super-Soldier Serum |"
ACTS_ONLY = ("`actsOnlyOnItsTargets()` is `true`, so a `MayEffect` wrapping it isn't asked when no Equipment was "
             "chosen")

FRAGMENTS = [
    (TS,
     "Single permanent/player targets use `AttackTriggerTarget`; multi-target / \"up to N\" "
     "(`needsSlotBySlotTargetSelection`) reuses `ETBTokenMultiTargetTrigger` (Archon of the Triumvirate) |",
     "Single permanent/player targets use `AttackTriggerTarget`; several target groups, or one optional or dynamic "
     "group (`needsSlotBySlotTargetSelection`), reuse `ETBTokenMultiTargetTrigger` (Archon of the Triumvirate); a "
     "single static \"up to N\" group (Armory Automaton's \"any number of target Equipment\") takes the "
     "multi-permanent `AttackTriggerTargets` choice, all its targets chosen in one answer. Routing reads the target "
     "groups only, never an effect's class |"),
    (QR, ATTACH_USERS_QR,
     "Used by Kemba, Kha Enduring, Super-Soldier Serum, Sokka and Suki, Amy Rose and Armory Automaton (in a "
     "`MayEffect`); " + ACTS_ONLY + "."),
    (IX, ATTACH_USERS_IX,
     "Used by Kemba, Kha Enduring, Super-Soldier Serum, Sokka and Suki, Amy Rose and Armory Automaton (in a "
     "`MayEffect`); " + ACTS_ONLY + " |"),
]

INSERTS = [
    (TS,
     "  list sliced one group short, handing group 1's target to group 0's effect.",
     "- **\"My optional ability asks its question though no target was chosen.\"** `MayEffectHandler` skips a "
     "\"you may\" whose bound target group took no target while other groups took some, and a \"you may\" whose "
     "wrapped effect declares `actsOnlyOnItsTargets()` (`AttachTargetEquipmentToTriggeringPermanentEffect`) when the "
     "entry has no target at all. Override `actsOnlyOnItsTargets()` only for an effect that does nothing but act "
     "on its targets: a \"you may pay / sacrifice ... If you do, ... up to N target ...\" (Aetherstorm Roc, "
     "Glorifier of Suffering) must still be asked with none chosen."),
]


def main():
    changed = 0
    for path, old, new in FRAGMENTS:
        file = ROOT / path
        text = file.read_text()
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"{path}: fragment not found: {old[:80]}")
        file.write_text(text.replace(old, new, 1))
        changed += 1
    for path, anchor, line in INSERTS:
        file = ROOT / path
        lines = file.read_text().split("\n")
        if line in lines:
            continue
        if anchor not in lines:
            raise SystemExit(f"{path}: anchor not found: {anchor[:80]}")
        lines.insert(lines.index(anchor) + 1, line)
        file.write_text("\n".join(lines))
        changed += 1
    print(f"{changed} edit(s)")


if __name__ == "__main__":
    main()
