#!/usr/bin/env python3
"""Step 15, run 17w4: the copy-for-each cast triggers read the stack entry's triggering spell.

Rewrites the card classes whose cast trigger was a copy record the spell-cast collector judged by class and rebuilt with
the spell's snapshot, caster and original target bound in: each becomes a filtered cast trigger whose target conditions
are the trigger's filters (an instant or sorcery, "targets only this creature" / "targets only a single Golem" /
"targets only a single nonland permanent an opponent controls", "a single target"), whose intervening "if"s ("if you
control one or more other creatures that spell could target", "if another opponent controls ...") are the new
`StackEntryCouldTargetAnotherPermanentPredicate`, and whose copy effects (which lost their bound components) read the
triggering spell from the stack entry. Hive Mind's and Curse of Echoes' spell filters move from the effect to the
trigger. Idempotent: a file already converted is left alone. Per letter module.

Usage: step15-run17w4-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

INSTANT_OR_SORCERY = """.withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))"""
TARGETS_ONLY_THIS = """new StackEntryTargetsOnlySinglePermanentOrPlayerPredicate(),
                                new StackEntryTargetsSourcePredicate()"""
INSTANT_OR_SORCERY_IMPORTS = ["model.CardType", "model.filter.CardAnyOfPredicate", "model.filter.CardTypePredicate",
                              "java:java.util.List"]
TARGETS_ONLY_THIS_IMPORTS = ["model.filter.StackEntryAllOfPredicate",
                             "model.filter.StackEntryTargetsOnlySinglePermanentOrPlayerPredicate",
                             "model.filter.StackEntryTargetsSourcePredicate"]

# card -> (letter, [(old, new)], imports to add, imports to remove)
CARDS = {
    "ZadaHedronGrinder": ("z", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new CopySpellForEachOtherControlledCreatureEffect());""",
         f"""        addTrigger(Triggers.youCastSpell()
                        {INSTANT_OR_SORCERY}
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                {TARGETS_ONLY_THIS}))),
                new CopySpellForEachOtherControlledCreatureEffect());"""),
    ], INSTANT_OR_SORCERY_IMPORTS + TARGETS_ONLY_THIS_IMPORTS, []),
    "MirrorwingDragon": ("m", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CopySpellForEachOtherControlledCreatureEffect());""",
         f"""        addTrigger(Triggers.playerCastsSpell()
                        {INSTANT_OR_SORCERY}
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                {TARGETS_ONLY_THIS}))),
                new CopySpellForEachOtherControlledCreatureEffect());"""),
    ], INSTANT_OR_SORCERY_IMPORTS + TARGETS_ONLY_THIS_IMPORTS, []),
    "BeamsplitterMage": ("b", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new CopySpellForEachOtherControlledCreatureEffect(true));""",
         f"""        // The intervening "if" ("if you control one or more other creatures that spell could target") is the last
        // stack-entry filter; the copy effect makes no copy when no such creature is left as it resolves.
        addTrigger(Triggers.youCastSpell()
                        {INSTANT_OR_SORCERY}
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                {TARGETS_ONLY_THIS},
                                new StackEntryCouldTargetAnotherPermanentPredicate(new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentControlledBySourceControllerPredicate())), false)))),
                new CopySpellForEachOtherControlledCreatureEffect(true));"""),
    ], INSTANT_OR_SORCERY_IMPORTS + TARGETS_ONLY_THIS_IMPORTS + [
        "model.filter.PermanentAllOfPredicate", "model.filter.PermanentControlledBySourceControllerPredicate",
        "model.filter.PermanentIsCreaturePredicate", "model.filter.StackEntryCouldTargetAnotherPermanentPredicate"],
        []),
    "InkTreaderNephilim": ("i", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CopySpellForEachOtherCreatureEffect());""",
         f"""        // The intervening "if" ("if that spell targets only this creature") is the stack-entry filter; the copy effect
        // checks it again as it resolves.
        addTrigger(Triggers.playerCastsSpell()
                        {INSTANT_OR_SORCERY}
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                {TARGETS_ONLY_THIS}))),
                new CopySpellForEachOtherCreatureEffect());"""),
    ], INSTANT_OR_SORCERY_IMPORTS + TARGETS_ONLY_THIS_IMPORTS, []),
    "FeatherRadiantArbiter": ("f", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new CopySpellForEachOtherCreatureWithManaEffect("{2}"));""",
         f"""        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                {TARGETS_ONLY_THIS}))),
                new CopySpellForEachOtherCreatureWithManaEffect("{{2}}"));"""),
    ], ["model.CardType", "model.filter.CardNotPredicate", "model.filter.CardTypePredicate", "java:java.util.List"]
        + TARGETS_ONLY_THIS_IMPORTS, []),
    "PrecursorGolem": ("p", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM));""",
         f"""        addTrigger(Triggers.playerCastsSpell()
                        {INSTANT_OR_SORCERY}
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryTargetsOnlySinglePermanentOrPlayerPredicate(),
                                new StackEntryTargetsPermanentPredicate(
                                        new PermanentHasSubtypePredicate(CardSubtype.GOLEM))))),
                new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM));"""),
    ], INSTANT_OR_SORCERY_IMPORTS + ["model.filter.PermanentHasSubtypePredicate",
                                     "model.filter.StackEntryAllOfPredicate",
                                     "model.filter.StackEntryTargetsOnlySinglePermanentOrPlayerPredicate",
                                     "model.filter.StackEntryTargetsPermanentPredicate"], []),
    "ExterminatorMagmarch": ("e", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new CopySpellForAnotherOpponentPermanentEffect());""",
         f"""        PermanentPredicate nonlandPermanentAnOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        // The intervening "if" ("if another opponent controls one or more nonland permanents that spell could target")
        // is the last stack-entry filter; the copy effect makes no copy when no such permanent is left as it resolves.
        addTrigger(Triggers.youCastSpell()
                        {INSTANT_OR_SORCERY}
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryTargetsOnlySinglePermanentOrPlayerPredicate(),
                                new StackEntryTargetsPermanentPredicate(nonlandPermanentAnOpponentControls),
                                new StackEntryCouldTargetAnotherPermanentPredicate(
                                        nonlandPermanentAnOpponentControls, true)))),
                new CopySpellForAnotherOpponentPermanentEffect());"""),
    ], ["model.CardType", "model.filter.CardAnyOfPredicate", "model.filter.CardTypePredicate",
        "model.filter.PermanentAllOfPredicate", "model.filter.PermanentControlledBySourceControllerPredicate",
        "model.filter.PermanentIsLandPredicate", "model.filter.PermanentNotPredicate",
        "model.filter.PermanentPredicate", "model.filter.StackEntryAllOfPredicate",
        "model.filter.StackEntryCouldTargetAnotherPermanentPredicate",
        "model.filter.StackEntryTargetsOnlySinglePermanentOrPlayerPredicate",
        "model.filter.StackEntryTargetsPermanentPredicate"], []),
    "HiveMind": ("h", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CopySpellForEachOtherPlayerEffect(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL))));""",
         """        addTrigger(Triggers.playerCastsSpell().withStackEntryFilter(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL))),
                new CopySpellForEachOtherPlayerEffect());"""),
    ], [], []),
    "CurseOfEchoes": ("c", [
        ("""        // Reuses Hive Mind's Triggers.playerCastsSpell() copy machinery; the filter restricts
        // firing to instant/sorcery spells cast by the enchanted player.
        addTrigger(Triggers.playerCastsSpell(),
                new CopySpellForEachOtherPlayerEffect(true,
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                                new StackEntryControlledByEnchantedPlayerPredicate()))));""",
         """        addTrigger(Triggers.playerCastsSpell().withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        new StackEntryControlledByEnchantedPlayerPredicate()))),
                new CopySpellForEachOtherPlayerEffect(true));"""),
    ], [], []),
    "GlimmervoidBasin": ("g", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CopySpellForEachOtherPermanentOrPlayerEffect());""",
         f"""        addTrigger(Triggers.playerCastsSpell()
                        {INSTANT_OR_SORCERY}
                        .withStackEntryFilter(new StackEntryHasOneTargetPredicate()),
                new CopySpellForEachOtherPermanentOrPlayerEffect());"""),
    ], INSTANT_OR_SORCERY_IMPORTS + ["model.filter.StackEntryHasOneTargetPredicate"], []),
}


def imp(cls):
    return f"import {PKG}.{cls};\n"


def import_line(name):
    return f"import {name[5:]};\n" if name.startswith("java:") else imp(name)


def convert(name, letter, edits, add, remove):
    path = ROOT / f"magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
    text = path.read_text()
    original = text
    for old, new in edits:
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
        text = text.replace(old, new)
    for cls in remove:
        text = text.replace(import_line(cls), "")
    lines = text.split("\n")
    for cls in add:
        line = import_line(cls).rstrip("\n")
        if line in lines:
            continue
        imports = [i for i, l in enumerate(lines) if l.startswith("import ")]
        same_kind = [i for i in imports if lines[i].startswith("import java.") == line.startswith("import java.")]
        if same_kind:
            position = next((i for i in same_kind if lines[i] > line), same_kind[-1] + 1)
        else:
            position = imports[-1] + 1
            lines.insert(position, "")
            position += 1
        lines.insert(position, line)
    text = "\n".join(lines)
    if text != original:
        path.write_text(text)
        print(f"converted {name}")


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    for name, (letter, edits, add, remove) in CARDS.items():
        if letter in letters:
            convert(name, letter, edits, add, remove)


if __name__ == "__main__":
    main()
