#!/usr/bin/env python3
"""Step 14, run 8 of effect-slot-refactor.md: the trigger-only wrappers of the damage abilities (the Triggers
factories of scripts/step13-damage-slots.tsv, read by the DamageWatch collectors) become the trigger's filter and the
ability's TriggerOptions. The collectors read the abilities through TriggeredAbility.legacyEffects(), which gives
them the same wrappers again, so nothing they see changes.

The wrappers, outermost first, as the cards nest them and the legacy view rebuilds them:

  PerDamageSourceTriggerEffect(x)                       →  TriggerOptions.EACH_SOURCE
  TriggeringPermanentConditionalEffect(p, x)            →  trigger.withFilter(p)
  OncePerTurnTriggerEffect (new …, keyed, markOnAcceptance)
                                                        →  .withLimit(ONCE_EACH_TURN / DO_ONLY_ONCE_EACH_TURN)
  OncePerTurnPerCreatureTriggerEffect(x)                →  .withLimit(ONCE_EACH_TURN_FOR_EACH_CREATURE)
  ConditionalEffect(c, x) / (c, x, true)                →  .withInterveningIf(c), when it is an intervening "if"
  OnceOnlyTriggerEffect(x)                              →  .withLimit(ONCE)

Every effect of an ability must carry the same wrappers; an effect passed as a local variable is followed to its one
definition. An ability with a wrapper the legacy view can't rebuild in place (a flagged or factory-built
TriggeringPermanentConditionalEffect, wrappers in another order) is KEPT and reported.

Which ConditionalEffects are intervening "if"s is decided by the Oracle text: the condition is part of the trigger
event (QUALIFIERS: "deals 4 or more damage", an unlocked door), the card's text has a "When/Whenever … damage …, if"
sentence for each wrapped ability, or the card is in INTERVENING_CARDS. NOT_INTERVENING lists the effect-text "if"s,
which stay effects. Any other wrapped ability stops the script (UNDECIDED), so no card is decided silently. The legacy
view rebuilds an intervening "if" exactly as the ConditionalEffect was, so the decision changes no behaviour.

A second pass merges the cards in MERGE, whose one Oracle ability was split over several damage abilities with the same
options and effects, into one ability with all their trigger conditions.

--dry-run prints the diffs, --report the decision for each ability.
Usage: python3 scripts/step14-damage-options.py [--dry-run] [--report] <module dir>...
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


co = _load("combat_options", "step14-combat-options.py")
do = co.do
po = co.po
pilot = co.pilot
scan = _load("damage_scan", "step14-damage-scan.py")

ABILITY = re.compile(r"\baddTrigger\(|\baddSimultaneousTrigger\(|new TriggeredAbility\(")
FAMILY = scan.FAMILY
Keep = co.Keep
norm, bare = co.norm, co.bare

# Conditions that are part of the trigger event ("deals 4 or more damage", "is dealt 3 or more damage") or that say
# the ability exists at all (an unlocked door): intervening "if"s whatever the Oracle words, as runs 1 to 7 made them.
QUALIFIERS = {"EventValueAtLeast", "SourceRoomDoorUnlocked"}

# Cards whose wrapped damage abilities are all intervening "if"s though the Oracle text doesn't say "Whenever …, if"
# once for each of them.
INTERVENING_CARDS = {
    # Part of the trigger event: "deals noncombat damage to one or more of your opponents during your turn".
    "MoltenLavamancer.java",
}

# Wrappers that are the effect text's own "if", not an intervening "if": they stay effects. Card → the conditions
# kept (substrings of the condition expression; empty: every wrapped damage ability of the card).
NOT_INTERVENING = {}

# Abilities left as they are, with the reason (card → substrings of the ability; empty: every one of the card).
KEEP = {}

# Cards whose one Oracle ability is split over several damage abilities with the same options and effects.
MERGE = {
    # "Whenever a creature deals damage to one or more players for the first time each turn, put a +1/+1 counter on
    # it."
    "Stensia.java",
    # "Whenever this creature deals damage to a creature or opponent, this creature deals that much damage to you."
    "FleshReaver.java",
    # "Whenever a creature you control with deathtouch deals damage to a player or planeswalker, put a +1/+1 counter on
    # that creature."
    "VraskaSwarmsEminence.java",
}

IMPORTS = dict(co.IMPORTS)
IMPORTS["PermanentPredicate"] = "com.github.laxika.magicalvibes.model.filter.PermanentPredicate"


def peel(effect):
    """The wrappers of one effect expression, outermost first, as (kind, data) pairs, and the effect inside them."""
    chain = []
    while True:
        e = effect.strip()
        args = po.call(e, "new PerDamageSourceTriggerEffect")
        if args is not None and len(args) == 1:
            chain.append(("S", None)); effect = args[0]; continue
        args = po.call(e, "new OncePerTurnTriggerEffect")
        if args is not None and len(args) == 1:
            chain.append(("L", "ONCE_EACH_TURN")); effect = args[0]; continue
        args = po.call(e, "OncePerTurnTriggerEffect.keyed")
        if args is not None and len(args) == 2:
            chain.append(("L", "ONCE_EACH_TURN")); effect = args[0]; continue
        args = po.call(e, "OncePerTurnTriggerEffect.markOnAcceptance")
        if args is not None and len(args) == 1:
            chain.append(("L", "DO_ONLY_ONCE_EACH_TURN")); effect = args[0]; continue
        if re.match(r"(new )?OncePerTurnTriggerEffect\b", bare(e)):
            raise Keep("a once-each-turn wrapper the limit can't say: " + e[:60])
        args = po.call(e, "new OncePerTurnPerCreatureTriggerEffect")
        if args is not None and len(args) == 1:
            chain.append(("L", "ONCE_EACH_TURN_FOR_EACH_CREATURE")); effect = args[0]; continue
        if "OncePerTurnPerCreatureTriggerEffect" in bare(e).split("(")[0]:
            raise Keep("a global once-each-turn-for-each-creature wrapper")
        args = po.call(e, "new OnceOnlyTriggerEffect")
        if args is not None and len(args) == 1:
            chain.append(("O", None)); effect = args[0]; continue
        args = po.call(e, "new TriggeringPermanentConditionalEffect")
        if args is not None:
            if len(args) != 2:
                raise Keep("a flagged TriggeringPermanentConditionalEffect")
            chain.append(("P", args[0])); effect = args[1]; continue
        if bare(e).startswith("TriggeringPermanentConditionalEffect."):
            raise Keep("a TriggeringPermanentConditionalEffect factory (" + bare(e).split("(")[0] + ")")
        args = po.call(e, "new ConditionalEffect")
        if args is not None and (len(args) == 2 or (len(args) == 3 and args[2] == "true")):
            chain.append(("I", args[0])); effect = args[1]; continue
        return chain, effect


ORDER = {"S": 0, "P": 1, "L": 2, "I": 3, "O": 4}


def abilities(src):
    """(match, end, args, mode, options, effects) of each damage ability statement of the source."""
    for m in ABILITY.finditer(src):
        end = po.close_paren(src, m.end())
        args = po.split_args(src[m.end():end - 1])
        if not args or not FAMILY.search(args[0]):
            continue
        rest = args[1:]
        mode = rest.pop(0) if rest and re.match(r"(?:[\w.]+\.)?TriggerMode\.\w+$", rest[0]) else None
        options = rest.pop(0) if rest and re.match(r"(?:[\w.]+\.)?TriggerOptions\b", rest[0]) else None
        yield m, end, args, mode, options, rest


def intervening_sentences(oracle):
    return re.findall(r"\bWhen(?:ever)?\b[^.\n|]*?\bdamage\b[^.\n|]*?,\s*if\b", oracle, re.I)


def resolve(effects, src):
    """Per effect: (variable, chain, inner)."""
    out = []
    for effect in effects:
        variable = None
        text = effect
        if re.fullmatch(r"[a-z]\w*", effect.strip()):
            found = do.definition(src, effect.strip())
            if found is None:
                raise Keep("an effect variable without one definition: " + effect.strip())
            variable = (effect.strip(), found)
            text = found[2]
        chain, inner = peel(text)
        out.append((variable, chain, inner))
    return out


def with_filter(trigger, predicate):
    try:
        return co.with_filter(trigger, predicate, set())
    except co.Keep as keep:
        raise Keep(str(keep).replace("combat trigger", "damage trigger"))


def count_wrapped(src):
    """How many damage abilities of the card carry an intervening-if candidate."""
    n = 0
    for m, end, args, mode, options, effects in abilities(src):
        try:
            resolved = resolve(effects, src)
        except Keep:
            continue
        if any(any(k == "I" for k, _ in c) for _, c, _ in resolved):
            n += 1
    return n


def decide(name, src, oracle, report, wrapped_count):
    """The edits of the card's damage abilities: (start, end, text) and the variable edits."""
    edits, var_edits, spans, needs = [], {}, [], set()
    family_spans = [(a.start(), e) for a, e, *_ in abilities(src)]
    for m, end, args, mode, options, effects in abilities(src):
        text = re.sub(r"\s+", " ", src[m.start():end])
        try:
            if co.kept(KEEP, name, text):
                raise Keep("listed")
            if not effects:
                continue
            if options is not None and norm(bare(options)) != "TriggerOptions.DEFAULT":
                raise Keep("already has options")
            resolved = resolve(effects, src)
            for variable, chain_, _ in resolved:
                if variable is None or not chain_:
                    continue
                vname, (vstart, vend, _) = variable
                for use in re.finditer(r"\b%s\b" % re.escape(vname), src):
                    if vstart <= use.start() < vend or re.match(r"\s*=", src[use.end():]):
                        continue
                    if not any(a <= use.start() < b for a, b in family_spans):
                        raise Keep("its effect variable %s is shared with another family's ability" % vname)
            if all(not c for _, c, _ in resolved):
                continue
            chains = {tuple((k, norm(d) if isinstance(d, str) else d) for k, d in c) for _, c, _ in resolved}
            if len(chains) != 1:
                raise Keep("its effects carry different wrappers")
            chain = resolved[0][1]
            kinds = [k for k, _ in chain]
            orders = [ORDER[k] for k in kinds]
            if orders != sorted(orders) or len(set(orders)) != len(orders):
                if not (kinds.count("I") == 2 and kinds[-2:] == ["I", "I"]):
                    raise Keep("wrappers in an order the legacy view doesn't rebuild: " + ">".join(kinds))
            if mode is not None:
                raise Keep("a trigger mode next to wrappers")
            limit, predicate, condition, multiplicity = None, None, None, None
            inner_wrap = []
            seen_i = False
            for kind, data in chain:
                if kind == "S":
                    multiplicity = "EACH_SOURCE"
                elif kind == "L":
                    limit = data
                elif kind == "O":
                    if limit is not None:
                        raise Keep("two limits")
                    limit = "ONCE"
                elif kind == "P":
                    predicate = data
                elif kind == "I":
                    if seen_i or co.kept(NOT_INTERVENING, name, data):
                        inner_wrap.append(("I", data))
                        continue
                    seen_i = True
                    cls = co.condition_class(data)
                    intervening = (cls in QUALIFIERS or name in INTERVENING_CARDS
                                   or len(intervening_sentences(oracle)) >= wrapped_count)
                    if not intervening:
                        report.append("UNDECIDED\t%s\t%s\t%s" % (name, text, oracle.replace("\n", " | ")))
                        raise Keep("undecided")
                    condition = data
            if limit == "ONCE" and inner_wrap:
                raise Keep("\"triggers only once\" inside an effect-text if")
        except Keep as keep:
            if str(keep) != "undecided":
                report.append("KEEP\t%s\t%s\t%s" % (name, keep, text))
            continue

        if limit is None and predicate is None and condition is None and multiplicity is None:
            report.append("KEEP\t%s\tno trigger-level wrapper (an effect-text if)\t%s" % (name, text))
            continue
        trigger = args[0]
        if predicate is not None:
            try:
                trigger = with_filter(trigger, predicate)
            except Keep as keep:
                report.append("KEEP\t%s\t%s\t%s" % (name, keep, text))
                continue
        line = re.search(r"[^\n]*$", src[:m.start()]).group(0)
        indent = " " * (len(line) - len(line.lstrip()) + 8)
        trigger = co.break_filter(trigger, len(line) + len(m.group(0)), indent)
        base = "TriggerOptions.EACH_SOURCE" if multiplicity else "TriggerOptions.DEFAULT"
        opts = []
        if condition is not None or limit or multiplicity or options is not None:
            needs.add("TriggerOptions")
            if limit:
                needs.add("TriggerLimit")
            opts = [co.options_expr(base, condition, limit, indent)]
        new_effects = []
        for (variable, _, inner), written in zip(resolved, effects):
            body = inner
            for kind, data in reversed(inner_wrap):
                body = "new ConditionalEffect(%s, %s)" % (data, body)
            if variable is not None:
                vname, (vstart, vend, vtext) = variable
                if norm(body) != norm(vtext):
                    var_edits[vname] = (vstart, vend, body)
                new_effects.append(vname)
            else:
                new_effects.append(body)
        call_args = [trigger] + opts + new_effects
        body = do.layout(call_args, indent, len(line) + len(m.group(0)))
        edits.append((m.start(), end, m.group(0) + body + ")"))
        spans.append((m.start(), end))
        report.append("CONVERT\t%s\t%s\t%s" % (name, "".join(kinds), text))
    return edits, var_edits, spans, needs


def convert(src, name, oracle, report):
    edits, var_edits, spans, needs = decide(name, src, oracle, report, count_wrapped(src))
    for vname, (vstart, vend, _) in var_edits.items():
        for use in re.finditer(r"\b%s\b" % re.escape(vname), src):
            if vstart <= use.start() < vend or re.match(r"\s*=", src[use.end():]):
                continue
            if not any(a <= use.start() < b for a, b in spans):
                raise ValueError("%s is also used outside the converted abilities" % vname)
    for vname, (vstart, vend, text) in var_edits.items():
        line = re.search(r"[^\n]*$", src[:vstart]).group(0)
        edits.append((vstart, vend, do.reindent(text, " " * (len(line) - len(line.lstrip())))))
        declared = re.search(r"\b([A-Z]\w*)(\s+%s\s*=\s*)$" % re.escape(vname), src[:vstart])
        if declared and declared.group(1) not in ("CardEffect", "var"):
            edits.append((declared.start(1), declared.end(1), "CardEffect"))
            needs.add("CardEffect")
    for start, end, text in sorted(edits, reverse=True):
        src = src[:start] + text + src[end:]
    return src, needs


STATEMENT = re.compile(r"^([ \t]*)((?:[a-z]\w*\.)?)addTrigger\(", re.M)


def merge(src, name, report):
    """Merges the card's damage abilities with the same options and effects into one ability with all their
    trigger conditions."""
    if name not in MERGE:
        return src, set()
    found = []
    for m in STATEMENT.finditer(src):
        end = po.close_paren(src, m.end())
        if not src[end:].startswith(";"):
            continue
        args = po.split_args(src[m.end():end - 1])
        if not FAMILY.search(args[0]) or args[0].strip().startswith("List.of("):
            continue
        rest = args[1:]
        options = rest.pop(0) if rest and re.match(r"(?:[\w.]+\.)?TriggerOptions\b", rest[0]) else None
        if rest and re.match(r"(?:[\w.]+\.)?TriggerMode\.\w+$", rest[0]):
            raise ValueError("a trigger mode on an ability to merge")
        found.append((m, end, args[0], options, rest))
    groups = {}
    for f in found:
        key = (f[0].group(2), norm(f[3] or "TriggerOptions.DEFAULT"), norm(",".join(f[4])))
        groups.setdefault(key, []).append(f)
    merged = [g for g in groups.values() if len(g) > 1]
    if len(merged) != 1:
        raise ValueError("expected one group of damage abilities to merge, found %d" % len(merged))
    group = merged[0]
    first = group[0]
    m = first[0]
    indent = m.group(1) + " " * 8
    column = len(m.group(1)) + len(m.group(2)) + len("addTrigger(")
    flat = [re.sub(r"\s+\.", ".", do.collapse(f[2])) for f in group]
    triggers = "List.of(" + ", ".join(flat) + ")"
    if column + len(triggers) + 1 > 120:
        inner = indent + " " * 8
        triggers = "List.of(\n" + inner + (",\n" + inner).join(
            co.break_filter(t, len(inner), inner) for t in flat) + ")"
    call_args = [triggers] + ([first[3]] if first[3] else []) + first[4]
    if "\n" in triggers:
        body = (",\n" + indent).join([triggers] + [do.pretty(a, indent, len(indent)) for a in call_args[1:]])
    else:
        body = do.layout(call_args, indent, column)
    edits = [(m.start(2), first[1], m.group(2) + "addTrigger(" + body + ")")]
    for f in group[1:]:
        start = src.rfind("\n", 0, f[0].start()) + 1
        prev = src.rfind("\n", 0, start - 1) + 1
        if src[prev:start].strip().startswith("//"):
            start = prev
        edits.append((start, src.find("\n", f[1]) + 1, ""))
    for start, end, text in sorted(edits, reverse=True):
        src = src[:start] + text + src[end:]
    report.append("MERGE\t%s\t%d abilities" % (name, len(group)))
    return src, {"List"}


def main():
    dry_run = "--dry-run" in sys.argv
    show_report = "--report" in sys.argv
    changed, errors, report = 0, [], []
    for module in [a for a in sys.argv[1:] if not a.startswith("--")]:
        for root, _, files in os.walk(module):
            if "/build/" in root or "/test/" in root:
                continue
            for name in sorted(files):
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                original = open(path).read()
                if not FAMILY.search(original):
                    continue
                oracle = pilot.oracle(pilot.REG.findall(original)) or ""
                try:
                    src, needs = convert(original, name, oracle, report)
                    src, merged = merge(src, name, report)
                    needs |= merged
                except ValueError as error:
                    errors.append("%s: %s" % (path, error))
                    continue
                if src == original:
                    continue
                src = co.add_imports(src, [IMPORTS[n] for n in sorted(needs)])
                src = po.drop_unused_imports(original, src)
                changed += 1
                if dry_run:
                    if not show_report:
                        sys.stdout.writelines(difflib.unified_diff(
                            original.splitlines(True), src.splitlines(True), path, path))
                else:
                    open(path, "w").write(src)
    if show_report:
        print("\n".join(report))
    undecided = [r for r in report if r.startswith("UNDECIDED")]
    print("%d card classes %s, %d undecided" % (changed, "would change" if dry_run else "changed", len(undecided)),
          file=sys.stderr)
    if errors:
        raise SystemExit("\n".join(errors))
    if undecided and not dry_run:
        raise SystemExit("undecided abilities (add them to INTERVENING_CARDS, NOT_INTERVENING or KEEP)")


if __name__ == "__main__":
    main()
