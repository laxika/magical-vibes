#!/usr/bin/env python3
"""Moves the engine, AI and test readers of the combat EffectSlots to the triggers (effect-slot-refactor.md step 13,
family 3).

Each slot of scripts/step13-combat-slots.tsv is read through its CombatWatch constant:

* recv.getEffects(EffectSlot.X) -> CombatWatch.X.effectsOf(recv), and getEffectRegistrations -> registrationsOf;
* getTemporaryTriggeredEffects / getPersistentTriggeredEffects(EffectSlot.X) -> getTemporaryTriggerEffects /
  getPersistentTriggerEffects(CombatWatch.X.trigger());
* grantedTriggeredEffects(..., EffectSlot.X) -> grantedTriggerEffects(..., CombatWatch.X.trigger());
* getEffectiveGraveyardEffects(..., EffectSlot.X) of a graveyard slot -> getEffectiveGraveyardTriggerEffects;
* watcher.slot() != / == EffectSlot.X -> (!)watcher.ability().hasTrigger(CombatWatch.X.trigger());
* any other EffectSlot.X -> CombatWatch.X, whose method signatures are changed by hand.

Comments that name a slot keep the name: it is the CombatWatch constant's.

Usage: python3 scripts/step13-combat-readers.py FILE [FILE ...]
"""
import os
import re
import sys

HERE = os.path.dirname(__file__)
ROWS = [line.rstrip("\n").split("\t") for line in open(os.path.join(HERE, "step13-combat-slots.tsv"))
        if not line.startswith("#")]
ZONES = {row[0]: ("BATTLEFIELD" if row[2] == "-" else row[2]) for row in ROWS}
SLOT = r"(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.(%s)\b" % "|".join(ZONES)
IMPORT = "com.github.laxika.magicalvibes.service.trigger.CombatWatch"


def find_close(src, open_index):
    depth = 0
    for i in range(open_index, len(src)):
        if src[i] == "(":
            depth += 1
        elif src[i] == ")":
            depth -= 1
            if depth == 0:
                return i
    raise ValueError("unbalanced")


def receiver_start(src, dot):
    """The start of the expression whose method is called at src[dot] == '.'."""
    i = dot
    while True:
        j = i - 1
        while j >= 0 and src[j].isspace():
            j -= 1
        if src[j] == ")":
            depth = 0
            while True:
                if src[j] == ")":
                    depth += 1
                elif src[j] == "(":
                    depth -= 1
                    if depth == 0:
                        break
                j -= 1
            j -= 1
        k = j
        while k >= 0 and (src[k].isalnum() or src[k] == "_"):
            k -= 1
        start = k + 1
        before = k
        while before >= 0 and src[before].isspace():
            before -= 1
        if before >= 0 and src[before] == "." :
            i = before
            continue
        if src[start:j + 1].startswith("new ") or src[k - 3:k + 1] == "new ":
            raise ValueError("receiver is a new expression")
        return start


def convert_getters(src, getter, watch_method):
    pattern = re.compile(r"\.%s\(\s*%s\s*\)" % (getter, SLOT))
    while True:
        m = pattern.search(src)
        if m is None:
            return src
        start = receiver_start(src, m.start())
        recv = src[start:m.start()]
        src = src[:start] + "CombatWatch.%s.%s(%s)" % (m.group(1), watch_method, recv.strip()) + src[m.end():]


def convert(src):
    src = convert_getters(src, "getEffects", "effectsOf")
    src = convert_getters(src, "getEffectRegistrations", "registrationsOf")
    src = re.sub(r"\.getTemporaryTriggeredEffects\(\s*%s\s*\)" % SLOT,
                 lambda m: ".getTemporaryTriggerEffects(CombatWatch.%s.trigger())" % m.group(1), src)
    src = re.sub(r"\.getPersistentTriggeredEffects\(\s*%s\s*\)" % SLOT,
                 lambda m: ".getPersistentTriggerEffects(CombatWatch.%s.trigger())" % m.group(1), src)
    for m in reversed(list(re.finditer(r"\bgrantedTriggeredEffects\(", src))):
        close = find_close(src, m.end() - 1)
        args = src[m.end():close]
        last = re.search(r"%s\s*$" % SLOT, args)
        if last:
            src = (src[:m.start()] + "grantedTriggerEffects(" + args[:last.start()]
                   + "CombatWatch.%s.trigger()" % last.group(1) + src[close:])
    for m in reversed(list(re.finditer(r"\bgetEffectiveGraveyardEffects\(", src))):
        close = find_close(src, m.end() - 1)
        args = src[m.end():close]
        last = re.search(r"%s\s*$" % SLOT, args)
        if last and ZONES[last.group(1)] == "GRAVEYARD":
            src = (src[:m.start()] + "getEffectiveGraveyardTriggerEffects(" + args[:last.start()]
                   + "CombatWatch.%s.trigger()" % last.group(1) + src[close:])
    src = re.sub(r"\b(\w+)\.slot\(\)\s*!=\s*%s" % SLOT,
                 lambda m: "!%s.ability().hasTrigger(CombatWatch.%s.trigger())" % (m.group(1), m.group(2)), src)
    src = re.sub(r"\b(\w+)\.slot\(\)\s*==\s*%s" % SLOT,
                 lambda m: "%s.ability().hasTrigger(CombatWatch.%s.trigger())" % (m.group(1), m.group(2)), src)
    src = re.sub(SLOT, lambda m: "CombatWatch.%s" % m.group(1), src)
    return src


def add_import(src, fqcn):
    if re.search(r"^import %s;$" % re.escape(fqcn), src, re.M):
        return src
    package = re.search(r"^package ([\w.]+);", src, re.M).group(1)
    if fqcn.rsplit(".", 1)[0] == package:
        return src
    imports = list(re.finditer(r"^import (?!static)([\w.]+);$", src, re.M))
    for m in imports:
        if m.group(1) > fqcn:
            return src[:m.start()] + "import %s;\n" % fqcn + src[m.start():]
    last = imports[-1]
    return src[:last.end()] + "\nimport %s;" % fqcn + src[last.end():]


def drop_unused_effect_slot(src):
    body = re.sub(r"^import .*$", "", src, flags=re.M)
    if not re.search(r"\bEffectSlot\b", body):
        src = re.sub(r"^import com\.github\.laxika\.magicalvibes\.model\.EffectSlot;\n", "", src, flags=re.M)
    return src


def main():
    for path in sys.argv[1:]:
        src = open(path).read()
        if not re.search(SLOT, src):
            continue
        out = convert(src)
        if "CombatWatch." in out:
            out = add_import(out, IMPORT)
        out = drop_unused_effect_slot(out)
        if out != src:
            open(path, "w").write(out)
            print("converted:", path)


if __name__ == "__main__":
    main()
