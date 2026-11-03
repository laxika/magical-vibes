#!/usr/bin/env python3
"""Step 14, run 6b of effect-slot-refactor.md: rewrites the agent-docs code spans that author a "whenever [a player]
casts a spell" ability as a SpellCastTriggerEffect into the trigger's filter and the ability's effects, as
step14-spell-cast-options.py rewrote the cards:

    `Triggers.youCastSpell() SpellCastTriggerEffect(CardTypePredicate(CREATURE), List.of(DrawCardEffect(1)))`
    -> `Triggers.youCastSpell().withFilter(CardTypePredicate(CREATURE))` + `DrawCardEffect(1)`

A span with only the effect (the trigger in another column) becomes `.withFilter(...)` on the cast trigger plus the
effects. The factories (`nth`, `duringYourTurn`, `withIntervening`, ...) and the long constructors map to the
SpellTrigger filters and TriggerOptions the same way. A span the script can't parse is left and listed (--report), so
it can be done by hand.

Usage: python3 scripts/step14-spell-cast-docs.py [--dry-run] [--report]
"""
import glob
import importlib.util
import os
import re
import sys

_here = os.path.dirname(__file__)
spec = importlib.util.spec_from_file_location("cast", os.path.join(_here, "step14-spell-cast-options.py"))
cast = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cast)
po = cast.po

SPAN = re.compile(r"`([^`\n]*SpellCastTriggerEffect[^`\n]*)`")
TRIGGER = re.compile(r"^(?:new\s+)?(Triggers\.(?:youCastSpell|opponentCastsSpell|playerCastsSpell)\(\))\s*"
                     r"(?:\+\s*|from the command zone\s+|from the graveyard\s+)?")


def effects_of(text):
    t = text.strip()
    args = po.call(t, "List.of")
    if args is None and t.startswith("[") and t.endswith("]"):
        args = po.split_args(t[1:-1])
    return [a.strip() for a in args if a.strip()] if args is not None else None


def rewrite(code):
    """The rewritten span text, or None."""
    trig = None
    m = TRIGGER.match(code)
    rest = code
    if m:
        trig, rest = m.group(1), code[m.end():]
    rest = rest.strip()
    may_prompt = None
    may = po.call(rest.replace("new MayEffect", "MayEffect"), "MayEffect")
    if may is not None:
        rest = may[0]
        may_prompt = may[1] if len(may) > 1 else '"..."'
    rest = re.sub(r"^new ", "", rest)
    rest = re.sub(r"\bnew (SpellCastTriggerEffect)", r"\1", rest)
    if rest.count("(") != rest.count(")") or rest.count('"') % 2:
        return None
    try:
        f = cast.parse_sct("new " + rest if rest.startswith("SpellCastTriggerEffect(") else rest, "")
    except (cast.Skip, IndexError, AttributeError):
        return None
    if f is None:
        return None
    effects = effects_of(f.get("effects", ""))
    if not effects:
        return None
    sfx = cast.suffixes(f)
    out = (trig or "") + "".join(cast.collapse(s) for s in sfx)
    opts = []
    if cast.present(f, "intervening"):
        opts.append("TriggerOptions.DEFAULT.withInterveningIf(%s)" % cast.collapse(f["intervening"]))
    body = effects[0] if len(effects) == 1 else "SequenceEffect.of(%s)" % ", ".join(effects)
    if cast.present(f, "cost"):
        effects = ["MayPayManaEffect(%s, %s, %s)" % (f["cost"], body, may_prompt or '"..."')]
    elif may_prompt is not None:
        effects = ["MayEffect(%s, %s)" % (body, may_prompt)]
    if cast.present(f, "target"):
        out = "target(%s).addTrigger(%s)" % (cast.collapse(f["target"]), out or "<cast trigger>")
    parts = ["`%s`" % out] + ["`%s`" % o for o in opts] + ["`%s`" % e for e in effects]
    return " + ".join(parts) if trig or cast.present(f, "target") else (
        "`%s` on the cast trigger with " % out) + " + ".join(parts[1:])


def main():
    dry = "--dry-run" in sys.argv
    report = "--report" in sys.argv
    left = []
    changed = 0
    for path in sorted(glob.glob("agent-docs/*.md")):
        text = open(path).read()

        def sub(m):
            new = rewrite(m.group(1))
            if new is None:
                left.append("%s: %s" % (path, m.group(1)[:140]))
                return m.group(0)
            return new

        new_text = SPAN.sub(sub, text)
        if new_text != text:
            changed += 1
            if not dry:
                open(path, "w").write(new_text)
    if report:
        print("\n".join(left))
    print("%d docs %s, %d spans left" % (changed, "would change" if dry else "changed", len(left)), file=sys.stderr)


if __name__ == "__main__":
    sys.exit(main())
