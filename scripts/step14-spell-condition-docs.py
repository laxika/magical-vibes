#!/usr/bin/env python3
"""Step 14, run 6c of effect-slot-refactor.md: rewrites the agent-docs code spans that author an ability with one of the
run's condition-as-effect classes into the trigger's filter and the ability's effects, as
step14-spell-condition-options.py rewrote the cards:

    `Triggers.youCastSpell() NthSpellCastTriggerEffect(2, List.of(DrawCardEffect()))`
    -> `Triggers.youCastSpell().nthThisTurn(2)` + `DrawCardEffect()`

A span with only the condition (the trigger in another column) becomes "`<filters>` on the trigger with `<effects>`".
The class entries of EFFECTS_INDEX.md and EFFECTS_QUICK_REFERENCE.md are rewritten by hand. A span the script can't
parse is left and listed (--report), so it can be done by hand.

Usage: python3 scripts/step14-spell-condition-docs.py [--dry-run] [--report]
"""
import glob
import importlib.util
import os
import re
import sys

_here = os.path.dirname(__file__)
spec = importlib.util.spec_from_file_location("cond", os.path.join(_here, "step14-spell-condition-options.py"))
cond = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cond)
po = cond.po

SKIP_FILES = ("EFFECTS_INDEX.md", "EFFECTS_QUICK_REFERENCE.md")
SPAN = re.compile(r"`([^`\n]*\b(?:%s)\b[^`\n]*)`" % "|".join(cond.CLASSES))
TRIGGER = re.compile(r"^(?:new\s+)?Triggers\.(\w+)\(\)\s*(?:\+\s*)?")


def effects_of(text):
    t = text.strip()
    args = po.call(t, "List.of")
    if args is None and t.startswith("[") and t.endswith("]"):
        args = po.split_args(t[1:-1])
    return [a.strip() for a in args if a.strip()] if args is not None else None


def rewrite(code, family_hint):
    """The rewritten span text, or None."""
    trig = None
    m = TRIGGER.match(code)
    rest = code
    if m:
        trig, rest = m.group(1), code[m.end():]
    family = trig or family_hint
    if family is None:
        return None
    rest = rest.strip()
    may_prompt = None
    may = po.call(rest.replace("new MayEffect", "MayEffect"), "MayEffect")
    if may is not None:
        rest = may[0]
        may_prompt = may[1] if len(may) > 1 else '"..."'
    rest = re.sub(r"^new ", "", rest)
    if rest.count("(") != rest.count(")") or rest.count('"') % 2:
        return None
    try:
        parsed = cond.parse("new " + rest, "", family)
    except (cond.Skip, IndexError, AttributeError, ValueError):
        return None
    if parsed is None:
        return None
    f, sfx = parsed
    effects = effects_of(f.get("effects", ""))
    if not effects:
        return None
    body = effects[0] if len(effects) == 1 else "SequenceEffect.of(%s)" % ", ".join(effects)
    if may_prompt is not None:
        effects = ["MayEffect(%s, %s)" % (body, may_prompt)]
    out = ("Triggers.%s()" % trig if trig else "") + "".join(cond.collapse(s) for s in sfx)
    if f.get("target"):
        out = "target(%s%s).addTrigger(%s)" % (cond.collapse(f["target"]), ", 0, 1" if f.get("optional") else "",
                                               out if trig else "<trigger>" + out)
    parts = ["`%s`" % out] + ["`%s`" % e for e in effects]
    if trig or f.get("target"):
        return " + ".join(parts)
    return "`%s` on the trigger with " % out + " + ".join(parts[1:])


def family_near(line, start):
    """The trigger family named in the table row before the span, for a span without its trigger."""
    found = re.findall(r"Triggers\.(\w+)\(\)", line[:start]) or re.findall(r"Triggers\.(\w+)\(\)", line)
    return found[-1] if found else None


def main():
    dry = "--dry-run" in sys.argv
    report = "--report" in sys.argv
    left = []
    changed = 0
    for path in sorted(glob.glob("agent-docs/*.md")):
        if os.path.basename(path) in SKIP_FILES:
            continue
        lines = open(path).read().split("\n")
        out = []
        for line in lines:
            def sub(m):
                new = rewrite(m.group(1), family_near(line, m.start()))
                if new is None:
                    left.append("%s: %s" % (path, m.group(1)[:160]))
                    return m.group(0)
                return new
            out.append(SPAN.sub(sub, line))
        new_text = "\n".join(out)
        if new_text != "\n".join(lines):
            changed += 1
            if not dry:
                open(path, "w").write(new_text)
    if report:
        print("\n".join(left))
    print("%d docs %s, %d spans left" % (changed, "would change" if dry else "changed", len(left)), file=sys.stderr)


if __name__ == "__main__":
    sys.exit(main())
