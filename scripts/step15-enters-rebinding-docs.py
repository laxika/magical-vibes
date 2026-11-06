#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 7: docs for Confusion in the Ranks' triggering-permanent predicates.

Idempotent exact line edits in agent-docs: each line starting with a prefix is replaced by its new text, and a line is
inserted after an anchor unless it is already there.

    python3 scripts/step15-enters-rebinding-docs.py
"""
import pathlib

DOCS = pathlib.Path(__file__).resolve().parent.parent / "agent-docs"

REPLACE = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Whenever an artifact, creature, or enchantment enters, its controller chooses target permanent another player controls that shares a card type with it. Exchange control of those permanents.\" |",
         "| \"Whenever an artifact, creature, or enchantment enters, its controller chooses target permanent another player controls that shares a card type with it. Exchange control of those permanents.\" | `ExchangeControlOfTargetPermanentsEffect.forTriggeringPermanent(PermanentAllOfPredicate(PermanentNotPredicate(PermanentControlledByTriggeringPermanentControllerPredicate()), PermanentSharesCardTypeWithTriggeringPermanentPredicate()))` | Triggers.enters(PermanentAnyOfPredicate(artifact, creature, enchantment), ObjectRelation.ANY) | Confusion in the Ranks. The entering permanent is the triggering permanent and the first exchange participant; its controller chooses the other target, and the entering permanent is excluded from its own target set. \"Another player\" and \"it\" are relative to the triggering permanent, not the source. |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `ExchangeControlOfTargetPermanentsEffect.forTriggeringPermanent(PermanentPredicate targetPredicate)` — ",
         "- `ExchangeControlOfTargetPermanentsEffect.forTriggeringPermanent(PermanentPredicate targetPredicate)` — trigger-time exchange where the entering permanent is the first exchange participant and its controller chooses the other target; write the target relative to the triggering permanent (`PermanentControlledByTriggeringPermanentControllerPredicate`, `PermanentSharesCardTypeWithTriggeringPermanentPredicate`) when it must be another player's or share a type with the entering permanent. The entering permanent is not a legal target for its own trigger."),
    ],
    "PREDICATES_REFERENCE.md": [
        ("| `PermanentSharesCardTypeWithTriggeringPermanentPredicate` |",
         "| `PermanentSharesCardTypeWithTriggeringPermanentPredicate` | `()` | permanents that share a card type with the triggering permanent: the one `triggeringPermanentId` names while it is on the battlefield, by its current types (Confusion in the Ranks' \"shares a card type with it\"), otherwise the snapshot as it last existed (Martyr's Bond) | `FilterContext.triggeringPermanentId` / `triggeringPermanentSnapshot` |"),
    ],
}

INSERT_AFTER = {
    "PREDICATES_REFERENCE.md": {
        "| `PermanentManaValueEqualsTriggeringPermanentPredicate` |":
            "| `PermanentControlledByTriggeringPermanentControllerPredicate` | `()` | permanents controlled by the controller of the permanent `triggeringPermanentId` names; nothing while it is off the battlefield. Negated: Confusion in the Ranks' \"target permanent another player controls\", chosen by the entering permanent's controller | `gameData` + `triggeringPermanentId` |",
    },
}


def edit(name):
    path = DOCS / name
    lines = path.read_text().split("\n")
    changed = False
    for prefix, new in REPLACE.get(name, []):
        matches = [i for i, line in enumerate(lines) if line.startswith(prefix)]
        assert len(matches) == 1, (name, prefix, len(matches))
        if lines[matches[0]] != new:
            lines[matches[0]] = new
            changed = True
    for anchor, new in INSERT_AFTER.get(name, {}).items():
        if new in lines:
            continue
        matches = [i for i, line in enumerate(lines) if line.startswith(anchor)]
        assert len(matches) == 1, (name, anchor, len(matches))
        lines.insert(matches[0] + 1, new)
        changed = True
    if changed:
        path.write_text("\n".join(lines))
        print(f"edited {path}")


def main():
    for name in sorted(set(REPLACE) | set(INSERT_AFTER)):
        edit(name)


if __name__ == "__main__":
    main()
