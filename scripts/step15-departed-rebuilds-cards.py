#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 4: card classes whose departed-permanent rebuilds and skips are gone.

Idempotent; run per letter module:

    python3 scripts/step15-departed-rebuilds-cards.py magical-vibes-card/o

- Ochre Jelly: "if it had two or more +1/+1 counters on it" is the trigger's filter, "half that many" the effect's
  amount (Divided(CountersOnTriggeringPermanent(+1/+1), 2))
- Nine-Lives Familiar: "if it had a revival counter on it" is the trigger's filter, "one fewer" the
  RegisterDelayedSelfReturnFromGraveyardEffect's amount
- Galuf's Final Act: PutCountersOnTargetForEachDyingSourcePowerEffect -> up-to-one PutCounterOnTargetPermanentEffect
  with SourcePower, restricted to creatures
- Toxrill, Village Pillagers: CreateTokenIfDyingSourceHadCounterEffect(type, token) -> token, the counter the filter
- Athreos: "a creature with a coin counter on it" is both triggers' filter; ReturnTriggeringPermanentToBattlefieldEffect
- MoveDyingSourceCountersToTargetCreatureEffect loses requiresCounters: Iron Apprentice and Scolding Administrator get
  "if it had counters on it" as the trigger's filter, alwaysTriggers(x) / (x, false) become (x)
- GainLifeEqualToDyingSourcePowerEffect() -> GainLifeEffect(new SourcePower());
  TargetPlayerLosesLifeEqualToPowerEffect() -> LoseLifeEffect(new SourcePower(), LoseLifeRecipient.TARGET_PLAYER)
- Revered Unicorn: "the number of age counters on it" reads the triggering permanent
"""
import pathlib
import re
import sys

PKG = "com.github.laxika.magicalvibes"
EFFECT = PKG + ".model.effect."
AMOUNT = PKG + ".model.amount."
FILTER = PKG + ".model.filter."

HAS_COUNTERS = "new PermanentHasCountersPredicate(CounterType.ANY)"


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


def replace_exact(src, old, new, name):
    if old not in src:
        raise ValueError("%s: expected text not found:\n%s" % (name, old))
    return src.replace(old, new)


def counter_token(args):
    """CreateTokenIfDyingSourceHadCounterEffect(type, token) -> token; the type goes into the filter."""
    if len(args) != 2:
        raise ValueError("unexpected CreateTokenIfDyingSourceHadCounterEffect arguments: %s" % args)
    return args[1]


def convert(path):
    src = path.read_text()
    original = src
    name = path.stem

    if name == "OchreJelly" and "RegisterDelayedCreateTokenCopyOfDyingSourceEffect(2, 2)" in src:
        src = replace_exact(
            src,
            "        addTrigger(Triggers.dies(ObjectRelation.THIS),\n"
            "                new RegisterDelayedCreateTokenCopyOfDyingSourceEffect(2, 2));",
            "        addTrigger(Triggers.dies(new PermanentCounterCountAtLeastPredicate(CounterType.PLUS_ONE_PLUS_ONE, 2),\n"
            "                        ObjectRelation.THIS),\n"
            "                new RegisterDelayedCreateTokenCopyOfDyingSourceEffect(\n"
            "                        new Divided(new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE), 2)));",
            name)
    if name == "NineLivesFamiliar" and "WithOneFewerCounterEffect" in src:
        src = replace_exact(
            src,
            "        addTrigger(Triggers.dies(ObjectRelation.THIS),\n"
            "                new RegisterDelayedSelfReturnFromGraveyardWithOneFewerCounterEffect(CounterType.REVIVAL));",
            "        addTrigger(Triggers.dies(new PermanentHasCountersPredicate(CounterType.REVIVAL), ObjectRelation.THIS),\n"
            "                new RegisterDelayedSelfReturnFromGraveyardEffect(CounterType.REVIVAL,\n"
            "                        new Sum(new CountersOnTriggeringPermanent(CounterType.REVIVAL), new Fixed(-1))));",
            name)
    src = src.replace(
        "new PutCountersOnTargetForEachDyingSourcePowerEffect()",
        "new PutCounterOnTargetPermanentEffect(\n"
        "                                CounterType.PLUS_ONE_PLUS_ONE, new SourcePower(), null,\n"
        "                                new PermanentIsCreaturePredicate(), false, null, true)")
    if name == "ToxrillTheCorrosive" and "CreateTokenIfDyingSourceHadCounterEffect" in src:
        src = replace_exact(
            src, "addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),",
            "addTrigger(Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.SLIME),\n"
            "                        ObjectRelation.OPPONENT_CONTROLS),", name)
    if name == "VillagePillagers" and "CreateTokenIfDyingSourceHadCounterEffect" in src:
        src = replace_exact(
            src, "addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),",
            "addTrigger(Triggers.creatureDies(" + HAS_COUNTERS + ", ObjectRelation.OPPONENT_CONTROLS),", name)
    src = replace_calls(src, "CreateTokenIfDyingSourceHadCounterEffect", counter_token)
    if name == "AthreosShroudVeiled" and "WithCounterEffect" in src:
        src = replace_exact(
            src,
            "        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),\n"
            "                new ReturnTriggeringPermanentToBattlefieldWithCounterEffect(CounterType.COIN, Zone.GRAVEYARD));\n"
            "        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), Triggers.CREATURE, ObjectRelation.ANY),\n"
            "                new ReturnTriggeringPermanentToBattlefieldWithCounterEffect(CounterType.COIN, Zone.EXILE));",
            "        PermanentPredicate coinCounter = new PermanentHasCountersPredicate(CounterType.COIN);\n"
            "        addTrigger(Triggers.creatureDies(coinCounter, ObjectRelation.ANY),\n"
            "                new ReturnTriggeringPermanentToBattlefieldEffect(Zone.GRAVEYARD));\n"
            "        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD),\n"
            "                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, coinCounter)), ObjectRelation.ANY),\n"
            "                new ReturnTriggeringPermanentToBattlefieldEffect(Zone.EXILE));",
            name)
    src = re.sub(r"MoveDyingSourceCountersToTargetCreatureEffect\.alwaysTriggers\((true|false)\)",
                 r"new MoveDyingSourceCountersToTargetCreatureEffect(\1)", src)
    src = re.sub(r"new MoveDyingSourceCountersToTargetCreatureEffect\((true|false), false\)",
                 r"new MoveDyingSourceCountersToTargetCreatureEffect(\1)", src)
    if name == "IronApprentice" and HAS_COUNTERS not in src:
        src = replace_exact(
            src,
            "addTrigger(Triggers.dies(ObjectRelation.THIS), new MoveDyingSourceCountersToTargetCreatureEffect(true));",
            "addTrigger(Triggers.dies(" + HAS_COUNTERS + ", ObjectRelation.THIS),\n"
            "                new MoveDyingSourceCountersToTargetCreatureEffect(true));", name)
    if name == "ScoldingAdministrator" and HAS_COUNTERS not in src:
        src = replace_exact(
            src,
            "target(0, 1).addTrigger(Triggers.dies(ObjectRelation.THIS), new MoveDyingSourceCountersToTargetCreatureEffect());",
            "target(0, 1).addTrigger(Triggers.dies(" + HAS_COUNTERS + ", ObjectRelation.THIS),\n"
            "                new MoveDyingSourceCountersToTargetCreatureEffect());", name)
    src = src.replace("new GainLifeEqualToDyingSourcePowerEffect()", "new GainLifeEffect(new SourcePower())")
    src = src.replace("addTrigger(Triggers.dies(ObjectRelation.THIS), new TargetPlayerLosesLifeEqualToPowerEffect());",
                      "addTrigger(Triggers.dies(ObjectRelation.THIS),\n"
                      "                new LoseLifeEffect(new SourcePower(), LoseLifeRecipient.TARGET_PLAYER));")
    src = src.replace("new TargetPlayerLosesLifeEqualToPowerEffect()",
                      "new LoseLifeEffect(new SourcePower(), LoseLifeRecipient.TARGET_PLAYER)")
    if name == "ToxrillTheCorrosive":
        src = src.replace("                new CreateTokenEffect(\"Slug\", 1, 1, CardColor.BLACK,\n"
                          "                                List.of(CardSubtype.SLUG), Set.of(), Set.of()));",
                          "                new CreateTokenEffect(\"Slug\", 1, 1, CardColor.BLACK,\n"
                          "                        List.of(CardSubtype.SLUG), Set.of(), Set.of()));")
    if name == "VillagePillagers":
        src = src.replace("addTrigger(Triggers.creatureDies(" + HAS_COUNTERS + ", ObjectRelation.OPPONENT_CONTROLS),",
                          "addTrigger(Triggers.creatureDies(" + HAS_COUNTERS + ",\n"
                          "                        ObjectRelation.OPPONENT_CONTROLS),")
    if name == "MortisDogs":
        src = src.replace("addTrigger(Triggers.dies(ObjectRelation.THIS), new LoseLifeEffect(new SourcePower(), "
                          "LoseLifeRecipient.TARGET_PLAYER));",
                          "addTrigger(Triggers.dies(ObjectRelation.THIS),\n"
                          "                new LoseLifeEffect(new SourcePower(), LoseLifeRecipient.TARGET_PLAYER));")
    if name == "ReveredUnicorn":
        src = src.replace("new GainLifeEffect(new CountersOnSource(CounterType.AGE))",
                          "new GainLifeEffect(new CountersOnTriggeringPermanent(CounterType.AGE))")
        src = src.replace("        // counters on it. The count is frozen from the leaving permanent by the self-leaves\n"
                          "        // collector, since the source is gone by resolution time.\n",
                          "        // counters on it: the leaving permanent's, as it last existed on the battlefield.\n")

    if src == original:
        return False
    src = set_imports(
        src,
        add=[EFFECT + "RegisterDelayedSelfReturnFromGraveyardEffect", EFFECT + "PutCounterOnTargetPermanentEffect",
             EFFECT + "ReturnTriggeringPermanentToBattlefieldEffect", EFFECT + "GainLifeEffect",
             EFFECT + "LoseLifeEffect", EFFECT + "LoseLifeRecipient",
             AMOUNT + "CountersOnTriggeringPermanent", AMOUNT + "Divided", AMOUNT + "Fixed", AMOUNT + "Sum",
             AMOUNT + "SourcePower",
             FILTER + "PermanentAllOfPredicate", FILTER + "PermanentCounterCountAtLeastPredicate",
             FILTER + "PermanentHasCountersPredicate", FILTER + "PermanentIsCreaturePredicate",
             FILTER + "PermanentPredicate", PKG + ".model.CounterType"],
        remove=[EFFECT + "RegisterDelayedSelfReturnFromGraveyardWithOneFewerCounterEffect",
                EFFECT + "PutCountersOnTargetForEachDyingSourcePowerEffect",
                EFFECT + "CreateTokenIfDyingSourceHadCounterEffect",
                EFFECT + "ReturnTriggeringPermanentToBattlefieldWithCounterEffect",
                EFFECT + "GainLifeEqualToDyingSourcePowerEffect",
                EFFECT + "TargetPlayerLosesLifeEqualToPowerEffect",
                AMOUNT + "CountersOnSource"])
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
