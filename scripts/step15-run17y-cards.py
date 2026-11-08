#!/usr/bin/env python3
"""Step 15, run 17y: the "deals damage to a creature" card classes write their trigger conditions as `Triggers` filters.

The damaged-creature collector judged trigger-condition records by class: "this creature" / "equipped creature" / "a
creature" (whoever controls it), the dealer's and the damaged creature's filters and "combat damage only" were fields
of `TapAndSkipUntapDamagedCreatureEffect`, `DestroyDamagedCreatureAtEndOfCombatEffect`,
`EquipmentTapsAndLocksDamagedCreatureEffect`, `DamageDamagedCreatureControllerAndSelfEffect`,
`DestroyCreatureDamagedByMatchingCreatureEffect`, the `DamagedCreatureTriggerEffect` records (`DestroyDamagedCreatureEffect`,
`ExileDamagedCreatureEffect`, `CreateTokenWhenEquippedCreatureDealsCombatDamageToCreatureEffect`), Flayed Nim's form of
`ReflectAllyDamageToDamagedCreatureControllerEffect` and `EquipmentDamagesOtherDefendingCreaturesEffect`, and the
collector built the effect that went on the stack. Each card now writes the trigger with its relation
(`Triggers.dealsDamageToCreature()`, `equippedCreatureDealsDamageToCreature()`, `creatureDealsDamageToCreature()`,
`creatureYouControlDealsDamageToCreature()`) and filters (`withFilter` on the dealer, `withDamagedFilter`,
`withCombatDamageOnly()`), and the existing effects that read the stack entry's triggering permanent (the creature
dealt the damage) and event value (the damage).

The cards whose "this creature" ability was written as "a creature you control" and judged by the effect's class
(Strax, Spiritmonger, Mephidross Vampire's grant, Flesh Reaver) write `Triggers.dealsDamageToCreature()`, Quest for the
Gemblades its "combat damage" filter, Greatbow Doyen its Archer filter on the trigger.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17y-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

ALLY = "Triggers.creatureYouControlDealsDamageToCreature()"
SELF = "Triggers.dealsDamageToCreature()"
EQUIPPED = "Triggers.equippedCreatureDealsDamageToCreature()"
ANY = "Triggers.creatureDealsDamageToCreature()"
DESTROY = "new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)"
DESTROY_NO_REGEN = "new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true)"
DESTROY_EOC = "DestroyTargetPermanentAtEndOfCombatEffect.ofTriggeringPermanent()"
EXILE = "ExileTargetPermanentEffect.ofTriggeringPermanent()"
E = "model.effect."
F = "model.filter."
DESTROY_IMPORTS = [E + "DestroyReferencedPermanentEffect", E + "PermanentReference"]
EOC_IMPORTS = [E + "DestroyTargetPermanentAtEndOfCombatEffect"]
EOC_OLD = E + "DestroyDamagedCreatureAtEndOfCombatEffect"
SOURCE_PRED = F + "PermanentIsSourcePermanentPredicate"


def basilisk(old_effect):
    return ([(f'''        addTrigger({ALLY},
                {old_effect});''',
              f'''        addTrigger({SELF}.withCombatDamageOnly(),
                {DESTROY_EOC});''')],
            EOC_IMPORTS, [EOC_OLD, SOURCE_PRED])


def slayer(subtype):
    return ([(f'''        addTrigger({ALLY},
                new DestroyDamagedCreatureEffect(new PermanentHasSubtypePredicate(CardSubtype.{subtype})));''',
              f'''        addTrigger({SELF}.withDamagedFilter(new PermanentHasSubtypePredicate(CardSubtype.{subtype})),
                {DESTROY});''')],
            DESTROY_IMPORTS, [E + "DestroyDamagedCreatureEffect"])


COMBAT_DESTROY_NO_REGEN = ([(f'''        addTrigger({ALLY},
                new DestroyDamagedCreatureEffect(true, true));''',
                             f'''        addTrigger({SELF}.withCombatDamageOnly(),
                {DESTROY_NO_REGEN});''')],
                           DESTROY_IMPORTS, [E + "DestroyDamagedCreatureEffect"])

PUT_COUNTER = ([(f'''        addTrigger({ALLY},
                new PutCountersOnSourceEffect(1, 1, 1));''',
                 f'''        addTrigger({SELF},
                new PutCountersOnSourceEffect(1, 1, 1));''')], [], [])

CARDS = {
    "MatsuTribeSniper": (
        [(f'''        addTrigger({ALLY},
                new TapAndSkipUntapDamagedCreatureEffect());''',
          f'''        addTrigger({SELF},
                new TapPermanentsEffect(TapUntapScope.TRIGGERING),
                new SkipNextUntapEffect(TapUntapScope.TRIGGERING));''')],
        [E + "TapPermanentsEffect", E + "SkipNextUntapEffect", E + "TapUntapScope"],
        [E + "TapAndSkipUntapDamagedCreatureEffect"]),
    "StoneTongueBasilisk": basilisk("new DestroyDamagedCreatureAtEndOfCombatEffect(new PermanentIsSourcePermanentPredicate())"),
    "SerpentineBasilisk": basilisk("new DestroyDamagedCreatureAtEndOfCombatEffect(new PermanentIsSourcePermanentPredicate())"),
    "OhranViper": basilisk("new DestroyDamagedCreatureAtEndOfCombatEffect(new PermanentIsSourcePermanentPredicate())"),
    "LowlandBasilisk": (
        [(f'''        addTrigger({ALLY},
                DestroyDamagedCreatureAtEndOfCombatEffect.thisCreature());''',
          f'''        addTrigger({SELF},
                {DESTROY_EOC});''')],
        EOC_IMPORTS, [EOC_OLD]),
    "SimicBasilisk": (
        [(f'''                        new TriggeredAbility({ALLY},
                        new DestroyDamagedCreatureAtEndOfCombatEffect(
                                new PermanentIsSourcePermanentPredicate())))),''',
          f'''                        new TriggeredAbility({SELF}.withCombatDamageOnly(),
                        {DESTROY_EOC}))),''')],
        EOC_IMPORTS, [EOC_OLD, SOURCE_PRED]),
    "SosukeSonOfSeshiro": (
        [(f'''        addTrigger({ALLY},
                new DestroyDamagedCreatureAtEndOfCombatEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)));''',
          f'''        addTrigger({ALLY}
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.WARRIOR))
                        .withCombatDamageOnly(),
                {DESTROY_EOC});''')],
        EOC_IMPORTS, [EOC_OLD]),
    "NekoTe": (
        [(f'''        addTrigger({ALLY},
                new EquipmentTapsAndLocksDamagedCreatureEffect());''',
          f'''        addTrigger({EQUIPPED},
                new TapPermanentsEffect(TapUntapScope.TRIGGERING),
                DoesntUntapEffect.triggeringWhileSourceOnBattlefield());''')],
        [E + "TapPermanentsEffect", E + "DoesntUntapEffect", E + "TapUntapScope"],
        [E + "EquipmentTapsAndLocksDamagedCreatureEffect"]),
    "BellowingFiend": (
        [(f'''        addTrigger({ALLY},
                new DamageDamagedCreatureControllerAndSelfEffect(3, 3));''',
          f'''        addTrigger({SELF},
                new DealDamageToPlayersEffect(3, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER),
                new DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER));''')],
        [E + "DealDamageToPlayersEffect", E + "DamageRecipient"],
        [E + "DamageDamagedCreatureControllerAndSelfEffect"]),
    "ToxinSliver": (
        [(f'''        addTrigger({ALLY},
                new DestroyCreatureDamagedByMatchingCreatureEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.SLIVER), true, true));''',
          f'''        addTrigger({ANY}
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.SLIVER))
                        .withCombatDamageOnly(),
                {DESTROY_NO_REGEN});''')],
        DESTROY_IMPORTS, [E + "DestroyCreatureDamagedByMatchingCreatureEffect"]),
    "VampireSlayer": slayer("VAMPIRE"),
    "SpiderSlayerHatredHoned": slayer("SPIDER"),
    "DinosaurHunter": slayer("DINOSAUR"),
    "GrotesqueHybrid": COMBAT_DESTROY_NO_REGEN,
    "DrippingDead": COMBAT_DESTROY_NO_REGEN,
    "CruelDeceiver": (
        [(f'''                                        new TriggeredAbility({ALLY},
                                        new DestroyDamagedCreatureEffect())))),''',
          f'''                                        new TriggeredAbility({SELF},
                                        {DESTROY})))),''')],
        DESTROY_IMPORTS, [E + "DestroyDamagedCreatureEffect"]),
    "PitSpawn": (
        [(f'''        addTrigger({ALLY},
                new ExileDamagedCreatureEffect());''',
          f'''        addTrigger({SELF},
                {EXILE});''')],
        [E + "ExileTargetPermanentEffect"], [E + "ExileDamagedCreatureEffect"]),
    "SwordOfKaldra": (
        [(f'''        addTrigger({ALLY},
                new ExileDamagedCreatureEffect(true));''',
          f'''        addTrigger({EQUIPPED},
                {EXILE});''')],
        [E + "ExileTargetPermanentEffect"], [E + "ExileDamagedCreatureEffect"]),
    "GiantsSkewer": (
        [(f'''        addTrigger({ALLY},
                new CreateTokenWhenEquippedCreatureDealsCombatDamageToCreatureEffect(foodToken()));''',
          f'''        addTrigger({EQUIPPED}.withCombatDamageOnly(),
                foodToken());''')],
        [], [E + "CreateTokenWhenEquippedCreatureDealsCombatDamageToCreatureEffect"]),
    "FlayedNim": (
        [(f'''        addTrigger({ALLY},
                ReflectAllyDamageToDamagedCreatureControllerEffect.selfCombatDamageCausesLifeLoss());''',
          f'''        addTrigger({SELF}.withCombatDamageOnly(),
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));''')],
        [E + "LoseLifeEffect", E + "LoseLifeRecipient", "model.amount.EventValue"],
        [E + "ReflectAllyDamageToDamagedCreatureControllerEffect"]),
    "GreatbowDoyen": (
        [(f'''        addTrigger({ALLY},
                new ReflectAllyDamageToDamagedCreatureControllerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.ARCHER)));''',
          f'''        addTrigger({ALLY}.withFilter(new PermanentHasSubtypePredicate(CardSubtype.ARCHER)),
                new ReflectAllyDamageToDamagedCreatureControllerEffect());''')],
        [], []),
    "KusariGama": (
        [(f'''        addTrigger({ALLY},
                new EquipmentDamagesOtherDefendingCreaturesEffect());''',
          f'''        addTrigger({EQUIPPED}.withDamagedFilter(new PermanentIsBlockingPredicate()),
                new DealDamageToEachMatchingPermanentEffect(new EventValue(),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsTriggeringPermanentPredicate()),
                                new PermanentControlledByDefendingPlayerPredicate())),
                        EachPermanentScope.ALL_PLAYERS));''')],
        [E + "DealDamageToEachMatchingPermanentEffect", E + "EachPermanentScope", "model.amount.EventValue",
         F + "PermanentAllOfPredicate", F + "PermanentIsCreaturePredicate", F + "PermanentNotPredicate",
         F + "PermanentIsTriggeringPermanentPredicate", F + "PermanentControlledByDefendingPlayerPredicate",
         F + "PermanentIsBlockingPredicate"],
        [E + "EquipmentDamagesOtherDefendingCreaturesEffect"]),
    "StraxSontaranNurse": PUT_COUNTER,
    "Spiritmonger": PUT_COUNTER,
    "MephidrossVampire": (
        [(f'''                new TriggeredAbility({ALLY},
                new PutCountersOnSourceEffect(1, 1, 1)),''',
          f'''                new TriggeredAbility({SELF},
                new PutCountersOnSourceEffect(1, 1, 1)),''')], [], []),
    "FleshReaver": (
        [(f'''                        {ALLY},
                        Triggers.sourceYouControlDealsDamageToOpponent()),''',
          f'''                        {SELF},
                        Triggers.sourceYouControlDealsDamageToOpponent()),''')], [], []),
    "QuestForTheGemblades": (
        [(f'''        addTrigger({ALLY},
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),''',
          f'''        addTrigger({ALLY}.withCombatDamageOnly(),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),''')], [], []),
}

GONE = ["TapAndSkipUntapDamagedCreatureEffect", "DestroyDamagedCreatureAtEndOfCombatEffect",
        "EquipmentTapsAndLocksDamagedCreatureEffect", "DamageDamagedCreatureControllerAndSelfEffect",
        "DestroyCreatureDamagedByMatchingCreatureEffect", "DestroyDamagedCreatureEffect", "ExileDamagedCreatureEffect",
        "CreateTokenWhenEquippedCreatureDealsCombatDamageToCreatureEffect",
        "EquipmentDamagesOtherDefendingCreaturesEffect", "selfCombatDamageCausesLifeLoss"]


def import_line(fqcn):
    return f"import {fqcn[5:]};" if fqcn.startswith("java:") else f"import {PKG}.{fqcn};"


def add_import(text, fqcn):
    line = import_line(fqcn)
    if line in text:
        return text
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    pos = imports[0].start()
    return text[:pos] + line + "\n" + text[pos:]


def remove_unused_import(text, fqcn):
    line = import_line(fqcn) + "\n"
    simple = line.rsplit(".", 1)[1].rstrip(";\n")
    if line in text and not re.search(r"\b" + simple + r"\b", text.replace(line, "")):
        text = text.replace(line, "")
    return text


def sort_imports(text):
    """Keeps the project's import block sorted: the package's imports first, then java.*."""
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    if not imports:
        return text
    start, end = imports[0].start(), imports[-1].end()
    block = text[start:end]
    if re.search(r"^(?!import |\s*$).+", block, re.M):
        return text
    lines = [m.group(0) for m in imports]
    ours = sorted(l for l in lines if not l.startswith("import java"))
    java = sorted(l for l in lines if l.startswith("import java"))
    joined = "\n".join(ours) + ("\n\n" + "\n".join(java) if java and ours else "\n".join(java))
    return text[:start] + joined + text[end:]


def convert_file(path, name):
    if name not in CARDS:
        return
    text = path.read_text()
    original = text
    edits, imports, removed = CARDS[name]
    for old, new in edits:
        if old in text:
            text = text.replace(old, new)
        elif new not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
    if text == original:
        return
    for fqcn in imports:
        text = add_import(text, fqcn)
    for fqcn in removed:
        text = remove_unused_import(text, fqcn)
    text = sort_imports(text)
    for gone in GONE:
        if re.search(r"\b" + gone + r"\b", text):
            raise SystemExit(f"{name}: still names {gone}")
    path.write_text(text)
    print(f"converted {name}")


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz")) + ["shared"] if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for module in modules:
        root = ROOT / f"magical-vibes-card/{module}/src/main/java"
        if not root.exists():
            continue
        for path in sorted(root.rglob("*.java")):
            convert_file(path, path.stem)


if __name__ == "__main__":
    main()
