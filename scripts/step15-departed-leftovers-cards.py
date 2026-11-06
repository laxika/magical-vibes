#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 5: card classes whose last departed-permanent rebuilds are gone.

Idempotent; run per letter module:

    python3 scripts/step15-departed-leftovers-cards.py magical-vibes-card/a

- PutCounterOnTargetForEachDyingSourceCounterEffect loses `optional`: the "you may" is a MayEffect on the card.
  Modular's ability (an optional artifact-creature form on a card with modular, or Blaster, Combat DJ's grant of
  modular 1) becomes `new MayEffect(PutCounterOnTargetForEachDyingSourceCounterEffect.modularAbility(), MODULAR_PROMPT)`,
  which also sets the `modular` flag the collector used to derive from the dying card's printed keyword;
  Soulstinger's "you may" becomes a MayEffect; `(type, false, predicate)` becomes `(type, predicate)`
- Phantasmal Sphere: X is the counters on the triggering permanent (CountersOnTriggeringPermanent), not frozen
- Soulgorger Orgg: GainLifeEqualToLifeLostWhenEnteredEffect() -> GainLifeEffect(LifeLostWhenTriggeringPermanentEntered)
- Cathedral Membrane: "dies during combat" is the ability's event condition (DuringCombat); its card test no longer
  reads the blocked attacker from the entry's target ids (run it on magical-vibes-card-test/c)
"""
import pathlib
import re
import sys

PKG = "com.github.laxika.magicalvibes"
EFFECT = PKG + ".model.effect."
AMOUNT = PKG + ".model.amount."
FILTER = PKG + ".model.filter."

COUNTER_EFFECT = "PutCounterOnTargetForEachDyingSourceCounterEffect"

# Cards whose dying-source counter ability is modular's (CR 702.43a), checked against their Oracle text.
MODULAR = {
    "ArcboundBruiser", "ArcboundCrusher", "ArcboundFiend", "ArcboundHybrid", "ArcboundJavelineer", "ArcboundLancer",
    "ArcboundMouser", "ArcboundOverseer", "ArcboundPrototype", "ArcboundRavager", "ArcboundReclaimer",
    "ArcboundShikari", "ArcboundSlasher", "ArcboundSlith", "ArcboundStinger", "ArcboundTracker", "ArcboundWanderer",
    "ArcboundWhelp", "ArcboundWorker", "BlasterCombatDJ", "BlasterMoraleBooster", "PowerDepot",
    "ScrapyardRecombiner", "ZabazTheGlimmerwasp",
}

ARTIFACT_CREATURE = re.compile(
    r"^new PermanentAllOfPredicate\(List\.of\(\s*new PermanentIsArtifactPredicate\(\),\s*"
    r"new PermanentIsCreaturePredicate\(\)\s*\)\)$")

SOULSTINGER_PROMPT = "Put a -1/-1 counter on target creature for each -1/-1 counter on this creature?"


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
        line_start = src.rfind("\n", 0, i) + 1
        indent = len(src[line_start:i]) - len(src[line_start:i].lstrip())
        out.append(src[pos:i])
        built = build(split_args(src[open_paren + 1:end - 1]), indent)
        out.append(src[i:end] if built is None else built)
        pos = end


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
    if "import java.util.List;" in src and re.search(r"\bList\b", src.replace("import java.util.List;", "")) is None:
        src = src.replace("import java.util.List;\n", "")
    src = re.sub(r"\n\n\n+import", "\n\nimport", src)
    src = re.sub(r"(import [\w.]+;\n)\n\n+(@|public)", r"\1\n\2", src)
    return src


def replace_exact(src, old, new, name):
    if old not in src:
        raise ValueError("%s: expected text not found:\n%s" % (name, old))
    return src.replace(old, new)


def artifact_creature_variable(src, ident):
    """Whether `ident` is declared as the artifact-creature predicate in `src`."""
    m = re.search(r"PermanentPredicate " + re.escape(ident) + r" = (new PermanentAllOfPredicate\(List\.of\([^;]*\)\));", src)
    return m is not None and ARTIFACT_CREATURE.match(m.group(1)) is not None


def counter_effect(name, src):
    def build(args, indent):
        pad = " " * (indent + 8)
        if len(args) == 1 or (len(args) == 2 and args[1] not in ("true", "false")):
            return None
        if len(args) == 3 and args[1] == "false":
            return "new %s(\n%s%s,\n%s%s)" % (COUNTER_EFFECT, pad, args[0], pad, args[2])
        if len(args) == 2 and args[1] == "true":
            if args[0] != "CounterType.MINUS_ONE_MINUS_ONE" or name != "Soulstinger":
                raise ValueError("%s: unexpected optional form %s" % (name, args))
            return "new MayEffect(new %s(%s),\n%s\"%s\")" % (COUNTER_EFFECT, args[0], pad, SOULSTINGER_PROMPT)
        if len(args) in (3, 4) and args[1] == "true" and (len(args) == 3 or args[3] == "true"):
            predicate = args[2]
            is_artifact_creature = ARTIFACT_CREATURE.match(predicate) is not None or (
                re.fullmatch(r"\w+", predicate) is not None and artifact_creature_variable(src, predicate))
            if args[0] != "CounterType.PLUS_ONE_PLUS_ONE" or not is_artifact_creature or name not in MODULAR:
                raise ValueError("%s: optional form that isn't modular: %s" % (name, args))
            return "new MayEffect(%s.modularAbility(),\n%s%s.MODULAR_PROMPT)" % (COUNTER_EFFECT, pad, COUNTER_EFFECT)
        raise ValueError("%s: unexpected arguments %s" % (name, args))
    return build


def drop_unused_variable(src, ident):
    """Removes a local predicate declaration nothing reads any more."""
    m = re.search(r"\n[ \t]*PermanentPredicate " + re.escape(ident) + r" = [^;]*;\n", src)
    if m is None:
        return src
    rest = src[:m.start()] + src[m.end():]
    if re.search(r"\b" + re.escape(ident) + r"\b", rest) is not None:
        return src
    return src[:m.start()] + "\n" + src[m.end():]


def convert(path):
    src = path.read_text()
    original = src
    name = path.stem

    if "new " + COUNTER_EFFECT + "(" in src:
        src = replace_calls(src, COUNTER_EFFECT, counter_effect(name, src))
        src = drop_unused_variable(src, "artifactCreature")

    src = src.replace(
        "        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(" + COUNTER_EFFECT + ".modularAbility(),\n"
        "                " + COUNTER_EFFECT + ".MODULAR_PROMPT));",
        "        addTrigger(Triggers.dies(ObjectRelation.THIS),\n"
        "                new MayEffect(" + COUNTER_EFFECT + ".modularAbility(),\n"
        "                        " + COUNTER_EFFECT + ".MODULAR_PROMPT));")

    if name == "PhantasmalSphere" and "new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)" in src:
        src = replace_exact(
            src,
            "        // X is frozen from the leaving permanent when the trigger is collected.\n",
            "        // X is read from the permanent as it last existed on the battlefield as the ability resolves.\n",
            name)
        src = src.replace("new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)",
                          "new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE)")

    src = src.replace("new GainLifeEqualToLifeLostWhenEnteredEffect()",
                      "new GainLifeEffect(new LifeLostWhenTriggeringPermanentEntered())")

    if name == "CathedralMembrane" and "DuringCombat" not in src:
        src = replace_exact(
            src,
            "        addTrigger(Triggers.dies(ObjectRelation.THIS), new DealDamageToBlockedAttackersOnDeathEffect(6));",
            "        addTrigger(Triggers.dies(ObjectRelation.THIS), TriggerOptions.DEFAULT.withEventCondition(new DuringCombat()),\n"
            "                new DealDamageToBlockedAttackersOnDeathEffect(6));",
            name)

    if name == "CathedralMembraneTest":
        # The blocked creatures are no longer baked into the entry's target ids; resolving shows which it damages.
        src = src.replace(
            "                && e.getCard().getName().equals(\"Cathedral Membrane\")\n"
            "                && e.getTargetIds().contains(attackerId));",
            "                && e.getCard().getName().equals(\"Cathedral Membrane\"));")

    if src == original:
        return False
    src = set_imports(
        src,
        add=[EFFECT + "MayEffect", EFFECT + "GainLifeEffect", AMOUNT + "CountersOnTriggeringPermanent",
             AMOUNT + "LifeLostWhenTriggeringPermanentEntered", PKG + ".model.trigger.TriggerOptions",
             PKG + ".model.condition.DuringCombat"],
        remove=[EFFECT + "GainLifeEqualToLifeLostWhenEnteredEffect", AMOUNT + "CountersOnSource",
                FILTER + "PermanentAllOfPredicate", FILTER + "PermanentIsArtifactPredicate",
                FILTER + "PermanentIsCreaturePredicate", FILTER + "PermanentPredicate",
                PKG + ".model.CounterType"])
    path.write_text(src)
    return True


def main():
    changed = []
    for root in sys.argv[1:]:
        for path in sorted(pathlib.Path(root).rglob("*.java")):
            if convert(path):
                changed.append(path.stem)
    print("%d card classes changed: %s" % (len(changed), ", ".join(changed)))


if __name__ == "__main__":
    main()
