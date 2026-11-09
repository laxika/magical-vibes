#!/usr/bin/env python3
"""Step 15, run 17z6e: agent-docs for the cast triggers' mana spent read from the triggering spell.

`effectNeedsSpellManaSpentX` is gone: the spell records its mana spent (and the part of it creatures produced,
`StackEntry.creatureManaSpentToCast`) as it becomes cast, `ManaSpentToCast` on a cast trigger reads it from the entry's
snapshot of the spell, and the `SpellManaSpentAtLeast` / `SpellCreatureManaSpentAtLeast` /
`SpellManaSpentGreaterThanSourcePower` conditions from the condition context's triggering spell. Fragment edits; the
files keep their own encoding. Fails if a doc or skill still says the collector gives the ability the mana spent as
its X.

Idempotent.

Usage: step15-run17z6e-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"CARDNAME gets +X/+0 until end of turn, where X is the mana spent to cast that spell\" | "
         "`BoostSelfEffect(new XValue(), new Fixed(0))` inside `SpellCastTriggerEffect` | Triggers.youCastSpell() | "
         "Aberrant Manawurm; collector snapshots xValue for amounts referencing X |",
         "| \"CARDNAME gets +X/+0 until end of turn, where X is the mana spent to cast that spell\" | "
         "`BoostSelfEffect(new ManaSpentToCast(), new Fixed(0))` on the filtered cast trigger | Triggers.youCastSpell() | "
         "Aberrant Manawurm; `ManaSpentToCast` reads what the triggering spell recorded as it became cast, from the "
         "entry's snapshot of it, so a countered spell still counts. A cast trigger's `XValue` is the spell's X |"),
        ("Chandra, Dressed to Kill. The emblem's cast trigger goes through the generic cast collector, which gives it the "
         "mana spent on the triggering spell as its X |",
         "Chandra, Dressed to Kill. The emblem's cast trigger goes through the generic cast collector; `ManaSpentToCast` "
         "reads the mana the triggering spell recorded as it became cast, from the entry's snapshot of it (the target "
         "choice records the spell) |"),
        ("The condition compares the snapshotted mana spent with the source's current power when the trigger is created "
         "and again when it resolves.",
         "The condition compares the mana the triggering spell recorded as it became cast (the condition context's "
         "triggering spell) with the source's current power when the trigger is created and again when it resolves."),
        ("| \"if five or more mana was spent to cast that spell, [effect]\" | `ConditionalEffect(new SpellManaSpentAtLeast(5), "
         "innerEffect)` inside `SpellCastTriggerEffect` | Trigger collector snapshots mana spent into stack entry `xValue` |",
         "| \"if five or more mana was spent to cast that spell, [effect]\" | `ConditionalEffect(new SpellManaSpentAtLeast(5), "
         "innerEffect)` on the filtered cast trigger | Judged on the entry's triggering spell (the mana it recorded as it "
         "became cast) as the ability resolves |"),
        ("`ConditionalReplacementEffect(new SpellManaSpentAtLeast(5), baseEffect, upgradedEffect)` inside `SpellCastTriggerEffect`",
         "`ConditionalReplacementEffect(new SpellManaSpentAtLeast(5), baseEffect, upgradedEffect)` on the filtered cast trigger"),
        ("Player/self targeting and the mana-spent `xValue` are plumbed through the targeted-trigger interaction.",
         "Player/self targeting is plumbed through the targeted-trigger interaction, whose entry records the triggering "
         "spell the condition reads."),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `ConditionalEffect(new SpellManaSpentAtLeast(minMana), wrapped)` — mana spent to cast triggering spell >= N",
         "- `ConditionalEffect(new SpellManaSpentAtLeast(minMana), wrapped)` — mana spent to cast triggering spell >= N, "
         "read from the entry's triggering spell (what it recorded as it became cast); `SpellCreatureManaSpentAtLeast(N)` "
         "the mana from creatures (Inga and Esika)"),
        ("(Chandra, Dressed to Kill): the cast collector gives the ability the mana spent on the triggering spell as its X, "
         "which `ManaSpentToCast` reads on a cast trigger (effect-slot-refactor.md step 15, run 17w)",
         "(Chandra, Dressed to Kill): `ManaSpentToCast` on a cast trigger reads the mana the triggering spell recorded as it "
         "became cast, from the entry's snapshot of it, so a countered spell still counts; a cast trigger's `XValue` is the "
         "spell's X (effect-slot-refactor.md step 15, runs 17w and 17z6e)"),
        ("- `SpellManaSpentGreaterThanSourcePower` — spell-cast trigger condition: the mana spent on the triggering spell is "
         "greater than the source permanent's current power at trigger and resolution.",
         "- `SpellManaSpentGreaterThanSourcePower` — spell-cast trigger condition: the mana spent on the triggering spell "
         "(what it recorded as it became cast, the condition context's triggering spell) is greater than the source "
         "permanent's current power at trigger and resolution."),
        ("The trigger carries both values through trigger creation so it still resolves correctly if the spell leaves the stack.",
         "Both are read as it resolves (`TargetSpellManaValue`, `ManaSpentToCast` from the entry's snapshot of the spell), "
         "so it still resolves correctly if the spell leaves the stack."),
    ],
    "EFFECTS_INDEX.md": [
        ("Trigger collector snapshots mana spent into stack entry `xValue`. Used by Colorstorm Stallion",
         "Judged on the entry's triggering spell, the mana it recorded as it became cast. Used by Colorstorm Stallion"),
        ("with both values carried through trigger creation | Ancient Cellarspawn",
         "both read as it resolves (`TargetSpellManaValue`, `ManaSpentToCast` from the entry's snapshot of the spell) | "
         "Ancient Cellarspawn"),
        ("  Molten Note, Memory Deluge)\n",
         "  Molten Note, Memory Deluge). On a spell-cast trigger it is \"the amount of mana spent to cast that spell\": what\n"
         "  the triggering spell recorded as it became cast (`StackEntry.manaSpentToCastThisSpell()`, 0 for an uncast copy),\n"
         "  read from the entry's snapshot of it (Aetherflux Conduit, Manaform Hellkite, Tellah)\n"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("`TriggerOptions.DEFAULT.withInterveningIf(new SpellManaSpentAtLeast(5))`, judged with the spell's mana spent as X "
         "(Summitfest Closing Ceremony's Covercast; the graveyard reader likewise) |",
         "`TriggerOptions.DEFAULT.withInterveningIf(new SpellManaSpentAtLeast(5))`, judged on the spell (its mana spent "
         "recorded as it became cast), which the entry keeps as its triggering spell (Summitfest Closing Ceremony's "
         "Covercast; the graveyard reader likewise) |"),
    ],
    "ARCHITECTURE.md": [
        ("\"Damage equal to the amount of mana spent to cast that spell\" (`ManaSpentToCast`) reads the X the cast collector "
         "gives the ability, the mana spent on the triggering spell.",
         "\"The amount of mana spent to cast that spell\" (`ManaSpentToCast`) and \"if at least N mana (from creatures) was "
         "spent to cast it\" (`SpellManaSpentAtLeast`, `SpellCreatureManaSpentAtLeast`, `SpellManaSpentGreaterThanSourcePower`) "
         "read what the spell recorded as it became cast (`StackEntry.manaSpentToCast` / `creatureManaSpentToCast`, set as "
         "`TriggerCollectionService.checkSpellCastTriggers` begins; 0 for a copy that wasn't cast, CR 707.10), from the "
         "entry's snapshot of the spell as the ability resolves and from `ConditionContext.triggeringSpell` (the entry's, "
         "else the spell on the stack as it triggers): every cast-trigger entry records its triggering spell, the single- "
         "and multi-target choices and the graveyard and hand readers included. A cast trigger's X is the spell's."),
    ],
}

STALE = [
    "gives it the mana spent on the triggering spell as its X",
    "gives the ability the mana spent on the triggering spell as its X",
    "snapshots mana spent into stack entry `xValue`",
    "effectNeedsSpellManaSpentX",
    "judged with the spell's mana spent as X",
    "reads the X the cast collector gives the ability",
]


def read(path):
    return path.read_bytes().decode("utf-8", errors="surrogateescape")


def write(path, text):
    path.write_bytes(text.encode("utf-8", errors="surrogateescape"))


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = read(path)
        changed = False
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                raise SystemExit(f"{name}: fragment missing: {old[:80]}")
            text = text.replace(old, new, 1)
            changed = True
        if changed:
            write(path, text)
            print(f"{name}: updated")
    stale = []
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        text = read(path)
        stale += [f"{path.name}: {phrase}" for phrase in STALE if phrase in text]
    if stale:
        raise SystemExit("stale mentions:\n" + "\n".join(stale))


if __name__ == "__main__":
    main()
