#!/usr/bin/env python3
"""Step 14, run 6c of effect-slot-refactor.md: the spell, copy and land-play condition-as-effect classes become their
trigger's filter and the ability's effects.

An ability written as `addTrigger(Triggers.youCastSpell(), new NthSpellCastTriggerEffect(2, List.of(effects...)))`
(or `new MayEffect(<that>, prompt)`, or a local variable with one definition) becomes

    [target(targetFilter).]addTrigger(Triggers.youCastSpell().nthThisTurn(2), effects...)

with, per class:

- NthSpellCastTriggerEffect(n, effects[, scope[, zone]]): `.castFrom(zone)` `.nthThisTurn(n)`, and
  `.countingEveryPlayersSpells()` for CountScope.ANY_PLAYER;
- KickedSpellCastTriggerEffect(effects[, perKick]): `.kicked()`, or `.eachKick()` per kick;
- FirstMatchingSpellCastTriggerEffect(filter, effects): `.withFilter(filter).nthThisTurn(1)`;
- FirstMulticoloredSpellCastTriggerEffect(effects): a multicolored filter, `.nthThisTurn(1)`;
- FirstNoncreatureSpellCastTriggerEffect(effects): a noncreature filter, `.nthThisTurn(1).countingEveryPlayersSpells()`;
- SpellCastFromHandTriggerEffect(filter, effects): `[.withFilter(filter)].castFrom(Zone.HAND)`;
- CastFromGraveyardTriggerEffect(effects): `.castFrom(Zone.GRAVEYARD)`;
- CastFromLibraryTriggerEffect(effects): a creature filter, `.castFrom(Zone.LIBRARY)`;
- TeamworkSpellCastTriggerEffect(effects): `.withStackEntryFilter(new StackEntryCastWithTeamworkPredicate())`;
- ChosenSubtypeSpellCastTriggerEffect(effects[, creatureOnly]): `.withFilter(new
  CardHasSourceChosenSubtypePredicate(creatureOnly))`;
- SpellCastFromOutsideStartingDeckTriggerEffect(effects): a "not from your starting deck" filter;
- PlayFromOutsideHandTriggerEffect(effects[, targetFilter]): `.notCastFrom(Zone.HAND)` on a cast trigger,
  `.notPlayedFrom(Zone.HAND)` on a land-play trigger;
- LandPlayFromExileTriggerEffect(effects[, targetFilter]): `.playedFrom(Zone.EXILE)` on a land-play trigger;
- SpellCopyTriggerEffect(spell, effects, [targetFilter], [copiedSpellCondition], [allSpellTypes], [optional]) on a copy
  trigger: `.withFilter(spell)` (or `.anySpell()`), `.withStackEntryFilter(condition)`; without allSpellTypes the
  filter must already say "instant or sorcery".

An outer MayEffect becomes a MayEffect around the effects (several become one SequenceEffect.of). A target filter
becomes a target group of the card: dropped when the statement is already on target(sameFilter) or when its effects
are the effects of another statement on target(sameFilter) (a magecraft card's cast half), otherwise the statement is
put on target(filter[, 0, 1]) when the card declares no other target group.

TriggeredAbility.legacyEffects() gives the collectors a SpellCastTriggerEffect, SpellCopyTriggerEffect,
LandPlayFromExileTriggerEffect or PlayFromOutsideHandTriggerEffect of the converted ability. Abilities the script can't
convert are reported with a reason and left as they are (KEEP lists the cards kept on purpose).

--dry-run prints the diffs, --report lists every ability converted or skipped. Usage:
python3 scripts/step14-spell-condition-options.py [--dry-run] [--report] <module dir>...
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


cast = _load("spell_cast_options", "step14-spell-cast-options.py")
so, do, po = cast.so, cast.do, cast.po
Skip = cast.Skip
collapse = cast.collapse

CAST = ("youCastSpell", "opponentCastsSpell", "playerCastsSpell")
COPY = ("youCopySpell", "opponentCopiesSpell")
LAND = ("youPlayLand", "opponentPlaysLand")
cast.CAST = CAST + COPY + LAND

CLASSES = ("NthSpellCastTriggerEffect", "KickedSpellCastTriggerEffect", "FirstMatchingSpellCastTriggerEffect",
           "FirstMulticoloredSpellCastTriggerEffect", "FirstNoncreatureSpellCastTriggerEffect",
           "SpellCastFromHandTriggerEffect", "CastFromGraveyardTriggerEffect", "CastFromLibraryTriggerEffect",
           "TeamworkSpellCastTriggerEffect", "ChosenSubtypeSpellCastTriggerEffect",
           "SpellCastFromOutsideStartingDeckTriggerEffect", "PlayFromOutsideHandTriggerEffect",
           "LandPlayFromExileTriggerEffect", "SpellCopyTriggerEffect")

KEEP = {
    # A Treasure-owned delayed ability registered globally ("whenever you play a land or cast a spell this way"), bound
    # to the cards exiled with its source: its PlayFromOutsideHandTriggerEffect is a TemporaryGlobalTriggerEffect.
    "AppleOfEdenIsuRelic.java",
    # The land trigger shares a local variable with a target filter and two land-play abilities; done by hand.
    "RoccoStreetChef.java",
}

PKG = "com.github.laxika.magicalvibes.model."
IMPORTS = {
    "Zone.": PKG + "Zone",
    "CardIsMulticoloredPredicate": PKG + "filter.CardIsMulticoloredPredicate",
    "CardNotPredicate": PKG + "filter.CardNotPredicate",
    "CardTypePredicate": PKG + "filter.CardTypePredicate",
    "CardType.": PKG + "CardType",
    "StackEntryCastWithTeamworkPredicate": PKG + "filter.StackEntryCastWithTeamworkPredicate",
    "CardHasSourceChosenSubtypePredicate": PKG + "filter.CardHasSourceChosenSubtypePredicate",
    "CardInStartingDeckPredicate": PKG + "filter.CardInStartingDeckPredicate",
}


def zone_of(text):
    z = collapse(po.strip_fq(text))
    m = re.fullmatch(r"(?:Zone\.)?([A-Z_]+)", z)
    if not m:
        raise Skip("a zone that isn't a Zone constant: " + z)
    return "Zone." + m.group(1)


def parse(expr, src, family):
    """(fields, suffixes) of a condition-as-effect expression, or None when it isn't one."""
    e = po.strip_fq(cast.uncommented(expr + "\n").strip())
    m = re.match(r"new (\w+)\(", e)
    if not m or m.group(1) not in CLASSES:
        return None
    name = m.group(1)
    args = po.call(e, "new " + name)
    f = {"cls": name}
    sfx = []

    def expect(families):
        if family not in families:
            raise Skip("%s on %s" % (name, family))

    if name == "NthSpellCastTriggerEffect":
        expect(CAST)
        if len(args) not in (2, 3, 4):
            raise Skip("NthSpellCastTriggerEffect with %d arguments" % len(args))
        f["effects"] = args[1]
        if len(args) == 4 and collapse(args[3]) != "null":
            sfx.append(".castFrom(%s)" % zone_of(args[3]))
        sfx.append(".nthThisTurn(%s)" % collapse(args[0]))
        if len(args) >= 3:
            scope = collapse(args[2])
            if scope.endswith("ANY_PLAYER"):
                sfx.append(".countingEveryPlayersSpells()")
            elif not scope.endswith("CONTROLLER"):
                raise Skip("NthSpellCastTriggerEffect count scope " + scope)
    elif name == "KickedSpellCastTriggerEffect":
        expect(CAST)
        f["effects"] = args[0]
        per_kick = len(args) == 2 and collapse(args[1]) == "true"
        sfx.append(".eachKick()" if per_kick else ".kicked()")
    elif name == "FirstMatchingSpellCastTriggerEffect":
        expect(CAST)
        f["effects"] = args[1]
        sfx += [".withFilter(%s)" % args[0].strip(), ".nthThisTurn(1)"]
    elif name == "FirstMulticoloredSpellCastTriggerEffect":
        expect(CAST)
        f["effects"] = args[0]
        sfx += [".withFilter(new CardIsMulticoloredPredicate())", ".nthThisTurn(1)"]
    elif name == "FirstNoncreatureSpellCastTriggerEffect":
        expect(CAST)
        f["effects"] = args[0]
        sfx += [".withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))", ".nthThisTurn(1)",
                ".countingEveryPlayersSpells()"]
    elif name == "SpellCastFromHandTriggerEffect":
        expect(CAST)
        f["effects"] = args[1]
        if collapse(args[0]) != "null":
            sfx.append(".withFilter(%s)" % args[0].strip())
        sfx.append(".castFrom(Zone.HAND)")
    elif name == "CastFromGraveyardTriggerEffect":
        expect(CAST)
        f["effects"] = args[0]
        sfx.append(".castFrom(Zone.GRAVEYARD)")
    elif name == "CastFromLibraryTriggerEffect":
        expect(CAST)
        f["effects"] = args[0]
        sfx += [".withFilter(new CardTypePredicate(CardType.CREATURE))", ".castFrom(Zone.LIBRARY)"]
    elif name == "TeamworkSpellCastTriggerEffect":
        expect(CAST)
        f["effects"] = args[0]
        sfx.append(".withStackEntryFilter(new StackEntryCastWithTeamworkPredicate())")
    elif name == "ChosenSubtypeSpellCastTriggerEffect":
        expect(CAST)
        f["effects"] = args[0]
        creature_only = len(args) == 1 or collapse(args[1]) == "true"
        sfx.append(".withFilter(new CardHasSourceChosenSubtypePredicate(%s))"
                   % ("true" if creature_only else "false"))
    elif name == "SpellCastFromOutsideStartingDeckTriggerEffect":
        expect(CAST)
        f["effects"] = args[0]
        sfx.append(".withFilter(new CardNotPredicate(new CardInStartingDeckPredicate()))")
    elif name in ("PlayFromOutsideHandTriggerEffect", "LandPlayFromExileTriggerEffect"):
        if len(args) not in (1, 2):
            raise Skip("%s with %d arguments" % (name, len(args)))
        f["effects"] = args[0]
        if len(args) == 2:
            f["target"] = args[1]
        if name == "LandPlayFromExileTriggerEffect":
            expect(LAND)
            sfx.append(".playedFrom(Zone.EXILE)")
        else:
            expect(CAST + LAND)
            sfx.append(".notPlayedFrom(Zone.HAND)" if family in LAND else ".notCastFrom(Zone.HAND)")
    elif name == "SpellCopyTriggerEffect":
        expect(COPY)
        spell, f["effects"] = args[0], args[1]
        rest = args[2:]
        target = condition = None
        all_types = optional = False
        if len(rest) == 1:
            target = rest[0]
        elif len(rest) == 2 and collapse(rest[1]) in ("true", "false") and cast.kind(rest[0], src) == "target":
            target, optional = rest[0], collapse(rest[1]) == "true"
        elif len(rest) == 2:
            condition, all_types = rest[0], collapse(rest[1]) == "true"
        elif len(rest) == 3:
            target, condition, all_types = rest[0], rest[1], collapse(rest[2]) == "true"
        elif len(rest) == 4:
            target, condition = rest[0], rest[1]
            all_types, optional = collapse(rest[2]) == "true", collapse(rest[3]) == "true"
        elif rest:
            raise Skip("SpellCopyTriggerEffect with %d arguments" % len(args))
        spell_present = collapse(spell) != "null"
        if not all_types and not spell_present:
            raise Skip("SpellCopyTriggerEffect of instants and sorceries without a spell filter")
        if spell_present:
            sfx.append(".withFilter(%s)" % spell.strip())
        condition = None if condition is None or collapse(po.strip_fq(condition)) in ("null", "(StackEntryPredicate) null") \
            else condition
        if condition is not None:
            sfx.append(".withStackEntryFilter(%s)" % condition.strip())
        if not sfx:
            sfx.append(".anySpell()")
        if target is not None and collapse(target) != "null":
            f["target"] = target
            f["optional"] = optional
        elif optional:
            raise Skip("an optional target without a target filter")
    return f, sfx


def unwrap(expr, src, family):
    """(fields, suffixes, may prompt) of an ability's effect argument, or None when it holds no class of this run."""
    e = po.strip_fq(cast.uncommented(expr + "\n").strip())
    may = po.call(e, "new MayEffect")
    if may is not None:
        parsed = parse(may[0], src, family)
        if parsed is None:
            return None
        if len(may) != 2:
            raise Skip("MayEffect around a condition with %d arguments" % len(may))
        return parsed[0], parsed[1], may[1]
    parsed = parse(e, src, family)
    return (parsed[0], parsed[1], None) if parsed is not None else None


def effects_text(effects):
    """The effect expressions an effects argument stands for, by identity: a list variable stays a variable."""
    listed = cast.effect_list(effects, "")
    if isinstance(listed, tuple):
        return ("VAR", listed[1])
    return tuple(collapse(e) for e in listed)


def bound_elsewhere(src, info, f):
    """Whether the ability's effects are the effects of another statement on target(sameFilter)."""
    mine = effects_text(f["effects"])
    target = collapse(f["target"])
    for m in re.finditer(r"\btarget\(", src):
        end = po.close_paren(src, m.end())
        if collapse(po.split_args(src[m.end():end - 1])[0]) != target:
            continue
        follow = re.match(r"\s*\.\s*addTrigger\(", src[end:])
        if not follow:
            continue
        start = end + follow.end()
        stop = po.close_paren(src, start)
        args = po.split_args(cast.uncommented(src[start:stop - 1] + "\n"))[1:]
        if args and collapse(args[0]).startswith("TriggerOptions."):
            args = args[1:]
        theirs = tuple(collapse(a) for a in args)
        if isinstance(mine, tuple) and mine and mine[0] == "VAR":
            if theirs == ("%s.toArray(CardEffect[]::new)" % mine[1],):
                return True
        elif theirs == mine:
            return True
    return False


def plan(src, cls, info, f, sfx, may_prompt, effects_override=None):
    """(edits, needs, report line) for one statement."""
    m, cond, existing = info["m"], info["cond"], info["existing"]
    if info["ability"]:
        ctx = cast.enclosing_call(src, m.start())
        if ctx is None or not any(ctx.endswith(c) for c in cast.ABILITY_CONTEXTS):
            raise Skip("a TriggeredAbility inside %s" % ctx)
    if cond is not None:
        raise Skip("a TriggerCondition with zones")
    if existing is not None:
        raise Skip("an ability with options")
    needs = set()
    effects, once, extra = cast.build_effects(f, may_prompt, src, False, cls, needs)
    needs |= extra
    if once:
        raise Skip("OnceOnlyTriggerEffect inside the condition")
    if effects_override is not None:
        effects = effects_override
    edits = []
    prefix = ""
    target = f.get("target")
    if target is not None:
        if info["ability"]:
            raise Skip("a target filter on a TriggeredAbility, which has no card target group")
        recv, _ = cast.receiver(src, m.start())
        if recv:
            tcall = re.match(r"target\((.*)\)$", collapse(recv), re.S)
            if not tcall or collapse(po.split_args(tcall.group(1))[0]) != collapse(target):
                raise Skip("a target filter unlike its statement's target group")
        elif not bound_elsewhere(src, info, f):
            if list(cast.TARGET_DECL.finditer(src)):
                raise Skip("a target filter on a card with other target groups")
            prefix = "target(%s%s)." % (collapse(target), ", 0, 1" if f.get("optional") else "")
    line_start = src.rfind("\n", 0, m.start()) + 1
    indent = re.match(r"[ \t]*", src[line_start:]).group(0)
    head = prefix + m.group(0)
    column = m.start() - line_start + len(head)
    lead = "".join("%s\n%s" % (c, indent) for c in info["comments"])
    edits.append((m.start(), info["end"], lead + cast.statement_text(head, info["trig"], None, sfx, effects, indent,
                                                                      column)))
    for key, imp in IMPORTS.items():
        if key in "".join(sfx):
            needs.add(imp)
    line = "%s\tCONVERTED\t%s\t%s\t%s%s%s" % (cls, info["family"], f["cls"], collapse("".join(sfx)),
                                             "\tmay" if may_prompt else "", "\ttarget" if target else "")
    return edits, needs, line


def convert(src, cls, report):
    needs = set()
    edits = []
    var_uses = {}
    for m in re.finditer(r"\baddTrigger\(|new TriggeredAbility\(", src):
        info = cast.statement(src, m)
        if info is None:
            continue
        if info["var"]:
            var_uses.setdefault(info["var"], []).append(info)
            continue
        try:
            parsed = unwrap(info["effect"], src, info["family"])
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
            parsed = unwrap(found[2], src, uses[0]["family"])
            if parsed is None:
                continue
            if len(re.findall(r"\b%s\b" % re.escape(var), src)) != 1 + len(uses) or len(uses) != 1:
                raise Skip("variable %s is used outside its one ability" % var)
            def_start = src.rfind("\n", 0, found[0]) + 1
            def_end = src.index("\n", found[1]) + 1
            e, n, line = plan(src, cls, uses[0], *parsed)
            edits += e + [(def_start, def_end, "")]
            needs |= n
            report.append(line)
        except Skip as skip:
            for info in uses:
                report.append("%s\tSKIP\t%s\t%s" % (cls, info["family"], skip))
    for start, stop, text in sorted(edits, key=lambda e: e[0], reverse=True):
        src = src[:start] + text + src[stop:]
    return src, needs


def main():
    dry = "--dry-run" in sys.argv
    show_report = "--report" in sys.argv
    report = []
    changed = 0
    pattern = re.compile(r"\b(%s)\b" % "|".join(CLASSES))
    for module in [a for a in sys.argv[1:] if not a.startswith("--")]:
        for root, _, files in os.walk(module):
            if "/build/" in root or "/test/" in root:
                continue
            for name in sorted(files):
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                original = open(path).read()
                if not pattern.search(original):
                    continue
                if name in KEEP:
                    report.append("%s\tKEEP" % name[:-5])
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
