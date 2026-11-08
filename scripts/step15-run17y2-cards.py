#!/usr/bin/env python3
"""Step 15, run 17y2: the "a source deals damage", "deals damage to you", chosen-color creature and fight card classes
write their trigger conditions as `Triggers` filters.

`DamageTriggerCollectorService` judged trigger-condition records by class and built the effect that went on the stack:
Justice's `ReflectSourceDamageToItsControllerEffect` ("a red creature or spell"), Essence Sliver's
`SourceDamageGainLifeForSourceControllerEffect` ("a Sliver"), The Red Terror's
`PutCountersOnSourceCardForColorSourceDamageEffect` ("a red source you control"), Tamanoa's `GainLifeEffect` (its
handler judged "a noncreature source you control"), No Mercy's, Dread's and Mikaeus's `DestroyDamageSourcePermanentEffect`
("a creature / a Human deals damage to you"), Mangara's Equity's `ReflectDamageToChosenColorCreatureEffect` ("a
creature of the chosen color … to you or a white creature you control") and Foe-Razer Regent's
`PutCounterOnTargetPermanentEffect`, which the fight collector wrapped in a
`RegisterDelayedCountersOnTriggeringPermanentEffect`. Each card now writes the trigger with its relation
(`Triggers.sourceDealsDamage()`, the new `sourceYouControlDealsDamage()`) and filters (`withFilter` the source as a
permanent, the new `withSpellFilter` the source as a spell or card, `withDamagedFilter` the permanent dealt the
damage), and the existing effects that read the stack entry's triggering permanent (the source, the creature that
dealt the damage, the fighting creature) and event value (the damage).

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17y2-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."
EVENT_VALUE = "model.amount.EventValue"
DESTROY = "new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)"
DESTROY_IMPORTS = [E + "DestroyReferencedPermanentEffect", E + "PermanentReference"]
DESTROY_SOURCE = E + "DestroyDamageSourcePermanentEffect"

CARDS = {
    "Justice": (
        [('''        addTrigger(Triggers.sourceDealsDamage(),
                new ReflectSourceDamageToItsControllerEffect(CardColor.RED));''',
          '''        addTrigger(Triggers.sourceDealsDamage()
                        .withFilter(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.RED)))))
                        .withSpellFilter(new CardColorPredicate(CardColor.RED)),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));''')],
        [E + "DealDamageToPlayersEffect", E + "DamageRecipient", EVENT_VALUE, F + "PermanentAllOfPredicate",
         F + "PermanentIsCreaturePredicate", F + "PermanentColorInPredicate", F + "CardColorPredicate",
         "java:java.util.Set"],
        [E + "ReflectSourceDamageToItsControllerEffect"]),
    "EssenceSliver": (
        [('''        addTrigger(Triggers.sourceDealsDamage(),
                new SourceDamageGainLifeForSourceControllerEffect(new CardSubtypePredicate(CardSubtype.SLIVER)));''',
          '''        addTrigger(Triggers.sourceDealsDamage()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.SLIVER))
                        .withSpellFilter(new CardSubtypePredicate(CardSubtype.SLIVER)),
                new GainLifeEffect(new EventValue(), GainLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));''')],
        [E + "GainLifeEffect", E + "GainLifeRecipient", EVENT_VALUE, F + "PermanentHasSubtypePredicate"],
        [E + "SourceDamageGainLifeForSourceControllerEffect"]),
    "TheRedTerror": (
        [('''        addTrigger(Triggers.sourceDealsDamage(),
                new PutCountersOnSourceCardForColorSourceDamageEffect(
                        CardColor.RED, CounterType.PLUS_ONE_PLUS_ONE));''',
          '''        addTrigger(Triggers.sourceYouControlDealsDamage()
                        .withFilter(new PermanentColorInPredicate(Set.of(CardColor.RED)))
                        .withSpellFilter(new CardColorPredicate(CardColor.RED)),
                new PutCountersOnSourceCardEffect(CounterType.PLUS_ONE_PLUS_ONE));''')],
        [E + "PutCountersOnSourceCardEffect", F + "PermanentColorInPredicate", F + "CardColorPredicate",
         "java:java.util.Set"],
        [E + "PutCountersOnSourceCardForColorSourceDamageEffect"]),
    "Tamanoa": (
        [('''        addTrigger(Triggers.sourceDealsDamage(), new GainLifeEffect(new EventValue()));''',
          '''        addTrigger(Triggers.sourceYouControlDealsDamage()
                        .withFilter(new PermanentNotPredicate(new PermanentIsCreaturePredicate()))
                        .withSpellFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new GainLifeEffect(new EventValue()));''')],
        [F + "PermanentNotPredicate", F + "PermanentIsCreaturePredicate", F + "CardNotPredicate",
         F + "CardTypePredicate", "model.CardType"],
        []),
    "NoMercy": (
        [('''        addTrigger(Triggers.permanentDealsDamageToYou(),
                new DestroyDamageSourcePermanentEffect(new PermanentIsCreaturePredicate()));''',
          f'''        addTrigger(Triggers.permanentDealsDamageToYou().withFilter(new PermanentIsCreaturePredicate()),
                {DESTROY});''')],
        DESTROY_IMPORTS, [DESTROY_SOURCE]),
    "Dread": (
        [('''        addTrigger(Triggers.permanentDealsDamageToYou(),
                new DestroyDamageSourcePermanentEffect(new PermanentIsCreaturePredicate()));''',
          f'''        addTrigger(Triggers.permanentDealsDamageToYou().withFilter(new PermanentIsCreaturePredicate()),
                {DESTROY});''')],
        DESTROY_IMPORTS, [DESTROY_SOURCE]),
    "MikaeusTheUnhallowed": (
        [('''        addTrigger(Triggers.permanentDealsDamageToYou(),
                new DestroyDamageSourcePermanentEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));''',
          f'''        addTrigger(Triggers.permanentDealsDamageToYou()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)),
                {DESTROY});''')],
        DESTROY_IMPORTS, [DESTROY_SOURCE]),
    "MangarasEquity": (
        [('''        addTrigger(Triggers.creatureDealsDamageToYouOrYourPermanent(),
                new ReflectDamageToChosenColorCreatureEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.WHITE))))));''',
          '''        addTrigger(Triggers.creatureDealsDamageToYouOrYourPermanent()
                        .withFilter(new PermanentHasSourceChosenColorPredicate())
                        .withDamagedFilter(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.WHITE))))),
                new DealDamageToTriggeringPermanentEffect(new EventValue()));''')],
        [E + "DealDamageToTriggeringPermanentEffect", EVENT_VALUE, F + "PermanentHasSourceChosenColorPredicate"],
        [E + "ReflectDamageToChosenColorCreatureEffect"]),
    "FoeRazerRegent": (
        [('''        addTrigger(Triggers.creatureYouControlFights(),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));''',
          '''        addTrigger(Triggers.creatureYouControlFights(),
                new RegisterDelayedCountersOnTriggeringPermanentEffect(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)));''')],
        [E + "RegisterDelayedCountersOnTriggeringPermanentEffect"], []),
}

GONE = ["ReflectSourceDamageToItsControllerEffect", "SourceDamageGainLifeForSourceControllerEffect",
        "PutCountersOnSourceCardForColorSourceDamageEffect", "DestroyDamageSourcePermanentEffect",
        "ReflectDamageToChosenColorCreatureEffect"]


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
