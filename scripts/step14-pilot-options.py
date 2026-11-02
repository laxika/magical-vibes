#!/usr/bin/env python3
"""Step 14, run 1 of effect-slot-refactor.md: the trigger-only wrappers of the source's own zone-change abilities
(the step 10 pilot: dies, sacrificed, leaves the battlefield, put into a graveyard, milled, all ObjectRelation.THIS)
become trigger filters and TriggerOptions.

For an ability's one effect, outermost wrapper first, repeatedly:

- new ConditionalEffect(c, x) / (c, x, true): an intervening "if" (CR 603.4) -> addTrigger(trigger,
  TriggerOptions.DEFAULT.withInterveningIf(c), x). SourceIsCreature and NotCondition(SourceIsToken) are about the
  object as it last existed and become the filters CREATURE and NONTOKEN instead. A SourceCardInGraveyard condition
  isn't an intervening "if" ("you may exile it. If you do") and stays an effect.
- ConditionalEffect.atTriggerTime(SourceIsToken / SourceMatchesPermanentPredicate(p), x) -> filter TOKEN / p.
- TriggeringPermanentConditionalEffect(p, x) -> filter p.
- TriggeringCardConditionalEffect(CardTypePredicate(LAND), x) -> filter LAND.
- DyingPermanentWasCreatureConditionalEffect(x) -> filter CREATURE; ...WasNonlandCreature... -> creature and nonland.
- SacrificeOnlyEffect(x) on Triggers.sacrificed(THIS) -> x.
- DrawCardIfDyingSourceHadCounterEffect(t) -> filter PermanentHasCountersPredicate(t), DrawCardEffect(1).

Cards in MANUAL are rewritten by exact replacement. Usage: python3 scripts/step14-pilot-options.py <module dir>...
"""
import os
import re
import sys

ABILITY = re.compile(r"\baddTrigger\(|new TriggeredAbility\(")
PILOT = re.compile(r"^Triggers\.(dies|sacrificed|leavesBattlefield|putIntoGraveyardFrom|putIntoGraveyardFromAnywhere|"
                   r"putIntoGraveyardWhileResolving|milled)\((.*)\)$", re.S)
FQ = "com.github.laxika.magicalvibes.model."

IMPORTS = {
    "TriggerOptions": "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
    "PermanentAllOfPredicate": "com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate",
    "PermanentHasCountersPredicate": "com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate",
    "DrawCardEffect": "com.github.laxika.magicalvibes.model.effect.DrawCardEffect",
    "List": "java.util.List",
}

ONI_CULT_ANVIL_OLD = """        addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL).onlyDuringYourTurn(),
                new OncePerTurnTriggerEffect(new TriggeringPermanentConditionalEffect(
                        new PermanentIsArtifactPredicate(), createConstruct)));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new OncePerTurnTriggerEffect(new ConditionalEffect(new ControllerTurn(), createConstruct)));
"""
ONI_CULT_ANVIL_NEW = """        // One ability (CR 603.3) whose limit covers both conditions: another artifact you control, and this one.
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL)
                        .onlyDuringYourTurn()),
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.ARTIFACT, ObjectRelation.THIS)
                        .onlyDuringYourTurn())),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                List.of(new EffectRegistration(createConstruct, TriggerMode.NORMAL))));
"""
MINTHARA_OLD = """        OncePerTurnTriggerEffect intensify =
                new OncePerTurnTriggerEffect(new IntensifyNamedCardsEffect(CARD_NAME));
        addStaticEffect(new DynamicStaticBoostEffect(new SourceIntensity(), new Fixed(0), GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), intensify);
        addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL), intensify);
"""
MINTHARA_NEW = """        addStaticEffect(new DynamicStaticBoostEffect(new SourceIntensity(), new Fixed(0), GrantScope.ALL_OWN_CREATURES));
        // One ability (CR 603.3) whose limit covers both conditions: this permanent, and another you control.
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(ObjectRelation.THIS)),
                new TriggerCondition(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                List.of(new EffectRegistration(new IntensifyNamedCardsEffect(CARD_NAME), TriggerMode.NORMAL))));
"""
SYR_VONDAM_OLD = """                .addTrigger(Triggers.dies(ObjectRelation.THIS), destroyIfPowerAtLeast)
"""
SYR_VONDAM_NEW = """                .addTrigger(Triggers.dies(new PermanentPowerAtLeastPredicate(4), ObjectRelation.THIS),
                        new DestroyTargetPermanentEffect())
                .bindEffect(destroyIfPowerAtLeast)
"""
ABILITY_IMPORTS = ["com.github.laxika.magicalvibes.model.EffectRegistration",
                   "com.github.laxika.magicalvibes.model.TriggerMode",
                   "com.github.laxika.magicalvibes.model.TriggeredAbility",
                   "com.github.laxika.magicalvibes.model.trigger.TriggerCondition",
                   "com.github.laxika.magicalvibes.model.trigger.TriggerLimit",
                   "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
                   "java.util.List"]
MANUAL = {
    "OniCultAnvil.java": [(ONI_CULT_ANVIL_OLD, ONI_CULT_ANVIL_NEW)],
    "MintharaOfTheAbsolute.java": [(MINTHARA_OLD, MINTHARA_NEW)],
    "SyrVondamSunstarExemplar.java": [(SYR_VONDAM_OLD, SYR_VONDAM_NEW)],
}
MANUAL_IMPORTS = {
    "OniCultAnvil.java": ABILITY_IMPORTS,
    "MintharaOfTheAbsolute.java": ABILITY_IMPORTS,
    "SyrVondamSunstarExemplar.java": ["com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate"],
}


def close_paren(src, i):
    """Index just past the parenthesis that closes the one opened before i."""
    depth = 1
    while depth:
        c = src[i]
        if c == '"':
            i = re.compile(r'(?<!\\)"').search(src, i + 1).start()
        depth += (c == "(") - (c == ")")
        i += 1
    return i


def split_args(text):
    """The top-level, comma-separated arguments of a call's argument text."""
    args, depth, start = [], 0, 0
    i = 0
    while i < len(text):
        c = text[i]
        if c == '"':
            i = re.compile(r'(?<!\\)"').search(text, i + 1).start()
        elif c in "([{":
            depth += 1
        elif c in ")]}":
            depth -= 1
        elif c == "," and depth == 0:
            args.append(text[start:i])
            start = i + 1
        i += 1
    args.append(text[start:])
    return [a.strip() for a in args]


def call(expr, name):
    """The arguments of expr when it is exactly `name(...)`, else None."""
    expr = expr.strip()
    expr = re.sub(r"^" + re.escape(FQ) + r"[a-z]+\.", "", expr)
    if not expr.startswith(name + "(") or close_paren(expr, len(name) + 1) != len(expr):
        return None
    return split_args(expr[len(name) + 1:-1])


def strip_fq(expr):
    return re.sub(re.escape(FQ) + r"[a-z]+\.", "", expr)


def unwrap(effect):
    """(filters, condition, effect) with the outermost trigger-only wrappers taken off."""
    filters, condition = [], None
    while True:
        args = call(effect, "new ConditionalEffect")
        if args is not None and (len(args) == 2 or (len(args) == 3 and args[2] == "true")):
            cond = strip_fq(args[0])
            if cond.startswith("new SourceCardInGraveyard("):
                break
            if cond == "new SourceIsCreature()":
                filters.append("Triggers.CREATURE")
            elif cond == "new NotCondition(new SourceIsToken())":
                filters.append("Triggers.NONTOKEN")
            elif condition is None:
                condition = args[0]
            else:
                raise ValueError("two intervening ifs: " + effect)
            effect = args[1]
            continue
        args = call(effect, "ConditionalEffect.atTriggerTime")
        if args is not None:
            cond = strip_fq(args[0])
            if cond == "new SourceIsToken()":
                filters.append("Triggers.TOKEN")
            else:
                inner = call(cond, "new SourceMatchesPermanentPredicate")
                if inner is None:
                    raise ValueError("unknown trigger-time condition: " + cond)
                filters.append(inner[0])
            effect = args[1]
            continue
        args = call(effect, "new TriggeringPermanentConditionalEffect")
        if args is not None and len(args) == 2:
            filters.append(args[0])
            effect = args[1]
            continue
        args = call(effect, "new TriggeringCardConditionalEffect")
        if args is not None:
            if strip_fq(args[0]) != "new CardTypePredicate(CardType.LAND)":
                raise ValueError("unknown card filter: " + args[0])
            filters.append("Triggers.LAND")
            effect = args[1]
            continue
        args = call(effect, "new DyingPermanentWasCreatureConditionalEffect")
        if args is not None:
            filters.append("Triggers.CREATURE")
            effect = args[0]
            continue
        args = call(effect, "new DyingPermanentWasNonlandCreatureConditionalEffect")
        if args is not None:
            filters.append("new PermanentAllOfPredicate(List.of(Triggers.CREATURE, Triggers.NONLAND))")
            effect = args[0]
            continue
        args = call(effect, "new SacrificeOnlyEffect")
        if args is not None:
            effect = args[0]
            continue
        args = call(effect, "new DrawCardIfDyingSourceHadCounterEffect")
        if args is not None:
            filters.append("new PermanentHasCountersPredicate(" + args[0] + ")")
            effect = "new DrawCardEffect(1)"
            continue
        if "OncePerTurnTriggerEffect" in effect.split("(")[0]:
            raise ValueError("a limit: " + effect)
        break
    return filters, condition, effect


def with_filter(trigger, filters):
    if not filters:
        return trigger
    flt = filters[0] if len(filters) == 1 else "new PermanentAllOfPredicate(List.of(" + ", ".join(filters) + "))"
    m = PILOT.match(trigger)
    args = split_args(m.group(2))
    if args[-1] != "ObjectRelation.THIS" or (len(args) > 1 and m.group(1) != "putIntoGraveyardFrom"):
        raise ValueError("unexpected trigger: " + trigger)
    return "Triggers.%s(%s)" % (m.group(1), ", ".join(args[:-1] + [flt, args[-1]]))


def convert(src):
    out, pos, changed, needs = [], 0, False, set()
    for m in ABILITY.finditer(src):
        if m.start() < pos:
            continue
        end = close_paren(src, m.end())
        args = split_args(src[m.end():end - 1])
        if len(args) != 2 or not PILOT.match(args[0]) or not args[0].endswith("ObjectRelation.THIS)"):
            continue
        filters, condition, effect = unwrap(args[1])
        if not filters and condition is None and effect == args[1]:
            continue
        trigger = with_filter(args[0], filters)
        if condition is not None:
            if m.group(0).startswith("new"):
                raise ValueError("an intervening if on a granted ability: " + src[m.start():end])
            body = "%s, TriggerOptions.DEFAULT.withInterveningIf(%s), %s" % (trigger, condition, effect)
            needs.add("TriggerOptions")
        else:
            body = "%s, %s" % (trigger, effect)
        for name in ("PermanentAllOfPredicate", "PermanentHasCountersPredicate", "DrawCardEffect"):
            if name in body:
                needs.add(name)
        if "List.of(" in body:
            needs.add("List")
        indent = re.search(r"[^\n]*$", src[:m.start()]).group(0)
        indent = " " * (len(indent) - len(indent.lstrip()) + 8)
        out.append(src[pos:m.start()] + m.group(0) + reflow(body, indent) + ")")
        pos = end
        changed = True
    out.append(src[pos:])
    return "".join(out), changed, needs


def reflow(body, indent):
    """One argument per line when the call is long, the way the card classes wrap them; a long trigger with a filter
    wraps its own arguments too."""
    one_line = re.sub(r"\(\s+", "(", re.sub(r"\s+\)", ")", re.sub(r"\s+", " ", body)))
    if len(one_line) <= 90:
        return one_line
    lines = []
    for arg in split_args(one_line):
        m = PILOT.match(arg)
        if m and len(indent) + len(arg) > 110:
            inner = split_args(m.group(2))
            arg = "Triggers.%s(\n%s%s)" % (m.group(1), indent + " " * 8,
                                           (",\n" + indent + " " * 8).join(inner))
        lines.append(arg)
    return ("\n" + indent).join(a + "," for a in lines)[:-1]


def add_imports(src, names):
    """Adds each import that is missing, in order among the card's other plain imports."""
    for fq in names:
        simple = fq.rsplit(".", 1)[1]
        if re.search(r"^import [\w.]+\." + simple + ";", src, re.M):
            continue
        line = "import " + fq + ";\n"
        imports = [m for m in re.finditer(r"^import (?!static)[\w.]+;\n", src, re.M)]
        after = next((m for m in imports if m.group(0) > line), None)
        if after is not None:
            src = src[:after.start()] + line + src[after.start():]
        elif imports:
            src = src[:imports[-1].end()] + line + src[imports[-1].end():]
        else:
            src = re.sub(r"(^package [^\n]+\n\n)", r"\1" + line, src, count=1, flags=re.M)
    return src


def drop_unused_imports(original, src):
    """Drops the imports the rewrite left unused: names the original code used and the new code doesn't."""
    def body(text):
        return re.sub(r"^import [^\n]+\n", "", text, flags=re.M)
    for m in list(re.finditer(r"^import (?!static)[\w.]+\.(\w+);\n", src, re.M)):
        name = re.compile(r"\b" + m.group(1) + r"\b")
        if name.search(body(original)) and not name.search(body(src)):
            src = src.replace(m.group(0), "", 1)
    return src


def main():
    for module in sys.argv[1:]:
        for root, _, files in os.walk(module):
            if "/build/" in root:
                continue
            for name in sorted(files):
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                original = src
                needs = set()
                for old, new in MANUAL.get(name, []):
                    if new in src:
                        continue
                    if old not in src:
                        raise SystemExit("MANUAL replacement not found in " + path)
                    src = src.replace(old, new)
                try:
                    src, changed, needs = convert(src)
                except ValueError as error:
                    raise SystemExit("%s: %s" % (path, error))
                if src == original:
                    continue
                src = add_imports(src, [IMPORTS[n] for n in sorted(needs)] + MANUAL_IMPORTS.get(name, []))
                src = drop_unused_imports(original, src)
                open(path, "w").write(src)
                print(path)


if __name__ == "__main__":
    main()
