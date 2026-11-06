#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 6: the death counter distributions no longer bake their total.

Idempotent; run per letter module:

    python3 scripts/step15-distribution-cards.py magical-vibes-card/v

- Vastwood Hydra: the untargeted "you may distribute" is a MayEffect around
  DistributeCountersAmongControlledCreaturesEffect(type, CountersOnTriggeringPermanent(type)).
- Quirion Beastcaller, Jugan, Grand Ossuary: DistributeCountersAmongCreaturesOnDeathEffect takes its total as a
  DynamicAmount read from the stack entry (CountersOnTriggeringPermanent, Fixed, TriggeringPermanentPower) and names
  its recipients; Jugan's "you may" is a MayEffect on the card. The targets and division are announced as the
  ability goes on the stack.
"""
import pathlib
import sys

PKG = "com.github.laxika.magicalvibes"
EFFECT = PKG + ".model.effect."
AMOUNT = PKG + ".model.amount."
OLD_EFFECT = "DistributeCountersAmongCreaturesOnDeathEffect"

EDITS = {
    "VastwoodHydra": [(
        """        // When this creature dies, you may distribute a number of +1/+1 counters equal to the number
        // of +1/+1 counters on this creature among any number of creatures you control.
        // Does not target — division rides on pendingETBDamageAssignments at resolution.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                DistributeCountersAmongCreaturesOnDeathEffect
                        .fromDyingSourceCountersAmongControlledCreatures(CounterType.PLUS_ONE_PLUS_ONE));""",
        """        // When this creature dies, you may distribute a number of +1/+1 counters equal to the number
        // of +1/+1 counters on this creature among any number of creatures you control.
        // Does not target: the division is chosen as the ability resolves.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new MayEffect(new DistributeCountersAmongControlledCreaturesEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE)),
                        "Distribute a +1/+1 counter among creatures you control for each +1/+1 counter on this creature?"));""")],
    "QuirionBeastcaller": [(
        """                DistributeCountersAmongCreaturesOnDeathEffect
                        .fromDyingSourceCountersAmongTargetControlledCreatures(CounterType.PLUS_ONE_PLUS_ONE));""",
        """                DistributeCountersAmongCreaturesOnDeathEffect.amongTargetCreaturesYouControl(
                        CounterType.PLUS_ONE_PLUS_ONE, new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE)));""")],
    "JuganTheRisingStar": [(
        """        // The division is announced as the trigger goes on the stack; any creature on the battlefield
        // may receive counters, not just ones you control.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                DistributeCountersAmongCreaturesOnDeathEffect.fixedAmongAnyCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE, 5));""",
        """        // The targets and division are announced as the trigger goes on the stack, the "you may" is
        // chosen as it resolves; any creature on the battlefield may receive counters.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new MayEffect(DistributeCountersAmongCreaturesOnDeathEffect.amongAnyTargetCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(5)),
                        "Distribute five +1/+1 counters among the target creatures?"));""")],
    "GrandOssuary": [(
        """                DistributeCountersAmongCreaturesOnDeathEffect
                        .fromDyingSourcePowerAmongControlledCreatures(CounterType.PLUS_ONE_PLUS_ONE));""",
        """                DistributeCountersAmongCreaturesOnDeathEffect.amongTargetCreaturesItsControllerControls(
                        CounterType.PLUS_ONE_PLUS_ONE, new TriggeringPermanentPower()));""")],
}

IMPORTS = {
    "VastwoodHydra": [AMOUNT + "CountersOnTriggeringPermanent", EFFECT + "DistributeCountersAmongControlledCreaturesEffect",
                      EFFECT + "MayEffect"],
    "QuirionBeastcaller": [AMOUNT + "CountersOnTriggeringPermanent"],
    "JuganTheRisingStar": [AMOUNT + "Fixed", EFFECT + "MayEffect"],
    "GrandOssuary": [AMOUNT + "TriggeringPermanentPower"],
}


def add_import(src, fqcn):
    line = f"import {fqcn};\n"
    if line in src:
        return src
    imports = [l for l in src.splitlines(keepends=True) if l.startswith("import " + PKG)]
    anchor = None
    for existing in imports:
        if existing > line:
            anchor = existing
            break
    if anchor is None:
        last = imports[-1]
        return src.replace(last, last + line, 1)
    return src.replace(anchor, line + anchor, 1)


def convert(path):
    name = path.stem
    src = path.read_text()
    original = src
    for old, new in EDITS[name]:
        if old in src:
            src = src.replace(old, new, 1)
        elif new not in src:
            raise SystemExit(f"{path}: expected text not found")
    for fqcn in IMPORTS[name]:
        src = add_import(src, fqcn)
    body_without_import = src.replace(f"import {EFFECT}{OLD_EFFECT};\n", "")
    if OLD_EFFECT not in body_without_import:
        src = body_without_import
    if src != original:
        path.write_text(src)
        print(f"converted {path}")


def main(module):
    for path in sorted(pathlib.Path(module).rglob("*.java")):
        if path.stem in EDITS:
            convert(path)


if __name__ == "__main__":
    main(sys.argv[1])
