#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 8: "that source" and "that source's controller" read the stack entry.

Idempotent; run once per letter module:

    for m in magical-vibes-card/*/; do python3 scripts/step15-damage-source-cards.py "$m"; done

The damage-source effects no longer have a collector bind the source, its controller and the damage into them: the
stack entry carries the source as its triggering permanent (and its controller as it dealt the damage), and the damage
as its event value. So:

- the four effects that existing ones express are replaced: Belltower Sphinx's mill, Phyrexian Obliterator's
  sacrifice, Reaper of Sheoldred's poison counter and Visions of Brutality's life loss, each with a
  TRIGGERING_PERMANENT_CONTROLLER recipient and an EventValue amount;
- the kept effects lose their marker arguments (Archfiend of Spite);
- "whenever a source deals damage to this" says it triggers once for each source (TriggerOptions.EACH_SOURCE,
  CR 603.2c), which the combat collector used to infer from the effect's class;
- Mesmeric Orb's MillRecipient.UNTAPPED_PERMANENT_CONTROLLER is renamed TRIGGERING_PERMANENT_CONTROLLER.
"""
import pathlib
import re
import sys

EFFECT = "com.github.laxika.magicalvibes.model.effect."
TRIGGER_OPTIONS = "com.github.laxika.magicalvibes.model.trigger.TriggerOptions"
EVENT_VALUE = "com.github.laxika.magicalvibes.model.amount.EventValue"

EACH_SOURCE_TRIGGER = ("addTrigger(Triggers.dealtDamage(),", "addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,")

CARDS = {
    "ArchfiendOfSpite.java": {
        "replace": [
            EACH_SOURCE_TRIGGER,
            ("new DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect(0, null)",
             "new DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect()"),
        ],
        "add": [TRIGGER_OPTIONS],
        "remove": [],
    },
    "BelltowerSphinx.java": {
        "replace": [
            ("addTrigger(Triggers.dealtDamage(), new DamageSourceControllerMillsEffect());",
             "addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,\n"
             "                new MillEffect(new EventValue(), MillRecipient.TRIGGERING_PERMANENT_CONTROLLER));"),
        ],
        "add": [TRIGGER_OPTIONS, EVENT_VALUE, EFFECT + "MillEffect", EFFECT + "MillRecipient"],
        "remove": [EFFECT + "DamageSourceControllerMillsEffect"],
    },
    "CragSaurian.java": {
        "replace": [EACH_SOURCE_TRIGGER],
        "add": [TRIGGER_OPTIONS],
        "remove": [],
    },
    "PhyrexianObliterator.java": {
        "replace": [
            ("addTrigger(Triggers.dealtDamage(), new DamageSourceControllerSacrificesPermanentsEffect(0, null));",
             "addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,\n"
             "                new SacrificePermanentsEffect(new EventValue(), new PermanentTruePredicate(),\n"
             "                        SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER));"),
        ],
        "add": [TRIGGER_OPTIONS, EVENT_VALUE, "com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate",
                EFFECT + "SacrificePermanentsEffect", EFFECT + "SacrificeRecipient"],
        "remove": [EFFECT + "DamageSourceControllerSacrificesPermanentsEffect"],
    },
    "ReaperOfSheoldred.java": {
        "replace": [
            ("addTrigger(Triggers.dealtDamage(), new DamageSourceControllerGetsPoisonCounterEffect(null));",
             "addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,\n"
             "                new GivePoisonCountersEffect(1, PoisonRecipient.TRIGGERING_PERMANENT_CONTROLLER));"),
        ],
        "add": [TRIGGER_OPTIONS, EFFECT + "GivePoisonCountersEffect", EFFECT + "PoisonRecipient"],
        "remove": [EFFECT + "DamageSourceControllerGetsPoisonCounterEffect"],
    },
    "RonaTolarianObliterator.java": {
        "replace": [("addTrigger(Triggers.dealtDamage(), new DamageSourceControllerExilesRandomHandCardEffect());",
                     "addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,\n"
                     "                new DamageSourceControllerExilesRandomHandCardEffect());")],
        "add": [TRIGGER_OPTIONS],
        "remove": [],
    },
    "Tephraderm.java": {
        "replace": [EACH_SOURCE_TRIGGER],
        "add": [TRIGGER_OPTIONS],
        "remove": [],
    },
    "VisionsOfBrutality.java": {
        "replace": [
            ("new DamageSourceControllerLosesLifeEffect()",
             "new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)"),
        ],
        "add": [EVENT_VALUE, EFFECT + "LoseLifeEffect", EFFECT + "LoseLifeRecipient"],
        "remove": [EFFECT + "DamageSourceControllerLosesLifeEffect"],
    },
    "MesmericOrb.java": {
        "replace": [("MillRecipient.UNTAPPED_PERMANENT_CONTROLLER", "MillRecipient.TRIGGERING_PERMANENT_CONTROLLER")],
        "add": [],
        "remove": [],
    },
}


def fix_imports(src, add, remove):
    for name in remove:
        src = src.replace(f"import {name};\n", "")
    imports = re.findall(r"^import [^;]+;\n", src, flags=re.M)
    present = {line[len("import "):-2] for line in imports}
    missing = [name for name in add if name not in present]
    if not missing:
        return src
    first = src.index(imports[0])
    end = src.rindex(imports[-1]) + len(imports[-1])
    block = sorted(set(imports) | {f"import {name};\n" for name in missing})
    return src[:first] + "".join(block) + src[end:]


def convert(path, spec):
    src = path.read_text()
    converted = src
    for old, new in spec["replace"]:
        if new in converted:
            continue
        if old not in converted:
            raise SystemExit(f"{path}: neither the old nor the new form of {old!r}")
        converted = converted.replace(old, new)
    converted = fix_imports(converted, spec["add"], spec["remove"])
    if converted != src:
        path.write_text(converted)
        print(f"converted {path}")


def main(module):
    for name, spec in CARDS.items():
        for path in sorted(pathlib.Path(module).rglob(name)):
            if "/build/" not in str(path):
                convert(path, spec)


if __name__ == "__main__":
    main(sys.argv[1])
