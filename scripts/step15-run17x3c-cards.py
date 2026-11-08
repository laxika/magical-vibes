#!/usr/bin/env python3
"""Step 15, run 17x3c: the enter family's last "it" bindings read the stack entry's triggering permanent.

Rewrites the card classes whose enter ability reached the stack only because the enter collector bound the entering
permanent as the entry's target (or a class-keyed collector handler did), so each card writes the ability that goes on
the stack and "it" / "that creature" is read from the entry's triggering permanent as the ability resolves:

- Graft (CR 702.58a, 13 cards): "Whenever another creature enters, if this permanent has a +1/+1 counter on it, you
  may move a +1/+1 counter from this permanent onto that creature" is the intervening "if"
  `SourceCounterThreshold(1, PLUS_ONE_PLUS_ONE)` and a `MayEffect` around
  `MoveCounterFromSourceToEnteringCreatureEffect`, asked as the ability resolves (CR 603.5). Agent's Toolkit's
  "you may move a counter" a `MayEffect` around `MoveChosenCounterFromSourceToEnteringCreatureEffect`.
- Soulbond's other half (CR 702.95a, 25 cards): "Whenever another creature you control enters, if you control both
  that creature and this one and both are unpaired, you may pair that creature with this creature" is the intervening
  "if" `AllConditions(NotCondition(SourceIsPaired), TriggeringPermanentMatches(controlled and not
  PermanentIsPairedPredicate))` and a `MayEffect` around `SoulbondPairWithEnteringEffect`.
- "It connives": `DrawDiscardAndConniveEffect.ofTriggeringPermanent()` (Baron Strucker, Swordsman).
- Belligerent Yearling: "that creature's power" `TriggeringPermanentPower(true)` (a negative power kept).
- Kolodin: "it becomes saddled" `BecomeSaddledUntilEndOfTurnEffect(GrantScope.TRIGGERING_PERMANENT)`.
- Aether Flash: "deals 2 damage to it" `DealDamageToTriggeringPermanentEffect(2)`; the attack-trigger cards'
  `DealDamageToTriggeringAttackerEffect` is the same class, renamed (Raking Canopy, Circle of Flame, Barbed Foliage,
  Palazzo Archers, Storm, Queen of Wakanda).
- Marauding Raptor: `DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect` (renamed, no target).
- Tribute to the World Tree, Shelinda: the branch's "put +1/+1 counters on it / that creature"
  `PutCounterOnTargetPermanentEffect.onTriggeringPermanent`.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17x3c-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

GRAFT_CARDS = ["VigeanHydropon", "VigeanGraftmage", "SporebackTroll", "SimicBasilisk", "PlaxcasterFrogling",
               "NovijenSages", "LlanowarReborn", "HeliumSquirter", "CytoplastRootKin", "CytospawnShambler",
               "CytoplastManipulator", "AquastrandSpider", "SimicInitiate"]

SOULBOND_CARDS = ["BreathkeeperSeraph", "DruidsFamiliar", "DoomWeaver", "DonnaNoble", "DiregrafEscort",
                  "DeadeyeNavigator", "ElgaudShieldmate", "GeistTrappers", "GalvanicAlchemist", "HanweirLancer",
                  "ImperiousMindbreaker", "LightningMauler", "MiragePhalanx", "NightshadePeddler",
                  "NearheathPilgrim", "PathbreakerWurm", "Stonewright", "SternMentor", "SpectralGateguards",
                  "SilverbladePaladin", "TrustedForcemage", "ThunderingMightmare", "TandemLookout",
                  "WolfirSilverheart", "Wingcrafter"]

GRAFT_OLD = ("        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),\n"
             "                new MoveCounterFromSourceToEnteringCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE));")
GRAFT_NEW = ("        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),\n"
             "                TriggerOptions.DEFAULT.withInterveningIf(\n"
             "                        new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),\n"
             "                new MayEffect(new MoveCounterFromSourceToEnteringCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE),\n"
             "                        \"Move a +1/+1 counter onto that creature?\"));")
GRAFT_IMPORTS = ["model.trigger.TriggerOptions", "model.condition.SourceCounterThreshold", "model.effect.MayEffect"]

SOULBOND_OLD = ("        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),\n"
                "                new SoulbondPairWithEnteringEffect());")
SOULBOND_IMPORTS = ["model.trigger.TriggerOptions", "model.condition.AllConditions", "model.condition.NotCondition",
                    "model.condition.SourceIsPaired", "model.condition.TriggeringPermanentMatches",
                    "model.filter.PermanentAllOfPredicate",
                    "model.filter.PermanentControlledBySourceControllerPredicate",
                    "model.filter.PermanentIsPairedPredicate", "model.filter.PermanentNotPredicate",
                    "model.effect.MayEffect"]


def soulbond_new(name):
    return ("        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),\n"
            "                TriggerOptions.DEFAULT.withInterveningIf(new AllConditions(List.of(\n"
            "                        new NotCondition(new SourceIsPaired()),\n"
            "                        new TriggeringPermanentMatches(new PermanentAllOfPredicate(List.of(\n"
            "                                new PermanentControlledBySourceControllerPredicate(),\n"
            "                                new PermanentNotPredicate(new PermanentIsPairedPredicate()))))))),\n"
            "                new MayEffect(new SoulbondPairWithEnteringEffect(),\n"
            f"                        \"Pair {name} with that creature?\"));")


ATTACKER = ("new DealDamageToTriggeringAttackerEffect(", "new DealDamageToTriggeringPermanentEffect(")
ON_IT = "PutCounterOnTargetPermanentEffect.onTriggeringPermanent"

# card -> (edits, imports to add); "edits" may be a callable taking the file text
CARDS = {
    "AgentsToolkit": ([(
        "                new MoveChosenCounterFromSourceToEnteringCreatureEffect(java.util.Arrays.stream(CounterType.values())\n"
        "                        .filter(type -> type != CounterType.ANY && type != CounterType.SILVER).toList()));",
        "                new MayEffect(new MoveChosenCounterFromSourceToEnteringCreatureEffect(\n"
        "                        java.util.Arrays.stream(CounterType.values())\n"
        "                                .filter(type -> type != CounterType.ANY && type != CounterType.SILVER).toList()),\n"
        "                        \"Move a counter from this artifact onto that creature?\"));")],
        ["model.effect.MayEffect"]),
    "BaronStruckerHYDRAOverlord": ([("DrawDiscardAndConniveEffect.forEnteringPermanent()",
                                     "DrawDiscardAndConniveEffect.ofTriggeringPermanent()")], []),
    "SwordsmanSharpScoundrel": ([("DrawDiscardAndConniveEffect.forEnteringPermanent()",
                                  "DrawDiscardAndConniveEffect.ofTriggeringPermanent()")], []),
    "BelligerentYearling": ([("new SetSelfBasePowerToAmountUntilEndOfTurnEffect(new TargetPower(true))",
                              "new SetSelfBasePowerToAmountUntilEndOfTurnEffect(new TriggeringPermanentPower(true))")],
                            ["model.amount.TriggeringPermanentPower"]),
    "KolodinTriumphCaster": ([("new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)",
                               "new BecomeSaddledUntilEndOfTurnEffect(GrantScope.TRIGGERING_PERMANENT)")], []),
    "AetherFlash": ([("        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),\n"
                      "                new DealDamageToTargetCreatureEffect(2));",
                      "        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),\n"
                      "                new DealDamageToTriggeringPermanentEffect(2));")],
                    ["model.effect.DealDamageToTriggeringPermanentEffect"]),
    "MaraudingRaptor": ([("new DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect(",
                          "new DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect(")],
                        ["model.effect.DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect"]),
    "TributeToTheWorldTree": ([("new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)",
                                f"{ON_IT}(CounterType.PLUS_ONE_PLUS_ONE, 2)")], []),
    "ShelindaYevonAcolyte": ([("new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)",
                               f"{ON_IT}(CounterType.PLUS_ONE_PLUS_ONE, 1)")], []),
}
for attack_card in ["RakingCanopy", "CircleOfFlame", "BarbedFoliage", "PalazzoArchers", "StormQueenOfWakanda"]:
    CARDS[attack_card] = ([ATTACKER], ["model.effect.DealDamageToTriggeringPermanentEffect"])
for graft_card in GRAFT_CARDS:
    CARDS[graft_card] = ([(GRAFT_OLD, GRAFT_NEW)], GRAFT_IMPORTS)
for soulbond_card in SOULBOND_CARDS:
    CARDS[soulbond_card] = ("soulbond", SOULBOND_IMPORTS)


def imp(cls):
    return f"import {PKG}.{cls};"


def remove_unused_imports(text):
    lines = text.split("\n")
    body = "\n".join(l for l in lines if not l.startswith("import "))
    kept = []
    for line in lines:
        match = re.match(r"import ([\w.]+)\.(\w+);$", line)
        if match and not re.search(r"\b" + match.group(2) + r"\b", body):
            continue
        kept.append(line)
    text = "\n".join(kept)
    return re.sub(r"\n\n\n+", "\n\n", text)


def add_imports(text, add):
    lines = text.split("\n")
    for cls in add:
        line = imp(cls)
        if line in lines:
            continue
        imports = [i for i, l in enumerate(lines) if l.startswith("import ") and not l.startswith("import java.")]
        position = next((i for i in imports if lines[i] > line), imports[-1] + 1)
        lines.insert(position, line)
    return "\n".join(lines)


def add_java_list(text):
    if "import java.util.List;" in text:
        return text
    lines = text.split("\n")
    java = [i for i, l in enumerate(lines) if l.startswith("import java.")]
    if java:
        position = next((i for i in java if lines[i] > "import java.util.List;"), java[-1] + 1)
        lines.insert(position, "import java.util.List;")
    else:
        last = max(i for i, l in enumerate(lines) if l.startswith("import "))
        lines[last + 1:last + 1] = ["", "import java.util.List;"]
    return "\n".join(lines)


def find_card(name, modules):
    for module in modules:
        base = ROOT / f"magical-vibes-card/{module}/src/main/java/com/github/laxika/magicalvibes/cards"
        for path in base.glob(f"*/{name}.java"):
            return path
    return None


def convert(name, path, edits, add):
    text = path.read_text()
    original = text
    if edits == "soulbond":
        match = re.search(r'"Pair (.+?) with another unpaired creature you control\?"', text)
        if not match:
            raise SystemExit(f"{name}: soulbond prompt not found")
        edits = [(SOULBOND_OLD, soulbond_new(match.group(1)))]
    for old, new in edits:
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
        text = text.replace(old, new)
    if text == original:
        return
    text = add_imports(text, add)
    if "List.of(" in text:
        text = add_java_list(text)
    text = remove_unused_imports(text)
    path.write_text(text)
    print(f"converted {name}")


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz") | {"shared"}) if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for name, (edits, add) in CARDS.items():
        path = find_card(name, modules)
        if path is not None:
            convert(name, path, edits, add)


if __name__ == "__main__":
    main()
