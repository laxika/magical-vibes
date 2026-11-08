#!/usr/bin/env python3
"""Step 15, run 17y3: the "a permanent deals damage to you", "a source of the chosen color deals damage to you" and
"an instant or sorcery spell that targets only a single creature deals damage to that creature" card classes write their
trigger conditions as `Triggers` filters.

The damage collectors judged trigger-condition records by class and built the effect that went on the stack:
Dissipation Field's `ReturnDamageSourcePermanentToHandEffect` (a `ReturnToHandEffect` aimed at the source), Contested
War Zone's and Contested Game Ball's `DamageSourceControllerGainsControlOfThisPermanentEffect` ("a creature deals combat
damage to you", the control change and the untap), Hixus's `ExileDamageSourcePermanentUntilSourceLeavesEffect` ("a
creature deals combat damage to you, if Hixus entered this turn"), Aurification's `PutCounterOnReferencedPermanentEffect`
(its handler judged "a creature"), Circle of Affliction's `MayPayManaEffect` (its handler judged "a source of the chosen
color deals damage to you" and chose the target as it triggered) and Imodane's
`DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect`. Each card now writes the trigger the Oracle
text names (`Triggers.permanentDealsDamageToYou()` with `withFilter`, `creatureDealsCombatDamageToYou()`, the new
`sourceDealsDamageToYou()` with `withFilter` / `withSpellFilter`, the new
`instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature()`), an intervening "if" as the trigger's option, and the
existing effects that read the stack entry's triggering permanent, target and event value.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17y3-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."

CARDS = {
    "Aurification": (
        [('''        addTrigger(Triggers.permanentDealsDamageToYou(),
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, CounterType.GOLD));''',
          '''        addTrigger(Triggers.permanentDealsDamageToYou().withFilter(new PermanentIsCreaturePredicate()),
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, CounterType.GOLD));''')],
        [],
        []),
    "DissipationField": (
        [('''        addTrigger(Triggers.permanentDealsDamageToYou(), new ReturnDamageSourcePermanentToHandEffect());''',
          '''        addTrigger(Triggers.permanentDealsDamageToYou(), ReturnToHandEffect.triggering());''')],
        [E + "ReturnToHandEffect"],
        [E + "ReturnDamageSourcePermanentToHandEffect"]),
    "HixusPrisonWarden": (
        [('''        // "Whenever a creature deals combat damage to you, if Hixus entered this turn, exile that
        // creature until Hixus leaves the battlefield." The intervening-"if" is re-checked on
        // resolution (CR 603.4).
        addTrigger(Triggers.permanentDealsDamageToYou(),
                new ExileDamageSourcePermanentUntilSourceLeavesEffect(
                        new PermanentIsCreaturePredicate(), true, new SourceEnteredThisTurn()));''',
          '''        addTrigger(Triggers.creatureDealsCombatDamageToYou(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceEnteredThisTurn()),
                new ExileTargetPermanentUntilSourceLeavesEffect());''')],
        [E + "ExileTargetPermanentUntilSourceLeavesEffect", "model.trigger.TriggerOptions"],
        [E + "ExileDamageSourcePermanentUntilSourceLeavesEffect", F + "PermanentIsCreaturePredicate"]),
    "ContestedWarZone": (
        [('''        addTrigger(Triggers.permanentDealsDamageToYou(),
                new DamageSourceControllerGainsControlOfThisPermanentEffect(true, true));''',
          '''        addTrigger(Triggers.creatureDealsCombatDamageToYou(),
                new DamageSourceControllerGainsControlOfDamagedPermanentEffect());''')],
        [E + "DamageSourceControllerGainsControlOfDamagedPermanentEffect"],
        [E + "DamageSourceControllerGainsControlOfThisPermanentEffect"]),
    "ContestedGameBall": (
        [('''                new DamageSourceControllerGainsControlOfThisPermanentEffect(true, true, true));''',
          '''                DamageSourceControllerGainsControlOfDamagedPermanentEffect.attackingPlayer(),
                new UntapPermanentsEffect(TapUntapScope.SELF));''')],
        [E + "DamageSourceControllerGainsControlOfDamagedPermanentEffect", E + "UntapPermanentsEffect",
         E + "TapUntapScope"],
        [E + "DamageSourceControllerGainsControlOfThisPermanentEffect"]),
    "CircleOfAffliction": (
        [('''        addTrigger(Triggers.sourceDealsDamage(),''',
          '''        addTrigger(Triggers.sourceDealsDamageToYou()
                        .withFilter(new PermanentHasSourceChosenColorPredicate())
                        .withSpellFilter(new CardHasSourceChosenColorPredicate()),''')],
        [F + "PermanentHasSourceChosenColorPredicate", F + "CardHasSourceChosenColorPredicate"],
        []),
    "ImodaneThePyrohammer": (
        [('''        addTrigger(Triggers.instantOrSorceryYouControlDealsDamage(),
                new DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect());''',
          '''        addTrigger(Triggers.instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature(),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OPPONENT));''')],
        [E + "DealDamageToPlayersEffect", E + "DamageRecipient", "model.amount.EventValue"],
        [E + "DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect"]),
}

GONE = ["ReturnDamageSourcePermanentToHandEffect", "DamageSourceControllerGainsControlOfThisPermanentEffect",
        "ExileDamageSourcePermanentUntilSourceLeavesEffect",
        "DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect"]


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
