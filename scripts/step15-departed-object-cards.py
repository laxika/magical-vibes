#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 3: card classes whose departed-object effects read the stack entry.

Idempotent; run per letter module:

    python3 scripts/step15-departed-object-cards.py magical-vibes-card/d

- DrawCardForEachDyingSourceCounterEffect(type) -> DrawCardEffect(new CountersOnTriggeringPermanent(type));
  (null, then) -> SequenceEffect.of(new DrawCardEffect(new CountersOnTriggeringPermanent()), then)
- ManifestTopCardsForEachDyingSourceCounterEffect() -> ManifestTopCardEffect(new CountersOnTriggeringPermanent())
- PutCounterOnTargetForEachLeavingSourceCounterEffect(type)
  -> PutCounterOnTargetPermanentEffect(type, new CountersOnTriggeringPermanent(type))
- GainLifeEqualToDyingCreatureToughnessEffect() -> GainLifeEffect(new TriggeringPermanentToughness())
- Grim Feast: "whenever a creature is put into an opponent's graveyard" gets its creature filter
- Ambitious Augmenter: "if it had one or more counters on it" becomes the trigger's filter
- Necroskitter, The Reaper King, No More: "you may return/put that card" is a MayEffect
"""
import pathlib
import re
import sys

PKG = "com.github.laxika.magicalvibes"
EFFECT = PKG + ".model.effect."
AMOUNT = PKG + ".model.amount."


def call_args(src, start):
    """Index just past the parenthesised argument list opening at src[start] == '('."""
    depth = 0
    for i in range(start, len(src)):
        if src[i] == '(':
            depth += 1
        elif src[i] == ')':
            depth -= 1
            if depth == 0:
                return i + 1
    raise ValueError("unbalanced parentheses")


def split_args(inner):
    args, depth, cur = [], 0, ""
    for ch in inner:
        if ch in "(<{[":
            depth += 1
        elif ch in ")>}]":
            depth -= 1
        if ch == "," and depth == 0:
            args.append(cur.strip())
            cur = ""
        else:
            cur += ch
    if cur.strip():
        args.append(cur.strip())
    return args


def replace_calls(src, name, build):
    out, pos, token = [], 0, "new " + name + "("
    while True:
        i = src.find(token, pos)
        if i < 0:
            out.append(src[pos:])
            return "".join(out)
        open_paren = i + len(token) - 1
        end = call_args(src, open_paren)
        out.append(src[pos:i])
        out.append(build(split_args(src[open_paren + 1:end - 1])))
        pos = end


def draw(args):
    counter_type = args[0]
    amount = ("new CountersOnTriggeringPermanent()" if counter_type == "null"
              else "new CountersOnTriggeringPermanent(" + counter_type + ")")
    drawn = "new DrawCardEffect(" + amount + ")"
    return drawn if len(args) == 1 else "SequenceEffect.of(" + drawn + ", " + args[1] + ")"


def put_counters(args):
    if len(args) != 1:
        raise ValueError("unexpected PutCounterOnTargetForEachLeavingSourceCounterEffect arguments: %s" % args)
    return ("new PutCounterOnTargetPermanentEffect(" + args[0] + ",\n"
            + "                        new CountersOnTriggeringPermanent(" + args[0] + "))")


def set_imports(src, add, remove):
    for fqcn in remove:
        if re.search(r"\b" + re.escape(fqcn.rsplit(".", 1)[1]) + r"\b",
                     src.replace("import " + fqcn + ";", "")) is None:
            src = src.replace("import " + fqcn + ";\n", "")
    for fqcn in add:
        simple = fqcn.rsplit(".", 1)[1]
        if "import " + fqcn + ";" in src or re.search(r"\b" + simple + r"\b", src) is None:
            continue
        lines = src.split("\n")
        project = [i for i, line in enumerate(lines) if line.startswith("import " + PKG + ".")]
        line = "import " + fqcn + ";"
        after = [i for i in project if lines[i] > line]
        lines.insert(after[0] if after else project[-1] + 1, line)
        src = "\n".join(lines)
    return src


def convert(path):
    src = path.read_text()
    original = src
    name = path.stem

    src = replace_calls(src, "DrawCardForEachDyingSourceCounterEffect", draw)
    src = src.replace("new ManifestTopCardsForEachDyingSourceCounterEffect()",
                      "new ManifestTopCardEffect(new CountersOnTriggeringPermanent())")
    src = replace_calls(src, "PutCounterOnTargetForEachLeavingSourceCounterEffect", put_counters)
    src = src.replace("new GainLifeEqualToDyingCreatureToughnessEffect()",
                      "new GainLifeEffect(new TriggeringPermanentToughness())")

    if name == "GrimFeast":
        src = src.replace("Triggers.dies(null, ObjectRelation.OPPONENT_OWNS)",
                          "Triggers.dies(Triggers.CREATURE, ObjectRelation.OPPONENT_OWNS)")
        src = src.replace(
            "        // life equal to its toughness.\" The toughness used is the creature's last-known effective\n"
            "        // toughness on the battlefield, snapshotted when the trigger is collected.\n",
            "        // life equal to its toughness.\" The toughness used is the creature's last-known effective\n"
            "        // toughness on the battlefield, read from the stack entry as the ability resolves.\n")
    if name == "AmbitiousAugmenter":
        src = src.replace(
            "addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenWithDyingSourceCountersEffect(",
            "addTrigger(Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.THIS),\n"
            "                new CreateTokenWithDyingSourceCountersEffect(")
    if name == "Necroskitter":
        src = src.replace(
            "                new ReturnDyingOpponentCreatureUnderYourControlEffect());",
            "                new MayEffect(new ReturnDyingOpponentCreatureUnderYourControlEffect(),\n"
            "                        \"Return that card to the battlefield under your control?\"));")
        src = src.replace("        // The intervening-if is evaluated against the dying permanent's counters.\n",
                          "        // The trigger's filter is judged against the dying permanent's counters.\n")
    if name == "TheReaperKingNoMore":
        src = src.replace(
            "                new ReturnDyingOpponentCreatureUnderYourControlEffect());",
            "                new MayEffect(new ReturnDyingOpponentCreatureUnderYourControlEffect(),\n"
            "                        \"Put that card onto the battlefield under your control?\"));")

    if src == original:
        return False
    src = set_imports(
        src,
        add=[EFFECT + "DrawCardEffect", EFFECT + "SequenceEffect", EFFECT + "ManifestTopCardEffect",
             EFFECT + "PutCounterOnTargetPermanentEffect", EFFECT + "GainLifeEffect", EFFECT + "MayEffect",
             AMOUNT + "CountersOnTriggeringPermanent", AMOUNT + "TriggeringPermanentToughness",
             PKG + ".model.filter.PermanentHasCountersPredicate", PKG + ".model.CounterType"],
        remove=[EFFECT + "DrawCardForEachDyingSourceCounterEffect",
                EFFECT + "ManifestTopCardsForEachDyingSourceCounterEffect",
                EFFECT + "PutCounterOnTargetForEachLeavingSourceCounterEffect",
                EFFECT + "GainLifeEqualToDyingCreatureToughnessEffect",
                PKG + ".model.CounterType"])
    path.write_text(src)
    return True


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).rglob("*.java")):
            if "/src/main/" in str(path) and convert(path):
                changed.append(path.stem)
    print("\n".join(changed) if changed else "nothing to change")


if __name__ == "__main__":
    main()
