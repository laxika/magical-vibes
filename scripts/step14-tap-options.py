#!/usr/bin/env python3
"""Step 14, run 12 of effect-slot-refactor.md: the tap and untap abilities (step 13 family 8, the TapWatch collectors)
lose their trigger-only wrappers.

- A TriggeringPermanentConditionalEffect is the trigger's filter (TapTrigger.withFilter). On
  Triggers.permanentYouControlBecomesTapped() a filter of new PermanentIsSourceCardPredicate() is the trigger
  Triggers.becomesTapped() ("whenever this permanent becomes tapped"), and an all-of filter of it and one more
  predicate is Triggers.becomesTapped().withFilter(that predicate).
- The global OncePerTurnPerCreatureTriggerEffect ("for the first time during each of your turns", "if it's the first
  time that creature has become tapped this turn") is the event: Triggers.becomesTappedForFirstTimeDuringYourTurn() on
  "this creature", Triggers.creatureYouControlBecomesTappedForFirstTimeDuringYourTurn() on a creature filter.
- "This ability triggers only once each turn" (OncePerTurnTriggerEffect) is TriggerLimit.ONCE_EACH_TURN.
- A two-argument ConditionalEffect directly under the trigger and its filter is the intervening "if" (CR 603.4) on the
  cards of INTERVENING; any other ConditionalEffect stops the peeling and stays an effect.
- TriggerMode.ONCE_PER_BATCH ("one or more") is TriggerMultiplicity.ONE_OR_MORE.
- MANUAL: the five "a permanent becomes tapped" cards that wrote one shared effect (a local or a constant) into a
  "you control" and an "opponent controls" ability become one ability with both conditions, and Hangar Scrounger's
  shared rummage effect loses its filter (its printed and backup-granted abilities both become Triggers.becomesTapped()).
- Kept: Well Rested's per-grant limit (OncePerTurnTriggerEffect.perGrant, a key per granting Aura that TriggerOptions
  can't say) and The Ninth Doctor's ConditionalEffect.atTriggerTime; neither is peeled.

Each statement whose effects carry a wrapper is rewritten by peeling the wrappers, outermost first, and re-indenting
what is left; a wrapper the script doesn't know stops it. Unused imports of the removed wrappers are dropped and the
imports the new form needs added. Running it again changes nothing.

Usage: python3 scripts/step14-tap-options.py [--dry-run] [--report]
"""
import importlib.util
import os
import re
import sys

HERE = os.path.dirname(__file__)


def load(name, file):
    spec = importlib.util.spec_from_file_location(name, os.path.join(HERE, file))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


scan = load("scan", "step14-tap-scan.py")
target = load("target", "step14-target-options.py")
close_paren, split_args, placed, column = target.close_paren, target.split_args, target.placed, target.column

FAMILY = scan.FAMILY
ABILITY = scan.ABILITY
WRAPPERS = ("OncePerTurnTriggerEffect", "OncePerTurnPerCreatureTriggerEffect", "TriggeringPermanentConditionalEffect",
            "ConditionalEffect")

INTERVENING = {"BarbflareGremlin", "EncumberedReejerey", "WarchanterSkald", "InterfaceAce"}

SOURCE = "new PermanentIsSourceCardPredicate()"
ALL_OF_SOURCE = re.compile(r"new PermanentAllOfPredicate\(List\.of\(\s*new PermanentIsSourceCardPredicate\(\),\s*")

PKG = "com.github.laxika.magicalvibes"
IMPORTS = {
    "TriggerOptions": PKG + ".model.trigger.TriggerOptions",
    "TriggerLimit": PKG + ".model.trigger.TriggerLimit",
    "List": "java.util.List",
    "PermanentPredicate": PKG + ".model.filter.PermanentPredicate",
}
REMOVABLE = list(WRAPPERS) + ["PermanentIsSourceCardPredicate", "PermanentAllOfPredicate", "PermanentIsCreaturePredicate",
                              "TriggerMode", "CardEffect", "List"]

ROYAL_DECREE_OLD = """        // Whenever a Swamp, Mountain, black permanent, or red permanent becomes tapped, this
        // enchantment deals 1 damage to that permanent's controller. Both ally- and opponent-
        // scoped tap slots are needed; the conditional filters the tapped permanent. One trigger
        // per tap even if the permanent matches multiple criteria (e.g. Badlands).
        var damage = new TriggeringPermanentConditionalEffect(
                SWAMP_MOUNTAIN_BLACK_OR_RED,
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
        addTrigger(Triggers.permanentYouControlBecomesTapped(), damage);
        addTrigger(Triggers.permanentOpponentControlsBecomesTapped(), damage);
"""
ROYAL_DECREE_NEW = """        // Whenever a Swamp, Mountain, black permanent, or red permanent becomes tapped, this
        // enchantment deals 1 damage to that permanent's controller. One ability with a "you
        // control" and an "opponent controls" condition, each filtered to the tapped permanent.
        // One trigger per tap even if the permanent matches multiple criteria (e.g. Badlands).
        addTrigger(List.of(
                        Triggers.permanentYouControlBecomesTapped().withFilter(SWAMP_MOUNTAIN_BLACK_OR_RED),
                        Triggers.permanentOpponentControlsBecomesTapped().withFilter(SWAMP_MOUNTAIN_BLACK_OR_RED)),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
"""

MANUAL = {
    "RoyalDecree": [(ROYAL_DECREE_OLD, ROYAL_DECREE_NEW)],
    "TemporalDistortion": [("""        addTrigger(Triggers.permanentYouControlBecomesTapped(),
                new TriggeringPermanentConditionalEffect(CREATURE_OR_LAND, hourglassCounter));
        addTrigger(Triggers.permanentOpponentControlsBecomesTapped(),
                new TriggeringPermanentConditionalEffect(CREATURE_OR_LAND, hourglassCounter));
""", """        addTrigger(List.of(
                        Triggers.permanentYouControlBecomesTapped().withFilter(CREATURE_OR_LAND),
                        Triggers.permanentOpponentControlsBecomesTapped().withFilter(CREATURE_OR_LAND)),
                hourglassCounter);
""")],
    "HauntingWind": [("""        CardEffect artifactTapTrigger = new TriggeringPermanentConditionalEffect(
                new PermanentIsArtifactPredicate(),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
        addTrigger(Triggers.permanentYouControlBecomesTapped(), artifactTapTrigger);
        addTrigger(Triggers.permanentOpponentControlsBecomesTapped(), artifactTapTrigger);
""", """        addTrigger(List.of(
                        Triggers.permanentYouControlBecomesTapped()
                                .withFilter(new PermanentIsArtifactPredicate()),
                        Triggers.permanentOpponentControlsBecomesTapped()
                                .withFilter(new PermanentIsArtifactPredicate())),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
""")],
    "HawkeyesBow": [("""        TriggeringPermanentConditionalEffect damageTrigger = new TriggeringPermanentConditionalEffect(
                new PermanentIsHostOfSourceAuraPredicate(),
                new EnchantedCreatureDealsDamageToEachOpponentEffect(new Fixed(1)));
        addTrigger(Triggers.permanentYouControlBecomesTapped(), damageTrigger);
        addTrigger(Triggers.permanentOpponentControlsBecomesTapped(), damageTrigger);
""", """        addTrigger(List.of(
                        Triggers.permanentYouControlBecomesTapped()
                                .withFilter(new PermanentIsHostOfSourceAuraPredicate()),
                        Triggers.permanentOpponentControlsBecomesTapped()
                                .withFilter(new PermanentIsHostOfSourceAuraPredicate())),
                new EnchantedCreatureDealsDamageToEachOpponentEffect(new Fixed(1)));
""")],
    "MineLayer": [("""    private static final TriggeringPermanentConditionalEffect DESTROY_MINED_LAND =
            new TriggeringPermanentConditionalEffect(
                    new PermanentAllOfPredicate(List.of(
                            new PermanentIsLandPredicate(),
                            new PermanentHasCountersPredicate(CounterType.MINE))),
                    new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
""", """    private static final PermanentPredicate MINED_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentHasCountersPredicate(CounterType.MINE)));
"""), ("""        addTrigger(Triggers.permanentYouControlBecomesTapped(), DESTROY_MINED_LAND);
        addTrigger(Triggers.permanentOpponentControlsBecomesTapped(), DESTROY_MINED_LAND);
""", """        addTrigger(List.of(
                        Triggers.permanentYouControlBecomesTapped().withFilter(MINED_LAND),
                        Triggers.permanentOpponentControlsBecomesTapped().withFilter(MINED_LAND)),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
""")],
    "HangarScrounger": [("""        CardEffect rummage = new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(),
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));

        addTrigger(Triggers.permanentYouControlBecomesTapped(), rummage);
""", """        CardEffect rummage = new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?");

        addTrigger(Triggers.becomesTapped(), rummage);
"""), ("""                                new TriggeredAbility(Triggers.permanentYouControlBecomesTapped(),
                                rummage)))""", """                                new TriggeredAbility(Triggers.becomesTapped(), rummage)))""")],
}


# Card comments that named the removed wrappers, rewritten after the conversion (exact text; skipped once done).
COMMENTS = {
    "CityOfBrass": ("""        // Whenever this land becomes tapped, it deals 1 damage to you. The conditional restricts the
        // shared tap trigger to the source itself. When it taps for mana the trigger is deferred until
        // a player next receives priority (CR 603.3).
""", """        // Whenever this land becomes tapped, it deals 1 damage to you. When it taps for mana the
        // trigger is deferred until a player next receives priority (CR 603.3).
"""),
    "EncumberedReejerey": ("""        // -1/-1 counter from it. The triggering permanent filter checks the counter when the
        // ability triggers; the conditional wrapper checks it again when the ability resolves.
""", """        // -1/-1 counter from it. The trigger's filter checks the counter when the ability
        // triggers; the intervening "if" checks it again when the ability resolves.
"""),
}


def peel(src, span, name):
    """The wrappers around the effect at span, outermost first, and the effect's span."""
    wrappers = []
    while True:
        s, e = span
        m = re.match(r"new (\w+)\(", src[s:e])
        if not m or m.group(1) not in WRAPPERS:
            return wrappers, span
        wrapper = m.group(1)
        args = split_args(src, s + m.end(), e - 1)
        if wrapper == "ConditionalEffect":
            if len(args) != 2:
                return wrappers, span
            if name not in INTERVENING:
                raise SystemExit("%s: a ConditionalEffect the tables don't decide: %s" % (name, src[s:e]))
            wrappers.append(("if", src[args[0][0]:args[0][1]]))
            span = args[1]
        elif wrapper == "TriggeringPermanentConditionalEffect":
            if len(args) != 2:
                raise SystemExit("%s: a flagged TriggeringPermanentConditionalEffect: %s" % (name, src[s:e]))
            wrappers.append(("filter", args[0]))
            span = args[1]
        elif wrapper == "OncePerTurnPerCreatureTriggerEffect":
            if len(args) != 2 or src[args[1][0]:args[1][1]] != "true":
                raise SystemExit("%s: a per-source OncePerTurnPerCreatureTriggerEffect: %s" % (name, src[s:e]))
            wrappers.append(("first", None))
            span = args[0]
        else:
            if len(args) != 1:
                raise SystemExit("%s: a %s with arguments: %s" % (name, wrapper, src[s:e]))
            wrappers.append((wrapper, None))
            span = args[0]


def source_filter(src, filt, factory, name):
    """The factory and filter text a "you control" filter leaves when it names the source: (factory, filter or None).
    Other filters are kept as they are."""
    text = src[filt[0]:filt[1]]
    if factory != "permanentYouControlBecomesTapped":
        if "PermanentIsSourceCardPredicate" in text:
            raise SystemExit("%s: a source filter on %s" % (name, factory))
        return factory, filt
    if text == SOURCE:
        return "becomesTapped", None
    m = ALL_OF_SOURCE.match(text)
    if m:
        rest = split_args(src, filt[0] + m.end(), filt[0] + len(text) - 2)
        if len(rest) != 1:
            raise SystemExit("%s: a source filter with several predicates: %s" % (name, text))
        return "becomesTapped", rest[0]
    if "PermanentIsSourceCardPredicate" in text:
        raise SystemExit("%s: a source filter the script can't read: %s" % (name, text))
    return factory, filt


def rewrite(src, start, name, report):
    """The replacement for the ability expression starting at src[start], or None to leave it."""
    m = ABILITY.match(src, start)
    end = close_paren(src, m.end())
    args = split_args(src, m.end(), end - 1)
    trigger_span = args[0]
    fm = re.fullmatch(r"Triggers\.(\w+)\(\)", src[trigger_span[0]:trigger_span[1]])
    if not fm:
        return None
    factory = fm.group(1)
    mode = None
    rest = args[1:]
    if len(rest) == 2 and src[rest[0][0]:rest[0][1]].startswith("TriggerMode."):
        mode = src[rest[0][0]:rest[0][1]]
        rest = rest[1:]
    if len(rest) != 1:
        if any(w in src[start:end] for w in WRAPPERS):
            raise SystemExit("%s: several effects with a wrapper: %s" % (name, src[start:end]))
        return None
    wrappers, effect_span = peel(src, rest[0], name)
    if not wrappers:
        if mode:
            raise SystemExit("%s: a trigger mode without wrappers: %s" % (name, src[start:end]))
        return None
    filt, limit, cond, first = None, None, None, False
    for kind, value in wrappers:
        if kind == "filter":
            factory, filt = source_filter(src, value, factory, name)
        elif kind == "if":
            cond = value
        elif kind == "first":
            first = True
        elif kind == "OncePerTurnTriggerEffect":
            limit = "ONCE_EACH_TURN"
    if first:
        if factory == "becomesTapped" and filt is None:
            factory = "becomesTappedForFirstTimeDuringYourTurn"
        elif (factory == "permanentYouControlBecomesTapped" and filt
              and src[filt[0]:filt[1]] == "new PermanentIsCreaturePredicate()"):
            factory, filt = "creatureYouControlBecomesTappedForFirstTimeDuringYourTurn", None
        else:
            raise SystemExit("%s: no first-time event for %s" % (name, factory))
    multiplicity = None
    if mode == "TriggerMode.ONCE_PER_BATCH":
        multiplicity = "ONE_OR_MORE"
    elif mode:
        raise SystemExit("%s: a trigger mode the script doesn't read: %s" % (name, mode))
    line_start = src.rfind("\n", 0, start) + 1
    indent = len(src[line_start:start]) - len(src[line_start:start].lstrip())
    arg_col = indent + 8
    head = src[start:m.end()]
    trigger = "Triggers.%s()" % factory
    if filt:
        filter_col = indent + 16
        trigger += "\n%s.withFilter(%s)" % (" " * filter_col, placed(src, filt, filter_col + len(".withFilter(")))
    options = "TriggerOptions.ONE_OR_MORE" if multiplicity else "TriggerOptions.DEFAULT"
    if cond:
        one = options + ".withInterveningIf(%s)" % cond
        if arg_col + len(one) + (len(".withLimit(TriggerLimit.%s)" % limit) if limit else 0) + 1 > 120:
            options += ".withInterveningIf(\n%s%s)" % (" " * (arg_col + 8), cond)
        else:
            options = one
    if limit:
        options += ".withLimit(TriggerLimit.%s)" % limit
    parts = [trigger] + ([options] if options != "TriggerOptions.DEFAULT" else [])
    effect = placed(src, effect_span, arg_col)
    one_line = head + ", ".join(parts + [effect]) + ")"
    if "\n" not in one_line and column(src, start) + len(one_line) + 1 <= 120:
        new = one_line
    else:
        new = head + (",\n" + " " * arg_col).join(parts + [effect]) + ")"
    report.append("%s\t%s\t%s" % (name, ",".join(k for k, _ in wrappers) + (",mode" if mode else ""),
                                  re.sub(r"\s+", " ", new)))
    return start, end, new


def manual(src, name, report):
    if all(src.count(old) == 0 and src.count(new) == 1 for old, new in MANUAL[name]):
        return src
    for old, new in MANUAL[name]:
        if src.count(old) != 1:
            raise SystemExit("%s: the manual text isn't there once" % name)
        src = src.replace(old, new)
        report.append("%s\tmanual\t%s" % (name, re.sub(r"\s+", " ", new.strip())))
    return src


def fix_imports(src):
    body = src[src.index("public class"):]
    for name in REMOVABLE:
        if not re.search(r"\b%s\b" % name, body):
            src = re.sub(r"import [\w.]+\.%s;\n" % name, "", src)
    for name, fq in IMPORTS.items():
        if re.search(r"\b%s\b" % name, body) and ("import %s;" % fq) not in src:
            line = "import %s;" % fq
            group = "import java." if fq.startswith("java.") else "import com."
            imports = [i for i in re.findall(r"^import [\w.]+;$", src, re.M) if i.startswith(group)]
            if not imports:
                # A first java.util import goes after the last project import, a blank line between.
                last = re.findall(r"^import [\w.]+;$", src, re.M)[-1]
                at = src.index(last + "\n") + len(last) + 1
                src = src[:at] + "\n" + line + "\n" + src[at:]
                continue
            before = [i for i in imports if i < line]
            if before:
                at = src.index(before[-1] + "\n") + len(before[-1]) + 1
            else:
                at = src.index(imports[0])
            src = src[:at] + line + "\n" + src[at:]
    return src


def convert(src, name, report):
    if name in MANUAL:
        return manual(src, name, report)
    edits = []
    for m in ABILITY.finditer(src):
        end = close_paren(src, m.end())
        if not FAMILY.search(src[m.start():end]):
            continue
        # Only the innermost ability expression that holds the family trigger is rewritten.
        inner = [n for n in ABILITY.finditer(src, m.end(), end)
                 if FAMILY.search(src[n.start():close_paren(src, n.end())])]
        if inner:
            continue
        edit = rewrite(src, m.start(), name, report)
        if edit:
            edits.append(edit)
    for start, end, new in reversed(edits):
        src = src[:start] + new + src[end:]
    if edits:
        target.FAMILY = FAMILY
        src = target.reformat_filters(src)
    return src


def main():
    dry = "--dry-run" in sys.argv
    report = []
    changed = 0
    for root, _, files in os.walk("magical-vibes-card"):
        if "/build/" in root or "/src/main/" not in root + "/":
            continue
        for n in sorted(files):
            if not n.endswith(".java"):
                continue
            name = n[:-5]
            path = os.path.join(root, n)
            src = open(path).read()
            if not FAMILY.search(src):
                continue
            new = convert(src, name, report)
            if name in COMMENTS and COMMENTS[name][0] in new:
                new = new.replace(*COMMENTS[name])
            if new == src:
                continue
            new = fix_imports(new)
            changed += 1
            print(path)
            if not dry:
                open(path, "w").write(new)
    if "--report" in sys.argv:
        print("\n".join(report), file=sys.stderr)
    print("%d card classes" % changed, file=sys.stderr)


if __name__ == "__main__":
    main()
