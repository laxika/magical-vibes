#!/usr/bin/env python3
"""Step 14, run 13 of effect-slot-refactor.md: the life gain, loss and payment abilities (step 13 family 9, the
LifeWatch collectors) lose their trigger-only wrappers.

- The counting OncePerTurnTriggerEffect of "for the first time each turn" (OncePerTurnTriggerEffect.firstLifeGain,
  firstLifeLoss, and Gourmand's Talent's keyed form with firstLifeGain set) is the trigger's count:
  Triggers.youGainLife().firstTimeEachTurn(), Triggers.youLoseLife().firstTimeEachTurn().
- "This ability triggers only once each turn" (new OncePerTurnTriggerEffect(e)) is TriggerLimit.ONCE_EACH_TURN, "do
  this only once each turn" (OncePerTurnTriggerEffect.markOnAcceptance) TriggerLimit.DO_ONLY_ONCE_EACH_TURN.
- A two-argument ConditionalEffect directly under the trigger, and Cleric Class's ConditionalEffect.atTriggerTime (its
  Class level), is the intervening "if" (CR 603.4) on the cards of INTERVENING; any other ConditionalEffect stops the
  peeling and stays an effect, and one the tables don't decide stops the script.
- MANUAL: the abilities whose Oracle text is one ability the cards wrote as two (Wax-Wane Witness, Moonstone Harbinger,
  The Master of Lake-town, MACH-1's enters and life-gain halves), Valgavoth's FirstOpponentLifeLossEachTurnTriggerEffect
  (Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn()) and Deathless Knight's graveyard condition.
- Kept: Wedding Ring's ConditionalEffect.atTriggerTime, a condition of the event (run 16).

Each statement whose effects carry a wrapper is rewritten by peeling the wrappers, outermost first, and re-indenting
what is left; a wrapper the script doesn't know stops it. Unused imports of the removed wrappers are dropped and the
imports the new form needs added. Running it again changes nothing.

Usage: python3 scripts/step14-life-options.py [--dry-run] [--report]
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


scan = load("scan", "step14-life-scan.py")
target = load("target", "step14-target-options.py")
tap = load("tap", "step14-tap-options.py")
close_paren, split_args, placed, column = target.close_paren, target.split_args, target.placed, target.column

FAMILY = scan.FAMILY
ABILITY = scan.ABILITY

INTERVENING = {"VampireScrivener", "KefkaRulerOfRuin", "CatCollector", "IntermediateChirography", "GourmandsTalent",
               "ClericClass"}
KEEP = {"WeddingRing"}

PKG = "com.github.laxika.magicalvibes"
tap.IMPORTS = {
    "TriggerOptions": PKG + ".model.trigger.TriggerOptions",
    "TriggerLimit": PKG + ".model.trigger.TriggerLimit",
    "ObjectRelation": PKG + ".model.trigger.ObjectRelation",
    "List": "java.util.List",
}
tap.REMOVABLE = ["OncePerTurnTriggerEffect", "ConditionalEffect", "FirstOpponentLifeLossEachTurnTriggerEffect",
                 "CardEffect", "TriggerOptions", "List", "SequenceEffect", "MillOpponentOnLifeLossEffect"]

MANUAL = {
    "WaxWaneWitness": [("""        addTrigger(Triggers.youGainLife(), new ConditionalEffect(
                new ControllerTurn(), new BoostSelfEffect(1, 0)));
        addTrigger(Triggers.youLoseLife(), new ConditionalEffect(
                new ControllerTurn(), new BoostSelfEffect(1, 0)));
""", """        addTrigger(List.of(Triggers.youGainLife(), Triggers.youLoseLife()),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()),
                new BoostSelfEffect(1, 0));
""")],
    "MoonstoneHarbinger": [("""        CardEffect ownTurnLifeChange = new ConditionalEffect(new ControllerTurn(), lifeChangeEffect);
        addTrigger(Triggers.youGainLife(), new OncePerTurnTriggerEffect(ownTurnLifeChange));
        addTrigger(Triggers.youLoseLife(), new OncePerTurnTriggerEffect(ownTurnLifeChange));
""", """        addTrigger(List.of(Triggers.youGainLife(), Triggers.youLoseLife()),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn())
                        .withLimit(TriggerLimit.ONCE_EACH_TURN),
                lifeChangeEffect);
""")],
    "TheMasterOfLakeTown": [("""        MillOpponentOnLifeLossEffect millOnLifeLoss = new MillOpponentOnLifeLossEffect();
        addTrigger(Triggers.opponentLosesLife(), millOnLifeLoss);
        addTrigger(Triggers.youLoseLife(), millOnLifeLoss);
""", """        addTrigger(List.of(Triggers.opponentLosesLife(), Triggers.youLoseLife()),
                new MillOpponentOnLifeLossEffect());
""")],
    "MACH1SwoopingScoundrel": [("""        OncePerTurnTriggerEffect surveil = new OncePerTurnTriggerEffect(new SurveilEffect(1));
        addTrigger(Triggers.enters(ObjectRelation.THIS), surveil);
        addTrigger(Triggers.youGainLife(), surveil);
""", """        addTrigger(List.of(Triggers.enters(ObjectRelation.THIS), Triggers.youGainLife()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new SurveilEffect(1));
""")],
    "ValgavothHarrowerOfSouls": [("""        addTrigger(Triggers.opponentLosesLife(),
                new FirstOpponentLifeLossEachTurnTriggerEffect(List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1))));
""", """        addTrigger(Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new DrawCardEffect(1));
""")],
    "DeathlessKnight": [("""        addTrigger(new TriggerCondition(Triggers.youGainLife(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                OncePerTurnTriggerEffect.firstLifeGain(new ReturnSourceCardFromGraveyardToOwnerHandEffect()));
""", """        addTrigger(new TriggerCondition(Triggers.youGainLife().firstTimeEachTurn(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, new ReturnSourceCardFromGraveyardToOwnerHandEffect());
""")],
}


def peel(src, span, name):
    """The wrappers around the effect at span, outermost first, and the effect's span."""
    wrappers = []
    while True:
        s, e = span
        text = src[s:e]
        m = re.match(r"(?:new (OncePerTurnTriggerEffect|ConditionalEffect)|(OncePerTurnTriggerEffect|ConditionalEffect)"
                     r"\.(\w+))\(", text)
        if not m or not text.endswith(")") or close_paren(src, s + m.end()) != e:
            return wrappers, span
        args = split_args(src, s + m.end(), e - 1)
        values = [src[a:b] for a, b in args]
        cls, method = m.group(1) or m.group(2), m.group(3)
        if cls == "OncePerTurnTriggerEffect":
            if method in ("firstLifeGain", "firstLifeLoss"):
                wrappers.append(("first", None))
            elif method == "markOnAcceptance":
                wrappers.append(("limit", "DO_ONLY_ONCE_EACH_TURN"))
            elif method is None and len(args) == 1:
                wrappers.append(("limit", "ONCE_EACH_TURN"))
            elif (method is None and len(args) == 5 and values[1] == "false" and values[3] == "false"
                  and values[4] == "true"):
                wrappers.append(("first", None))
            else:
                raise SystemExit("%s: an OncePerTurnTriggerEffect the script doesn't read: %s" % (name, text))
            span = args[0]
        else:
            if not ((method is None and len(args) == 2) or method == "atTriggerTime"):
                return wrappers, span
            if name in KEEP:
                return wrappers, span
            if name not in INTERVENING:
                raise SystemExit("%s: a ConditionalEffect the tables don't decide: %s" % (name, text))
            wrappers.append(("if", values[0]))
            span = args[1]


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
    rest = args[1:]
    if len(rest) != 1:
        return None
    wrappers, effect_span = peel(src, rest[0], name)
    if not wrappers:
        return None
    limit, cond, first = None, None, False
    for kind, value in wrappers:
        if kind == "if":
            cond = value
        elif kind == "first":
            first = True
        elif kind == "limit":
            limit = value
    if first and limit:
        raise SystemExit("%s: a limit next to a count: %s" % (name, src[start:end]))
    line_start = src.rfind("\n", 0, start) + 1
    indent = len(src[line_start:start]) - len(src[line_start:start].lstrip())
    arg_col = indent + 8
    head = src[start:m.end()]
    trigger = "Triggers.%s()" % factory + (".firstTimeEachTurn()" if first else "")
    options = "TriggerOptions.DEFAULT"
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
    report.append("%s\t%s\t%s" % (name, ",".join(k for k, _ in wrappers), re.sub(r"\s+", " ", new)))
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


def convert(src, name, report):
    if name in MANUAL:
        return manual(src, name, report)
    edits = []
    for m in ABILITY.finditer(src):
        end = close_paren(src, m.end())
        if not FAMILY.search(src[m.start():end]):
            continue
        inner = [n for n in ABILITY.finditer(src, m.end(), end)
                 if FAMILY.search(src[n.start():close_paren(src, n.end())])]
        if inner:
            continue
        edit = rewrite(src, m.start(), name, report)
        if edit:
            edits.append(edit)
    for start, end, new in reversed(edits):
        src = src[:start] + new + src[end:]
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
            if new == src:
                continue
            new = tap.fix_imports(new)
            # A removed last java import leaves two blank lines before the class.
            new = re.sub(r"(^import [\w.]+;\n)\n\n+", r"\1\n", new, flags=re.M)
            changed += 1
            print(path)
            if not dry:
                open(path, "w").write(new)
    if "--report" in sys.argv:
        print("\n".join(report), file=sys.stderr)
    print("%d card classes" % changed, file=sys.stderr)


if __name__ == "__main__":
    main()
