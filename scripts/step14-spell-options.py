#!/usr/bin/env python3
"""Step 14, run 6a of effect-slot-refactor.md: the trigger-only wrappers around the spell, land-play and activation
abilities (Triggers.youCastSpell / opponentCastsSpell / ... / youPlayLand / youActivateAbility / ...
thisManaAbilityResolves, read by the SpellAbilityWatch collectors) become TriggerOptions and trigger filters.

The outermost wrappers of an ability's effect, written out or passed as a local variable with one definition, are
peeled off in any order:

- `new OncePerTurnTriggerEffect(x)` / `OncePerTurnTriggerEffect.keyed(x, k)`: "this ability triggers only once each
  turn", `withLimit(TriggerLimit.ONCE_EACH_TURN)` (keyed by the ability, so k is dropped);
- `OncePerTurnTriggerEffect.markOnAcceptance(x)`: "do this only once each turn" (CR 603.2h),
  `withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)`;
- `new ConditionalEffect(c, x)`: the intervening "if" (CR 603.4), `withInterveningIf(c)`;
- on a land-play trigger, `new TriggeringCardConditionalEffect(p, x)`: the trigger's filter, `.withFilter(p)` (dropped
  for `new CardTruePredicate()`);
- on an activation trigger, `new TriggeringPermanentConditionalEffect(p, x)`: `.withSource(p)`, and
  `new Triggering{Loyalty,Ninjutsu,Outlast,PowerUp,Boast,Tap,NonTap}AbilityConditionalEffect(x)`:
  `.withKind(AbilityTrigger.AbilityKind.X)`.

The collectors read the ability through TriggeredAbility.legacyEffects(), which wraps each effect in the same
wrappers again, so nothing they see changes. Which top-level ConditionalEffects are intervening "if"s was checked
against the Oracle text: every one this script converts is ("When you cast this spell, if it was kicked", "Whenever
an opponent casts a spell, if this card is suspended", a mode's ability, ...). KEEP lists the cards left as they are,
with the reason. The wrappers inside a SpellCastTriggerEffect are run 6b's.

--dry-run prints the diffs, --report lists every ability converted. Usage:
python3 scripts/step14-spell-options.py [--dry-run] [--report] <module dir>...
"""
import difflib
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


do = _load("death_options", "step14-death-options.py")
po = do.po

ABILITY = re.compile(r"\baddTrigger\(")
LAND = ("youPlayLand", "youPlayAnotherLand", "opponentPlaysLand")
ACTIVATION = ("youActivateAbility", "opponentActivatesAbility", "youActivateNonManaAbility",
              "opponentActivatesNonManaAbility", "youActivateExhaustAbility", "youActivateEternalizeOrEmbalm",
              "thisManaAbilityResolves")
CAST = ("youCastSpell", "opponentCastsSpell", "playerCastsSpell", "youCastThisSpell", "youCopySpell",
        "opponentCopiesSpell", "youCounterSpell", "yourSpellCountered", "thisSpellCounteredOrFizzles")
FAMILY = re.compile(r"^Triggers\.(" + "|".join(LAND + ACTIVATION + CAST) + r")\(\)$")
KINDS = {"TriggeringLoyaltyAbilityConditionalEffect": "LOYALTY",
         "TriggeringNinjutsuAbilityConditionalEffect": "NINJUTSU",
         "TriggeringOutlastAbilityConditionalEffect": "OUTLAST",
         "TriggeringPowerUpAbilityConditionalEffect": "POWER_UP",
         "TriggeringBoastAbilityConditionalEffect": "BOAST",
         "TriggeringNonTapAbilityConditionalEffect": "WITHOUT_TAP_COST",
         "TriggeringTapAbilityConditionalEffect": "WITH_TAP_COST"}

KEEP = {
    # One Oracle ability ("cast ... or activate ...") with one "triggers only once each turn" over two conditions whose
    # effects differ (a spell copy and an ability copy), which one ability can't hold; the shared unkeyed mark stays.
    "BillPotts.java",
    # "Once each turn, you may play ... from the top of your library. When you do, create a Food token": a reflexive
    # trigger of the permission, modelled with one unkeyed once-each-turn mark shared by the cast and land halves.
    "TheFourthDoctor.java",
    # Granted abilities with a per-grant limit (`OncePerTurnTriggerEffect.perGrant`), which a TriggerLimit can't say.
    "FolkHero.java", "TyvarTheBellicose.java",
    # "Has cascade as long as ..." is a condition on having the ability, checked only as the spell is cast
    # (`ConditionalEffect.atTriggerTime`); an intervening "if" would check it again on resolution. Run 16.
    "BloodbraidMarauder.java",
    # Four `TriggerMode.INDEPENDENT` cascades: run 16.
    "ApexDevastator.java",
}


def peel(expr, family):
    """(inner effect, trigger suffixes, limit, condition), or None when expr has no wrapper to peel."""
    suffixes, limit, condition = [], None, None
    peeled = False
    while True:
        e = po.strip_fq(expr.strip())
        args = po.call(e, "new OncePerTurnTriggerEffect")
        if args is not None and len(args) == 1 and limit is None:
            expr, limit, peeled = args[0], "ONCE_EACH_TURN", True
            continue
        args = po.call(e, "OncePerTurnTriggerEffect.keyed")
        if args is not None and limit is None:
            expr, limit, peeled = args[0], "ONCE_EACH_TURN", True
            continue
        args = po.call(e, "OncePerTurnTriggerEffect.markOnAcceptance")
        if args is not None and limit is None:
            expr, limit, peeled = args[0], "DO_ONLY_ONCE_EACH_TURN", True
            continue
        args = po.call(e, "new ConditionalEffect")
        if args is not None and (len(args) == 2 or args[2:] == ["true"]) and condition is None:
            expr, condition, peeled = args[1], args[0], True
            continue
        args = po.call(e, "new TriggeringCardConditionalEffect")
        if args is not None and len(args) == 2 and family in LAND:
            if do.collapse(args[0]) != "new CardTruePredicate()":
                suffixes.append(".withFilter(%s)" % args[0])
            expr, peeled = args[1], True
            continue
        args = po.call(e, "new TriggeringPermanentConditionalEffect")
        if args is not None and len(args) == 2 and family in ACTIVATION:
            suffixes.append(".withSource(%s)" % args[0])
            expr, peeled = args[1], True
            continue
        kind = next((k for k in KINDS if po.call(e, "new " + k) is not None), None)
        if kind is not None and family in ACTIVATION:
            suffixes.append(".withKind(AbilityTrigger.AbilityKind.%s)" % KINDS[kind])
            expr, peeled = po.call(e, "new " + kind)[0], True
            continue
        break
    if not peeled:
        return None
    # The kind reads before the source in the trigger, as the Oracle text does.
    suffixes.sort(key=lambda s: not s.startswith(".withKind"))
    return expr, suffixes, limit, condition


def reindent(text, indent):
    """A multi-line expression with its continuation lines moved under {@code indent}, keeping their shape: its body
    one level in, and a closing line that starts with ")" at {@code indent} itself."""
    lines = text.strip().split("\n")
    rest = [l for l in lines[1:] if l.strip()]
    if not rest:
        return lines[0]
    least = min(len(l) - len(l.lstrip()) for l in rest)
    target = len(indent) + (0 if rest[-1].strip().startswith(")") else 8)
    shifted = [(" " * target + l[least:]) if l.strip() else "" for l in lines[1:]]
    return "\n".join([lines[0].rstrip()] + shifted)


do.reindent = reindent


def trigger_text(base, suffixes, indent, column):
    """The trigger with its filters: on one line when it fits, otherwise one filter per line under the trigger."""
    flat = do.collapse(base) + "".join(do.collapse(s) for s in suffixes)
    if column + len(flat) <= 118:
        return flat
    cont = indent + " " * 8
    lines = [do.collapse(base)]
    for suffix in suffixes:
        name = suffix[:suffix.index("(") + 1]
        arg = suffix[len(name):-1]
        lines.append(cont + name + do.pretty(arg, cont, len(cont) + len(name)) + ")")
    return "\n".join(lines)


def needs_for(suffixes, limit, condition):
    needs = set()
    if limit or condition:
        needs.add("com.github.laxika.magicalvibes.model.trigger.TriggerOptions")
    if limit:
        needs.add("com.github.laxika.magicalvibes.model.trigger.TriggerLimit")
    if any(s.startswith(".withKind") for s in suffixes):
        needs.add("com.github.laxika.magicalvibes.model.trigger.AbilityTrigger")
    return needs


def statement_parts(src, m):
    """(end, args, trigger expression, family name, existing options, effect index) of the addTrigger at m, or None."""
    end = po.close_paren(src, m.end())
    args = po.split_args(src[m.end():end - 1])
    trigger, existing, idx = args[0], None, 1
    cond = po.call(trigger, "new TriggerCondition")
    inner_trigger = cond[0] if cond is not None else trigger
    fm = FAMILY.match(do.collapse(inner_trigger))
    if not fm:
        return None
    if len(args) > 1 and do.collapse(args[1]).startswith("TriggerOptions."):
        existing, idx = do.collapse(args[1]), 2
    elif len(args) > 1 and do.collapse(args[1]).startswith("TriggerMode."):
        return None
    if len(args) != idx + 1:
        return None
    return end, args, trigger, fm.group(1), existing, idx


def rebuilt(src, m, parts, inner, suffixes, limit, condition):
    end, args, trigger, family, existing, idx = parts
    line_start = src.rfind("\n", 0, m.start()) + 1
    indent = re.match(r"[ \t]*", src[line_start:]).group(0)
    column = m.start() - line_start + len("addTrigger(")
    cond = po.call(trigger, "new TriggerCondition")
    if cond is not None:
        trigger = "new TriggerCondition(%s)" % ", ".join(
            [do.collapse(cond[0]) + "".join(do.collapse(s) for s in suffixes)] + [do.collapse(a) for a in cond[1:]])
    else:
        trigger = trigger_text(trigger, suffixes, indent + " " * 8, column)
    rest = []
    if limit or condition or existing:
        rest.append(do.options_expr(existing, limit, condition))
    rest.append(inner)
    inner_indent = indent + " " * 8
    if "\n" not in trigger:
        return "addTrigger(" + do.layout([trigger] + rest, inner_indent, column) + ")"
    return "addTrigger(" + trigger + ",\n" + inner_indent + (",\n" + inner_indent).join(
        do.pretty(a, inner_indent, len(inner_indent)) for a in rest) + ")"


def convert(src, name, report):
    needs = set()
    edits = []
    var_uses = {}
    for m in ABILITY.finditer(src):
        parts = statement_parts(src, m)
        if parts is None:
            continue
        end, args, trigger, family, existing, idx = parts
        effect = args[idx]
        if re.fullmatch(r"[a-z]\w*", effect.strip()):
            var_uses.setdefault(effect.strip(), []).append((m, parts))
            continue
        result = peel(effect, family)
        if result is None:
            continue
        inner, suffixes, limit, condition = result
        edits.append((m.start(), end, rebuilt(src, m, parts, inner.strip(), suffixes, limit, condition)))
        needs |= needs_for(suffixes, limit, condition)
        report.append("%s\t%s\t%s\t%s\t%s" % (name, family, do.collapse("".join(suffixes)), limit or "", do.collapse(condition or "")))
    for var, uses in var_uses.items():
        found = do.definition(src, var)
        if found is None:
            continue
        start, stop, expr = found
        families = {parts[3] for _, parts in uses}
        results = {f: peel(expr, f) for f in families}
        if any(r is None for r in results.values()):
            continue
        if len(re.findall(r"\b%s\b" % re.escape(var), src)) != 1 + len(uses):
            raise SystemExit("%s: %s is used outside its spell/land/activation abilities" % (name, var))
        inners = {r[0].strip() for r in results.values()}
        if len(inners) != 1:
            raise SystemExit("%s: %s peels differently per trigger" % (name, var))
        line_start = src.rfind("\n", 0, start) + 1
        indent = re.match(r"[ \t]*", src[line_start:]).group(0)
        text = reindent(inners.pop(), indent)
        if "\n" not in text and start - line_start + len(text) > 118:
            text, start = "\n" + indent + " " * 8 + text, start - 1
        edits.append((start, stop, text))
        for m, parts in uses:
            _, suffixes, limit, condition = results[parts[3]]
            edits.append((m.start(), parts[0], rebuilt(src, m, parts, var, suffixes, limit, condition)))
            needs |= needs_for(suffixes, limit, condition)
            report.append("%s\t%s\t%s\t%s\t%s" % (name, parts[3], do.collapse("".join(suffixes)), limit or "",
                                                  do.collapse(condition or "")))
    for start, stop, text in sorted(edits, reverse=True):
        src = src[:start] + text + src[stop:]
    return src, needs


def main():
    dry = "--dry-run" in sys.argv
    show_report = "--report" in sys.argv
    report = []
    changed = 0
    for module in [a for a in sys.argv[1:] if not a.startswith("--")]:
        for root, _, files in os.walk(module):
            if "/build/" in root or "/test/" in root:
                continue
            for name in sorted(files):
                if not name.endswith(".java") or name in KEEP:
                    continue
                path = os.path.join(root, name)
                original = open(path).read()
                src, needs = convert(original, name, report)
                if src == original:
                    continue
                src = po.add_imports(src, sorted(needs))
                src = po.drop_unused_imports(original, src)
                changed += 1
                if dry:
                    sys.stdout.writelines(difflib.unified_diff(original.splitlines(True), src.splitlines(True),
                                                               path, path))
                else:
                    open(path, "w").write(src)
    if show_report:
        print("\n".join(report))
    print("%d card classes %s" % (changed, "would change" if dry else "changed"), file=sys.stderr)


if __name__ == "__main__":
    sys.exit(main())
