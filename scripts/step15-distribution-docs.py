#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 6: agent-docs for the death counter distributions and Martyr's Bond.

Idempotent exact line edits: each old line is replaced by its new text (or a line is inserted after an anchor) only
when the old line is still there.

    python3 scripts/step15-distribution-docs.py
"""
import pathlib

DOCS = pathlib.Path("agent-docs")

REPLACE = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"When this creature dies, you may distribute a number of +1/+1 counters equal to the number of +1/+1 counters on this creature among any number of creatures you control\" | `DistributeCountersAmongCreaturesOnDeathEffect.fromDyingSourceCountersAmongControlledCreatures(CounterType.PLUS_ONE_PLUS_ONE)` |",
         "| \"When this creature dies, you may distribute a number of +1/+1 counters equal to the number of +1/+1 counters on this creature among any number of creatures you control\" | `new MayEffect(new DistributeCountersAmongControlledCreaturesEffect(CounterType.PLUS_ONE_PLUS_ONE, new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE)), prompt)` | `Triggers.dies(THIS)` | Vastwood Hydra. Does **not** target: the division is chosen as the ability resolves. The count is the +1/+1 counters the dying creature had, read from the stack entry. Only creatures you control receive counters. Pair with `EnterWithCountersEffect(PLUS_ONE_PLUS_ONE, new XValue())` for \"enters with X +1/+1 counters\" |"),
        ("| \"When this creature dies, you may distribute five +1/+1 counters among any number of target creatures\" | `DistributeCountersAmongCreaturesOnDeathEffect.fixedAmongAnyCreatures(CounterType.PLUS_ONE_PLUS_ONE, 5)` |",
         "| \"When this creature dies, you may distribute five +1/+1 counters among any number of target creatures\" | `new MayEffect(DistributeCountersAmongCreaturesOnDeathEffect.amongAnyTargetCreatures(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(5)), prompt)` | `Triggers.dies(THIS)` | Jugan, the Rising Star. The targets and division are announced as the ability goes on the stack (CR 601.2c-d, CR 603.3d, by `StackedDividedDamageTriggerService`), the \"you may\" is chosen as it resolves (CR 603.5); any creature, an opponent's too, may receive counters |\n"
         "| \"When this creature dies, distribute X +1/+1 counters among any number of target creatures you control, where X is the number of +1/+1 counters on this creature\" | `DistributeCountersAmongCreaturesOnDeathEffect.amongTargetCreaturesYouControl(CounterType.PLUS_ONE_PLUS_ONE, new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE))` | `Triggers.dies(THIS)` | Quirion Beastcaller. Targets and division announced as the ability goes on the stack; with no counters it asks for no targets and leaves the stack |\n"
         "| \"Whenever a creature dies, its controller distributes a number of +1/+1 counters equal to its power among any number of target creatures they control\" | `DistributeCountersAmongCreaturesOnDeathEffect.amongTargetCreaturesItsControllerControls(CounterType.PLUS_ONE_PLUS_ONE, new TriggeringPermanentPower())` | `new TriggerCondition(Triggers.creatureDies(ANY), AbilityZones.COMMAND)` | Grand Ossuary. The ability stays under the planar controller; the dying creature's controller chooses the targets among their creatures and the division |"),
        ("| \"Whenever this enchantment or another nonland permanent you control is put into a graveyard from the battlefield, each opponent sacrifices a permanent of their choice that shares a card type with it.\" | `SacrificePermanentsEffect(1, PermanentSharesCardTypeWithSourcePermanentPredicate(), SacrificeRecipient.EACH_OPPONENT)`",
         "| \"Whenever this enchantment or another nonland permanent you control is put into a graveyard from the battlefield, each opponent sacrifices a permanent of their choice that shares a card type with it.\" | `SacrificePermanentsEffect(1, PermanentSharesCardTypeWithTriggeringPermanentPredicate(), SacrificeRecipient.EACH_OPPONENT)` on self/ally permanent-graveyard triggers, with the ally trigger's filter `PermanentNotPredicate(PermanentIsLandPredicate())` | trigger | Martyr's Bond; \"it\" is the stack entry's triggering permanent, so its last-known card types are used |"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `DistributeCountersAmongCreaturesOnDeathEffect` |",
         "| `DistributeCountersAmongCreaturesOnDeathEffect` | `(CounterType, DynamicAmount total, Recipients)`; factories `amongAnyTargetCreatures(type, total)` / `amongTargetCreaturesYouControl(type, total)` / `amongTargetCreaturesItsControllerControls(type, total)` | death triggers' \"distribute N counters among any number of target creatures [you control]\" (Jugan, Quirion Beastcaller, Grand Ossuary). `StackedDividedDamageTriggerService` evaluates `total` against the stack entry (`CountersOnTriggeringPermanent`, `TriggeringPermanentPower`, `Fixed`) and announces the targets and division as the ability goes on the stack (CR 601.2c-d, CR 603.3d); a staged `pendingETBDamageAssignments` division is checked and taken over. Resolution places the announced counters on recipients that are still legal targets. A \"you may\" is a `MayEffect` around it. For the untargeted \"distribute among creatures you control\" (Vastwood Hydra) use `DistributeCountersAmongControlledCreaturesEffect` |"),
        ("- `LifeLostWhenTriggeringPermanentEntered()` - ",
         None),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `DistributeCountersAmongCreaturesOnDeathEffect.fromDyingSourceCountersAmongControlledCreatures(CounterType)` — ",
         "- `new MayEffect(new DistributeCountersAmongControlledCreaturesEffect(type, new CountersOnTriggeringPermanent(type)), prompt)` — `Triggers.dies(THIS)`: \"you may distribute a number of `type` counters equal to the number on this creature among any number of creatures you control\" (Vastwood Hydra). Does not target; the division is chosen as it resolves."),
        ("- `DistributeCountersAmongCreaturesOnDeathEffect.fixedAmongAnyCreatures(CounterType, int total)` — ",
         "- `DistributeCountersAmongCreaturesOnDeathEffect.amongAnyTargetCreatures(CounterType, DynamicAmount total)` — `Triggers.dies(THIS)`: \"distribute N counters among any number of target creatures\" (Jugan, the Rising Star: `new MayEffect(… new Fixed(5)), prompt)`). The targets and division are announced as the ability goes on the stack (`StackedDividedDamageTriggerService`); `amongTargetCreaturesYouControl` restricts them to your creatures (Quirion Beastcaller, `new CountersOnTriggeringPermanent(PLUS_ONE_PLUS_ONE)`)."),
        ("- `DistributeCountersAmongCreaturesOnDeathEffect.fromDyingSourcePowerAmongControlledCreatures(CounterType)` — ",
         "- `DistributeCountersAmongCreaturesOnDeathEffect.amongTargetCreaturesItsControllerControls(CounterType, DynamicAmount total)` — `Triggers.creatureDies(ANY)` from the command zone: \"its controller distributes … among any number of target creatures they control\" (Grand Ossuary, `new TriggeringPermanentPower()`). The dying creature's controller chooses the targets and division; the ability stays under the planar controller."),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| On death distribute +1/+1 among controlled creatures | `v/VastwoodHydra.java` |",
         "| On death distribute +1/+1 among controlled creatures | `v/VastwoodHydra.java` | EnterWithCountersEffect(PLUS_ONE_PLUS_ONE, XValue) + Triggers.dies(THIS) MayEffect(DistributeCountersAmongControlledCreaturesEffect(PLUS_ONE_PLUS_ONE, CountersOnTriggeringPermanent(PLUS_ONE_PLUS_ONE))) — \"you may\" and the (non-targeting) division at resolution |"),
        ("| On death distribute a fixed number of +1/+1 among any creatures | `j/JuganTheRisingStar.java` |",
         "| On death distribute a fixed number of +1/+1 among any creatures | `j/JuganTheRisingStar.java` | Triggers.dies(THIS) MayEffect(DistributeCountersAmongCreaturesOnDeathEffect.amongAnyTargetCreatures(PLUS_ONE_PLUS_ONE, Fixed(5))) — targets and division announced as it goes on the stack, \"you may\" at resolution |"),
    ],
    "PREDICATES_REFERENCE.md": [
        ("| `PermanentSharesCardTypeWithSourcePermanentPredicate` |",
         None),
    ],
}

INSERT_AFTER = {
    "EFFECTS_INDEX.md": {
        "- `LifeLostWhenTriggeringPermanentEntered()` - ":
            "- `TriggeringPermanentPower()` - the power of the stack entry's triggering permanent as it last existed when the ability triggered (`triggeringPermanentPowerAtTrigger`), at least 0; 0 when the entry recorded none. Grand Ossuary's \"a number of +1/+1 counters equal to its power\"",
    },
    "PREDICATES_REFERENCE.md": {
        "| `PermanentSharesCardTypeWithSourcePermanentPredicate` |":
            "| `PermanentSharesCardTypeWithTriggeringPermanentPredicate` | `()` | permanents that share a card type with the stack entry's triggering permanent as it last existed; \"shares a card type with it\" (Martyr's Bond) | `FilterContext.triggeringPermanentSnapshot` |",
    },
}


def edit(name):
    path = DOCS / name
    lines = path.read_text().split("\n")
    changed = False
    for prefix, new in REPLACE.get(name, []):
        if new is None:
            continue
        for i, line in enumerate(lines):
            if line.startswith(prefix) and line != new and not line.startswith(new.split("\n")[0][:len(prefix)] + "\0"):
                if line == new.split("\n")[0] and "\n" not in new:
                    continue
                if "\n" in new and lines[i:i + new.count("\n") + 1] == new.split("\n"):
                    continue
                lines[i] = new
                changed = True
                break
    for anchor, new in INSERT_AFTER.get(name, {}).items():
        if new in lines:
            continue
        for i, line in enumerate(lines):
            if line.startswith(anchor):
                lines.insert(i + 1, new)
                changed = True
                break
    if changed:
        path.write_text("\n".join(lines))
        print(f"edited {path}")


def main():
    for name in sorted(set(REPLACE) | set(INSERT_AFTER)):
        edit(name)


if __name__ == "__main__":
    main()
