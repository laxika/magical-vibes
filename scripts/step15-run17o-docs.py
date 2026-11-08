#!/usr/bin/env python3
"""Step 15, run 17o: the agent docs name `EntryFactCondition`, and "enters untapped" as an event condition.

Exact edits: each must apply once, or have been applied (idempotent).

Usage: step15-run17o-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")
OLD = ("""- `DeclaredAttackCondition` — marker of a condition about the attack as declared (`AttackingCreaturesTotalPowerAtLeast`, `ExactlyAttackers`, `HasAttacker`; `isDeclaredAttackFact` also takes an `AllOf` / `AllConditions` of them): as an attack ability's intervening "if" ("if you attacked with … this combat", pack tactics, Creepy Puppeteer) it is judged from the declared attackers and not recounted as the ability resolves""")
NEW = OLD + """
- `EntryFactCondition` — marker of a condition fixed as the permanent entered (`Kicked`, `NotKicked`, `CastFromZone`, `EnteredFromZone`, `WasCast`, `CastForAlternateCost` / `…ProwlCost` / `…MadnessCost` / `…SpectacleCost`, `RepeatedAdditionalCostPaid`, `CollectEvidenceCostPaid`, `SourceEntryCostPaid`, `GiftPromised`, `ColorSpentToCast`, `SnowManaSpentToCast`, `TreasureManaSpentToCast`, `RevealCardFromHandCostPaid`, `ControlledDragonAsCast`, `SourceIsToken`; `isEntryFact` also takes a `NotCondition`, `AllOf`, `AllConditions` or `AnyOf` of them): a "when this enters" ability judges it from the entering permanent as it triggers and takes its wrapper off, as the stack entry can't read how the permanent was cast; as an effect's "if" a false one leaves an effect that does nothing, so the ability still goes on the stack (CR 603.4). Write "when this enters, if it was kicked" with `TriggerOptions.DEFAULT.withInterveningIf(new Kicked())`; "when this land enters untapped" is the event condition `withEventCondition(new SourceUntapped())` (Dwarven Mine)"""


MAIN_PHASE_OLD = "`SourceIsToken`; `isEntryFact` also takes"
MAIN_PHASE_NEW = ("`SourceIsToken`, `ControllerMainPhase` (as an own enters ability reads it: the phase the permanent "
                  "entered during, Addendum); `isEntryFact` also takes")


def main():
    path = DOCS / "EFFECTS_QUICK_REFERENCE.md"
    text = path.read_text()
    if NEW not in text and NEW.replace(MAIN_PHASE_OLD, MAIN_PHASE_NEW) not in text:
        if text.count(OLD) != 1:
            print("edit not found")
            sys.exit(1)
        text = text.replace(OLD, NEW)
    if MAIN_PHASE_NEW not in text:
        text = text.replace(MAIN_PHASE_OLD, MAIN_PHASE_NEW)
    path.write_text(text)


if __name__ == "__main__":
    main()
