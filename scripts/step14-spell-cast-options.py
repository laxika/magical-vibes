#!/usr/bin/env python3
"""Step 14, run 6b of effect-slot-refactor.md: every SpellCastTriggerEffect ability becomes its SpellTrigger's filter,
its options and its effects.

A "whenever [a player] casts a [quality] spell" ability written as `addTrigger(Triggers.youCastSpell(), new
SpellCastTriggerEffect(filter, List.of(effects...), ...))` (or one of its factories, `new MayEffect(<that>, prompt)`, or
a local variable with one definition) becomes

    [target(targetFilter).]addTrigger(Triggers.youCastSpell().withFilter(filter)<more filters>[, options], effects...)

- the spell filter is `.withFilter(p)`, the stack-entry condition `.withStackEntryFilter(p)`, the turn conditions
  `.duringYourTurn()` / `.duringOpponentsTurn()` / `.notDuringCastersTurn()`, `.duringCombat()`, the spell counts
  `.nthThisTurn(n)` / `.nthOrLaterThisTurn(n)`, `.paidWithManaFromThis()`, `.paidWithTreasureMana()`,
  `.withTwoOrMoreCardTypes()`, `.expend(n)`; a trigger with none of them is `.anySpell()` (a SpellTrigger with a filter
  is what the generic spell-cast collector reads); `anyPlayer` is an emblem-only flag that card abilities ignore;
- the intervening condition is `TriggerOptions.DEFAULT.withInterveningIf(c)` (CR 603.4), added to the options the
  ability already has; "this ability triggers only once" around every effect (`OnceOnlyTriggerEffect`) the
  `TriggerLimit.ONCE` limit;
- the mana cost is a `MayPayManaEffect(cost, effect, prompt)` around the effects (the "you may" prompt, "Pay {N}?" as the
  targeted collector asked it, or the graveyard collector's "Pay {N} to return <name> from your graveyard to your
  hand?"), and an outer `MayEffect` a `MayEffect` around them (several effects become one `SequenceEffect.of`);
- the target filter is a target group of the card: dropped when the statement is already on `target(sameFilter)`, the
  statement put on `target(filter)` when the card declares no other target group (or only a bare `target(filter);` of
  the same filter, which is removed).

TriggeredAbility.legacyEffects() gives the collectors the SpellCastTriggerEffect back, so nothing they see changes but
for the run's behaviour changes (effect-slot-refactor.md, run 6b). Abilities the script can't convert are reported
with a reason and left as they are (KEEP lists the cards kept on purpose); an ability nested in another one's effects
(a token's) is reported NESTED and converted by running the script again. The cards the script couldn't convert were
done by hand (effect-slot-refactor.md, run 6b; scripts/step14-spell-cast-converted.txt lists every ability).

--dry-run prints the diffs, --report lists every ability converted or skipped. Usage:
python3 scripts/step14-spell-cast-options.py [--dry-run] [--report] <module dir>...
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


so = _load("spell_options", "step14-spell-options.py")
do = so.do
po = do.po
scan = _load("pilot_scan", "step14-pilot-scan.py")
_pretty = do.pretty


def pretty(expr, indent, column):
    """do.pretty, which also breaks a long card predicate list one predicate per line."""
    flat = do.collapse(expr)
    if column + len(flat) > 118 and "\n" not in expr.strip():
        inner = indent + " " * 8
        for name in ("new CardAllOfPredicate", "new CardAnyOfPredicate"):
            args = po.call(flat, name)
            if args is not None and len(args) == 1 and po.call(args[0], "List.of") is not None:
                parts = po.call(args[0], "List.of")
                return "%s(List.of(\n%s%s))" % (name, inner, (",\n" + inner).join(
                    pretty(part, inner, len(inner)) for part in parts))
    return _pretty(expr, indent, column)


do.pretty = pretty


def reindent(text, indent):
    """A multi-line expression with its continuation lines moved under {@code indent}: its body one level in, keeping
    the body's own shape, and a last line that starts with ")" at {@code indent} itself."""
    lines = text.strip().split("\n")
    rest = lines[1:]
    if not [l for l in rest if l.strip()]:
        return lines[0]
    closing = rest[-1] if rest[-1].strip().startswith(")") else None
    body = rest[:-1] if closing is not None else rest
    out = [lines[0].rstrip()]
    filled = [l for l in body if l.strip()]
    if filled:
        least = min(len(l) - len(l.lstrip()) for l in filled)
        target = len(indent) + 8
        out += [(" " * (target + len(l) - len(l.lstrip()) - least) + l.strip()) if l.strip() else "" for l in body]
    if closing is not None:
        out.append(indent + closing.strip())
    return "\n".join(out)


so.reindent = reindent
do.reindent = reindent

CAST = ("youCastSpell", "opponentCastsSpell", "playerCastsSpell")
SCT_NEW = "new SpellCastTriggerEffect"
FACTORIES = {"nth", "atLeast", "atLeastDuringYourTurn", "duringYourTurn", "whenCasterIsNotActiveTurn",
             "duringCombat", "wheneverYouExpend", "withIntervening", "anyPlayer", "usingManaProducedBySource",
             "usingTreasureMana", "withAtLeastTwoCardTypes"}
# The contexts a `new TriggeredAbility(...)` is converted in: their readers take the ability's legacy view (a granted
# ability through GrantedTriggeredAbilitySupport, a token's abilities as its card's).
ABILITY_CONTEXTS = ("GrantTriggeredAbilityEffect", "List.of", "CreateTokenEffect", "addTriggeredAbility")

KEEP = {
    # "Look at that player's hand": the payment's effect targets a hand, but the player is the caster, bound as a
    # non-target; the collector would offer a target for a converted "you may pay" (step 15 binds the event's player).
    "LeshracsSigil.java",
    # The specialized ability is built at runtime by SpecializeGaleEffectHandler from the card's helper.
    "GaleConduitOfTheArcane.java",
    # A granted ability with a target filter ("target opponent"): a granted ability has no card target group.
    "PassionateArchaeologist.java",
}

FIELDS = ["spell", "effects", "cost", "target", "stack", "opp", "ctrl", "intervening", "nth", "min", "anyPlayer",
          "manaBySource", "expend", "treasure", "twoTypes", "casterNotActive", "combat"]
ARITY = {
    2: FIELDS[:2], 7: FIELDS[:7], 9: FIELDS[:9],
    11: FIELDS[:9] + ["anyPlayer", "manaBySource"],
    12: FIELDS[:12], 14: FIELDS[:14], 15: FIELDS[:15], 16: FIELDS[:16], 17: FIELDS[:17],
}


class Skip(Exception):
    pass


def collapse(text):
    return do.collapse(text)


def kind(arg, src):
    a = collapse(po.strip_fq(arg))
    if a == "null":
        return "null"
    if a in ("true", "false"):
        return "bool"
    if re.fullmatch(r"-?\d+", a):
        return "int"
    if a.startswith('"') or a == "(String) null":
        return "str"
    if re.match(r"(?:new )?StackEntry\w*\(|StackEntry\w*\.", a):
        return "stack"
    if re.match(r"TargetFilters\.|(?:new )?\w*TargetFilter\(|\(TargetFilter\)", a):
        return "target"
    m = re.fullmatch(r"[a-z]\w*", a)
    if m:
        v = re.search(r"\bvar\s+" + re.escape(a) + r"\s*=\s*new\s+(\w+)", src)
        d = v or re.search(r"\b([A-Z]\w*(?:<[^;=]*>)?)\s+" + re.escape(a) + r"\s*=", src)
        if d:
            t = d.group(1)
            if "StackEntry" in t:
                return "stack"
            if "TargetFilter" in t:
                return "target"
            if t == "String":
                return "str"
            if t in ("boolean", "Boolean"):
                return "bool"
            if t in ("int", "Integer"):
                return "int"
    return "other"


def parse_sct(expr, src):
    """The fields of a SpellCastTriggerEffect expression, or None when it isn't one."""
    e = po.strip_fq(expr.strip())
    args = po.call(e, SCT_NEW)
    f = {}
    if args is not None:
        n = len(args)
        if n == 3:
            k = kind(args[2], src)
            names = ["spell", "effects", {"str": "cost", "null": "cost", "stack": "stack", "bool": "opp"}.get(k)]
            if names[2] is None:
                raise Skip("3-argument SpellCastTriggerEffect with an argument of unknown type: " + collapse(args[2]))
        elif n == 4:
            k2, k3 = kind(args[2], src), kind(args[3], src)
            if k2 in ("str", "null") and k3 in ("target", "null") and not (k2 == "null" and k3 == "null"):
                names = ["spell", "effects", "cost", "target"]
            elif k2 in ("target", "null") and k3 in ("stack", "null"):
                names = ["spell", "effects", "target", "stack"]
            else:
                raise Skip("4-argument SpellCastTriggerEffect of unknown types: %s, %s" % (k2, k3))
        elif n == 10:
            k = kind(args[9], src)
            names = FIELDS[:9] + ["anyPlayer" if k == "bool" else "min"]
        elif n in ARITY:
            names = ARITY[n]
        else:
            raise Skip("SpellCastTriggerEffect with %d arguments" % n)
        for name, a in zip(names, args):
            f[name] = a
        return f
    m = re.match(r"SpellCastTriggerEffect\.(\w+)\(", e)
    if not m or m.group(1) not in FACTORIES:
        if m:
            raise Skip("unknown SpellCastTriggerEffect factory " + m.group(1))
        return None
    name = m.group(1)
    args = po.call(e, "SpellCastTriggerEffect." + name)
    layout = {
        "nth": ["nth", "spell", "effects", "target"], "atLeast": ["min", "spell", "effects"],
        "atLeastDuringYourTurn": ["min", "spell", "effects"], "duringYourTurn": ["spell", "effects"],
        "whenCasterIsNotActiveTurn": ["effects"], "duringCombat": ["spell", "effects"],
        "wheneverYouExpend": ["expend", "effects"], "withIntervening": ["spell", "effects", "intervening"],
        "anyPlayer": ["spell", "effects"], "usingManaProducedBySource": ["spell", "effects"],
        "usingTreasureMana": ["effects", "intervening"], "withAtLeastTwoCardTypes": ["effects"],
    }[name]
    for field, a in zip(layout, args):
        f[field] = a
    f.update({"atLeastDuringYourTurn": {"ctrl": "true"}, "duringYourTurn": {"ctrl": "true"},
              "whenCasterIsNotActiveTurn": {"casterNotActive": "true"}, "duringCombat": {"combat": "true"},
              "usingManaProducedBySource": {"manaBySource": "true"},
              "usingTreasureMana": {"treasure": "true"},
              "withAtLeastTwoCardTypes": {"twoTypes": "true"}}.get(name, {}))
    return f


def present(f, key):
    v = f.get(key)
    return v is not None and collapse(v) not in ("null", "false", "0", "(String) null")


def suffixes(f):
    out = []
    if present(f, "spell"):
        out.append(".withFilter(%s)" % f["spell"])
    if present(f, "stack"):
        out.append(".withStackEntryFilter(%s)" % f["stack"])
    turns = [k for k in ("opp", "ctrl", "casterNotActive") if present(f, k)]
    if len(turns) > 1:
        raise Skip("more than one turn condition")
    out += [{"opp": ".duringOpponentsTurn()", "ctrl": ".duringYourTurn()",
             "casterNotActive": ".notDuringCastersTurn()"}[t] for t in turns]
    if present(f, "combat"):
        out.append(".duringCombat()")
    if present(f, "nth"):
        out.append(".nthThisTurn(%s)" % collapse(f["nth"]))
    if present(f, "min"):
        out.append(".nthOrLaterThisTurn(%s)" % collapse(f["min"]))
    if present(f, "manaBySource"):
        out.append(".paidWithManaFromThis()")
    if present(f, "treasure"):
        out.append(".paidWithTreasureMana()")
    if present(f, "twoTypes"):
        out.append(".withTwoOrMoreCardTypes()")
    if present(f, "expend"):
        out.append(".expend(%s)" % collapse(f["expend"]))
    return out or [".anySpell()"]


COMMENT = re.compile(r"^[ \t]*//[^\n]*\n", re.M)


def comments(text):
    """The `//` comment lines of an argument text."""
    return [c.strip() for c in COMMENT.findall(text)]


def uncommented(text):
    return COMMENT.sub("", text)


def effect_list(text, src):
    """The effects of a `List.of(...)` argument, or ('VAR', name) for a list variable."""
    text = uncommented(text)
    args = po.call(po.strip_fq(text.strip()), "List.of")
    if args is not None:
        return [a for a in args if a.strip()]
    name = text.strip()
    if re.fullmatch(r"[a-z]\w*", name):
        return ("VAR", name)
    raise Skip("resolved effects are neither List.of(...) nor a list variable: " + collapse(text)[:60])


def card_name(src, cls):
    for code, number in scan.REG.findall(src):
        p = scan.load_set(code).get(number)
        if p and p.get("front") and p["front"].get("name"):
            return p["front"]["name"]
    return re.sub(r"(?<!^)(?=[A-Z])", " ", cls)


def java_string(text):
    return '"' + text.replace("\\", "\\\\").replace('"', '\\"') + '"'


def build_effects(f, may_prompt, src, zone_graveyard, cls, needs):
    """The ability's effect argument texts."""
    effects = effect_list(f["effects"], src)
    cost = f["cost"] if present(f, "cost") else None
    once = False
    if not isinstance(effects, tuple):
        inner = [po.call(po.strip_fq(e.strip()), "new OnceOnlyTriggerEffect") for e in effects]
        if any(i is not None for i in inner):
            if not all(i is not None and len(i) == 1 for i in inner) or may_prompt or cost:
                raise Skip("OnceOnlyTriggerEffect on some effects only, or with a payment")
            effects, once = [i[0] for i in inner], True
    if may_prompt is None and cost is None:
        if isinstance(effects, tuple):
            return ["%s.toArray(CardEffect[]::new)" % effects[1]], once, {"com.github.laxika.magicalvibes.model.effect.CardEffect"}
        return effects, once, set()
    if isinstance(effects, tuple):
        body = "SequenceEffect.of(%s.toArray(CardEffect[]::new))" % effects[1]
        needs |= {"com.github.laxika.magicalvibes.model.effect.SequenceEffect",
                  "com.github.laxika.magicalvibes.model.effect.CardEffect"}
    elif len(effects) == 1:
        body = effects[0]
    else:
        body = "SequenceEffect.of(%s)" % ", ".join(effects)
        needs.add("com.github.laxika.magicalvibes.model.effect.SequenceEffect")
    if cost is not None:
        prompt = may_prompt
        if prompt is None:
            prompt = java_string("Pay %s to return %s from your graveyard to your hand?"
                                 % (collapse(cost).strip('"'), card_name(src, cls))) if zone_graveyard \
                else java_string("Pay %s?" % collapse(cost).strip('"'))
        needs.add("com.github.laxika.magicalvibes.model.effect.MayPayManaEffect")
        return ["new MayPayManaEffect(%s, %s, %s)" % (collapse(cost), body, prompt)], once, set()
    needs.add("com.github.laxika.magicalvibes.model.effect.MayEffect")
    return ["new MayEffect(%s, %s)" % (body, may_prompt)], once, set()


def unwrap_effect(expr, src):
    """(fields, may prompt) of an ability's effect argument, or None when it holds no SpellCastTriggerEffect."""
    e = po.strip_fq(uncommented(expr + "\n").strip())
    may = po.call(e, "new MayEffect")
    if may is not None:
        f = parse_sct(may[0], src)
        if f is None:
            return None
        if len(may) != 2:
            raise Skip("MayEffect around a SpellCastTriggerEffect with %d arguments" % len(may))
        return f, may[1]
    f = parse_sct(e, src)
    return (f, None) if f is not None else None


def receiver(src, start):
    """The receiver text of the call at start ('' for none) and where the statement starts."""
    if start == 0 or src[start - 1] != ".":
        return "", start
    stmt = max(src.rfind(";", 0, start), src.rfind("{", 0, start), src.rfind("}", 0, start)) + 1
    return uncommented(src[stmt:start - 1] + "\n").strip(), stmt


def enclosing_call(src, pos):
    """The name of the innermost call whose parentheses enclose pos, or None."""
    depth = 0
    i = pos - 1
    while i >= 0:
        c = src[i]
        if c == ")":
            depth += 1
        elif c == "(":
            if depth == 0:
                m = re.search(r"([\w.]+)\s*$", src[:i])
                return m.group(1) if m else ""
            depth -= 1
        i -= 1
    return None


TARGET_DECL = re.compile(r"(?<![\w.])target(?:X|UpTo|ExactlyX|WhenKicked|WhenGiftPromised|WithDynamic\w*)?\(")


def statement(src, m):
    """The parts of the cast-trigger ability statement at m, or None when it isn't one this run converts."""
    is_ability = m.group(0).startswith("new")
    end = po.close_paren(src, m.end())
    body = src[m.end():end - 1]
    args = po.split_args(uncommented(body + "\n"))
    trig = args[0]
    cond = po.call(po.strip_fq(trig), "new TriggerCondition")
    inner_trig = cond[0] if cond is not None else trig
    fm = re.fullmatch(r"Triggers\.(\w+)\(\)", collapse(inner_trig))
    if not fm or fm.group(1) not in CAST:
        return None
    rest = args[1:]
    existing = None
    if rest and collapse(rest[0]).startswith("TriggerOptions."):
        existing, rest = collapse(rest[0]), rest[1:]
    if len(rest) != 1 or collapse(rest[0]).startswith("TriggerMode."):
        return None
    effect = rest[0].strip()
    return {"m": m, "end": end, "ability": is_ability, "trig": trig, "cond": cond, "family": fm.group(1),
            "comments": comments(body),
            "existing": existing, "effect": effect,
            "var": effect if re.fullmatch(r"[a-z]\w*", effect) else None}


def plan(src, cls, info, f, may_prompt, effects_override=None):
    """(edits, needs, report line) for one statement."""
    m, cond, existing = info["m"], info["cond"], info["existing"]
    if info["ability"]:
        ctx = enclosing_call(src, m.start())
        if ctx is None or not any(ctx.endswith(c) for c in ABILITY_CONTEXTS):
            raise Skip("a TriggeredAbility inside %s" % ctx)
        if existing is not None:
            raise Skip("a TriggeredAbility with options")
    zones = collapse(" ".join(cond[1:])) if cond is not None else ""
    graveyard = "GRAVEYARD" in zones
    if may_prompt is not None and re.search(r"GRAVEYARD|HAND|COMMAND|STACK|LIBRARY", zones):
        raise Skip("an outer MayEffect off the battlefield, which the zone's reader doesn't read")
    needs = set()
    effects, once, extra = build_effects(f, may_prompt, src, graveyard, cls, needs)
    needs |= extra
    if effects_override is not None:
        effects = effects_override
    sfx = suffixes(f)
    condition = f.get("intervening") if present(f, "intervening") else None
    if condition and existing and "withInterveningIf" in existing:
        raise Skip("two intervening conditions")
    if once and existing and "withLimit" in existing:
        raise Skip("two limits")
    opts = existing
    if condition or once:
        opts = do.options_expr(existing, "ONCE" if once else None, condition)
        needs.add("com.github.laxika.magicalvibes.model.trigger.TriggerOptions")
        if once:
            needs.add("com.github.laxika.magicalvibes.model.trigger.TriggerLimit")
    edits = []
    prefix = ""
    target = f.get("target") if present(f, "target") else None
    if target is not None:
        if info["ability"]:
            raise Skip("a target filter on a TriggeredAbility, which has no card target group")
        recv, _ = receiver(src, m.start())
        if recv:
            tcall = re.match(r"target\((.*)\)$", collapse(recv), re.S)
            if not tcall:
                raise Skip("a target filter on a statement with receiver " + collapse(recv)[:40])
            if collapse(po.split_args(tcall.group(1))[0]) != collapse(target):
                raise Skip("a target filter unlike its statement's target group")
        else:
            decls = list(TARGET_DECL.finditer(src))
            if decls:
                bare = re.compile(r"^[ \t]*target\(\s*" + re.escape(collapse(target)) + r"\s*\);[ \t]*\n", re.M)
                bares = list(bare.finditer(src))
                if len(decls) != 1 or len(bares) != 1:
                    raise Skip("a target filter on a card with other target groups")
                edits.append((bares[0].start(), bares[0].end(), ""))
            prefix = "target(%s)." % collapse(target)
    line_start = src.rfind("\n", 0, m.start()) + 1
    indent = re.match(r"[ \t]*", src[line_start:]).group(0)
    head = prefix + m.group(0)
    column = m.start() - line_start + len(head)
    lead = "".join("%s\n%s" % (c, indent) for c in info["comments"])
    edits.append((m.start(), info["end"], lead + statement_text(head, info["trig"], cond, sfx,
                                                                 ([opts] if opts else []) + effects, indent,
                                                                 column)))
    line = "%s\tCONVERTED\t%s\t%s%s%s%s" % (cls, info["family"], collapse("".join(sfx)),
                                            "\tmay" if may_prompt else "", "\tcost" if present(f, "cost") else "",
                                            "\ttarget" if target else "")
    return edits, needs, line


def statement_text(head, trig, cond, sfx, rest, indent, column):
    """The rewritten call: on one line when it fits in 120 columns, otherwise the trigger with one filter per line and
    each other argument on its own line."""
    inner = indent + " " * 8
    base = cond[0] if cond is not None else trig
    flat_trigger = collapse(base) + "".join(collapse(x) for x in sfx)
    if cond is not None:
        flat_trigger = "new TriggerCondition(%s)" % ", ".join([flat_trigger] + [collapse(a) for a in cond[1:]])
    one = ", ".join([flat_trigger] + [collapse(r) for r in rest])
    if column + len(one) + 2 <= 120:
        return head + one + ")"
    if cond is not None:
        trigger = "new TriggerCondition(%s, %s)" % (
            so.trigger_text(base, sfx, inner, column + len("new TriggerCondition(")),
            ", ".join(collapse(a) for a in cond[1:]))
    else:
        trigger = so.trigger_text(base, sfx, inner, column)
    return head + trigger + ",\n" + inner + (",\n" + inner).join(do.pretty(r, inner, len(inner)) for r in rest) + ")"


def convert(src, cls, report):
    needs = set()
    edits = []
    var_uses = {}
    for m in re.finditer(r"\baddTrigger\(|new TriggeredAbility\(", src):
        info = statement(src, m)
        if info is None:
            continue
        if info["var"]:
            var_uses.setdefault(info["var"], []).append(info)
            continue
        try:
            parsed = unwrap_effect(info["effect"], src)
            if parsed is None:
                continue
            e, n, line = plan(src, cls, info, *parsed)
            edits += e
            needs |= n
            report.append(line)
        except Skip as skip:
            report.append("%s\tSKIP\t%s\t%s" % (cls, info["family"], skip))
    for var, uses in var_uses.items():
        try:
            found = do.definition(src, var)
            if found is None:
                continue
            parsed = unwrap_effect(found[2], src)
            if parsed is None:
                continue
            if len(re.findall(r"\b%s\b" % re.escape(var), src)) != 1 + len(uses):
                raise Skip("variable %s is used outside its spell-cast abilities" % var)
            f, may_prompt = parsed
            def_start = src.rfind("\n", 0, found[0]) + 1
            def_end = src.index("\n", found[1]) + 1
            if len(uses) == 1:
                e, n, line = plan(src, cls, uses[0], f, may_prompt)
                edits += e + [(def_start, def_end, "")]
                needs |= n
                report.append(line)
                continue
            built, _, _ = build_effects(f, may_prompt, src, False, cls, set())
            if len(built) != 1:
                raise Skip("variable %s shared by %d abilities holds %d effects" % (var, len(uses), len(built)))
            for info in uses:
                e, n, line = plan(src, cls, info, f, may_prompt, effects_override=[var])
                edits += e
                needs |= n
                report.append(line)
            decl = re.compile(r"\b(?:SpellCastTriggerEffect|CardEffect|MayEffect)(\s+%s\s*=\s*)" % re.escape(var))
            dm = decl.search(src, def_start, found[0])
            if dm is None:
                raise Skip("variable %s has an unexpected type" % var)
            indent = re.match(r"[ \t]*", src[def_start:]).group(0)
            edits.append((dm.start(), found[1], "CardEffect%s%s" % (dm.group(1),
                                                                    so.reindent(built[0], indent))))
            needs.add("com.github.laxika.magicalvibes.model.effect.CardEffect")
        except Skip as skip:
            for info in uses:
                report.append("%s\tSKIP\t%s\t%s" % (cls, info["family"], skip))
    # An ability nested in another's effects (a token's) is converted by the next run of the script.
    kept = [e for e in edits if not any(o is not e and o[0] <= e[0] and e[1] <= o[1] and o[1] - o[0] > e[1] - e[0]
                                        for o in edits)]
    if len(kept) != len(edits):
        report.append("%s\tNESTED\t%d ability(ies) left for the next run" % (cls, len(edits) - len(kept)))
    for start, stop, text in sorted(kept, key=lambda e: e[0], reverse=True):
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
                if not name.endswith(".java"):
                    continue
                if name in KEEP:
                    if "SpellCastTriggerEffect" in open(os.path.join(root, name)).read():
                        report.append("%s\tKEEP" % name[:-5])
                    continue
                path = os.path.join(root, name)
                original = open(path).read()
                if "SpellCastTriggerEffect" not in original:
                    continue
                src, needs = convert(original, name[:-5], report)
                if src == original:
                    continue
                src = po.add_imports(src, sorted(needs))
                src = po.drop_unused_imports(original, src)
                head, sep, body = src.partition("\npublic ")
                src = re.sub(r"\n{3,}", "\n\n", head) + sep + body
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
