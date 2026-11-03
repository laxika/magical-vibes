#!/usr/bin/env python3
"""Prints the trigger-only wrapper chains (outermost first) of every damage ability's effects, with counts, so the
legacy view's wrapper order can be checked against the cards (effect-slot-refactor.md step 14, run 8).

Usage: python3 scripts/step14-damage-chains.py [--list SIGNATURE]
"""
import collections
import importlib.util
import os
import re
import sys

_here = os.path.dirname(__file__)


def _load(name, file):
    spec = importlib.util.spec_from_file_location(name, os.path.join(_here, file))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


scan = _load("damage_scan", "step14-damage-scan.py")
do = _load("death_options", "step14-death-options.py")
po = do.po


def peel(effect, src):
    """(chain, inner) of an effect expression: the wrapper kinds outermost first and the effect inside them."""
    chain = []
    while True:
        e = do.strip_fq(effect.strip())
        if re.fullmatch(r"[a-z]\w*", e):
            found = do.definition(src, e)
            if found is None:
                chain.append("VAR?")
                return chain, e
            chain.append("var")
            effect = found[2]
            continue
        a = po.call(e, "new OncePerTurnTriggerEffect")
        if a is not None and len(a) == 1:
            chain.append("L1"); effect = a[0]; continue
        a = po.call(e, "OncePerTurnTriggerEffect.markOnAcceptance")
        if a is not None and len(a) == 1:
            chain.append("Lacc"); effect = a[0]; continue
        a = po.call(e, "OncePerTurnTriggerEffect.keyed")
        if a is not None:
            chain.append("Lkey"); effect = a[0]; continue
        if e.startswith("OncePerTurnTriggerEffect.") or e.startswith("new OncePerTurnTriggerEffect("):
            chain.append("L?"); return chain, e
        a = po.call(e, "new OnceOnlyTriggerEffect")
        if a is not None and len(a) == 1:
            chain.append("O"); effect = a[0]; continue
        a = po.call(e, "new TriggeringPermanentConditionalEffect")
        if a is not None:
            chain.append("P" if len(a) == 2 else "P%d" % len(a)); effect = a[1]; continue
        if e.startswith("TriggeringPermanentConditionalEffect."):
            chain.append("P?"); return chain, e
        a = po.call(e, "new TriggeringCardConditionalEffect")
        if a is not None:
            chain.append("C" if len(a) == 2 else "C%d" % len(a)); effect = a[1]; continue
        if "TriggeringPermanentControllerConditionalEffect" in e.split("(")[0]:
            chain.append("PC"); return chain, e
        a = po.call(e, "new AllyCombatDamageTriggerEffect")
        if a is not None:
            chain.append("A" + "".join("t" if x == "true" else "f" for x in a[2:])); effect = a[1]; continue
        a = po.call(e, "new BatchedCombatDamageToYouTriggerEffect")
        if a is not None:
            chain.append("B"); effect = a[0]; continue
        a = po.call(e, "new ConditionalEffect")
        if a is not None:
            if len(a) == 2 or (len(a) == 3 and a[2] == "true"):
                chain.append("I"); effect = a[1]; continue
            chain.append("Iflag" + ",".join(a[2:])); return chain, e
        a = po.call(e, "ConditionalEffect.unless")
        if a is not None:
            chain.append("U"); return chain, e
        a = po.call(e, "ConditionalEffect.atTriggerTime")
        if a is not None:
            chain.append("T"); return chain, e
        return chain, e


def abilities(src):
    for m in scan.ABILITY.finditer(src):
        end = po.close_paren(src, m.end())
        args = po.split_args(src[m.end():end - 1])
        if not args or not scan.FAMILY.search(args[0]):
            continue
        rest = args[1:]
        mode = rest.pop(0) if rest and re.match(r"(?:[\w.]+\.)?TriggerMode\.\w+$", rest[0]) else None
        options = rest.pop(0) if rest and re.match(r"(?:[\w.]+\.)?TriggerOptions\b", rest[0]) else None
        yield m, end, args, mode, options, rest


def main():
    want = sys.argv[sys.argv.index("--list") + 1] if "--list" in sys.argv else None
    counts = collections.Counter()
    for root, _, files in os.walk("magical-vibes-card"):
        if "/build/" in root or "/test/" in root:
            continue
        for name in sorted(files):
            if not name.endswith(".java"):
                continue
            src = open(os.path.join(root, name)).read()
            for m, end, args, mode, options, effects in abilities(src):
                trig = scan.FAMILY.search(args[0]).group(1)
                sigs = []
                for effect in effects:
                    chain, _ = peel(effect, src)
                    sigs.append(">".join(c for c in chain if c != "var") or "-")
                sig = "|".join(sigs)
                if all(s == "-" for s in sigs) and not mode and not options:
                    continue
                key = "%s %s%s%s %s" % (m.group(0), trig, " " + mode.split(".")[-1] if mode else "",
                                        " opts" if options else "", sig)
                counts[key] += 1
                if want and want in key:
                    print(name, re.sub(r"\s+", " ", src[m.start():end])[:300])
    if not want:
        for k, v in sorted(counts.items(), key=lambda kv: (-kv[1], kv[0])):
            print("%5d %s" % (v, k))


if __name__ == "__main__":
    main()
