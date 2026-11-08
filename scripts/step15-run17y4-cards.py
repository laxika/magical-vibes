#!/usr/bin/env python3
"""Step 15, run 17y4: the "damage is dealt to a permanent or player" and emblem combat damage card classes write their
trigger conditions as `Triggers` filters and options.

The damage collectors judged trigger-condition records by class and built the effect that went on the stack: Sower of
Discord's `OtherChosenPlayerLosesLifeEffect` (a `LoseLifeEffect` for "the other chosen player", bound as the entry's
target), Ghyrson Starn's `GhyrsonStarnKelermorphEffect` (a `DealDamageToAnyTargetEffect(2)` for each recipient dealt
exactly 1 damage), Lolth's emblem's `LoseLifeToThresholdOnCombatDamageEffect` (read by class by the emblem collector,
its `triggeredEffect()` counting every opponent's life loss) and Maarika's `SacrificePermanentsEffect` (its handler
judged "Maarika deals damage" and "if that creature was dealt excess damage this turn" and bound the creature's
controller as the target). Each card now writes the trigger the Oracle text names (the new
`sourceDealsDamageToPermanentOrPlayer()` with `withDamagedPlayerFilter`, `anotherSourceYouControlDealsDamageToPermanentOrPlayer()`
with an event condition, the emblem's `TriggeredAbility` on the new `creaturesYouControlDealCombatDamageToOpponent()`,
`dealsDamageToCreature()`), an intervening "if" as the trigger's option, and existing effects that read the stack
entry's target, triggering player, triggering permanent and event value.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17y4-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."
C = "model.condition."
A = "model.amount."

CARDS = {
    "SowerOfDiscord": (
        [('''        addTrigger(Triggers.sourceDealsDamage(), new OtherChosenPlayerLosesLifeEffect());''',
          '''        addTrigger(Triggers.sourceDealsDamageToPermanentOrPlayer()
                        .withDamagedPlayerFilter(new PlayerChosenBySourcePredicate()),
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.OTHER_CHOSEN_PLAYER));''')],
        [F + "PlayerChosenBySourcePredicate", E + "LoseLifeEffect", E + "LoseLifeRecipient", A + "EventValue"],
        [E + "OtherChosenPlayerLosesLifeEffect"]),
    "GhyrsonStarnKelermorph": (
        [('''        addTrigger(Triggers.sourceDealsDamage(), new GhyrsonStarnKelermorphEffect());''',
          '''        // Three Autostubs: "exactly 1 damage" is a fact of the event, judged as the ability triggers.
        addTrigger(Triggers.anotherSourceYouControlDealsDamageToPermanentOrPlayer(),
                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new EventValueAtLeast(2))),
                new DealDamageToAnyTargetEffect(2));''')],
        ["model.trigger.TriggerOptions", C + "NotCondition", C + "EventValueAtLeast", E + "DealDamageToAnyTargetEffect"],
        [E + "GhyrsonStarnKelermorphEffect"]),
    "LolthSpiderQueen": (
        [('''                List.of(new CreateEmblemEffect(
                        List.of(new LoseLifeToThresholdOnCombatDamageEffect(8)), EMBLEM_TEXT)),''',
          '''                List.of(new CreateEmblemEffect(new TriggeredAbility(
                        Triggers.creaturesYouControlDealCombatDamageToOpponent(),
                        TriggerOptions.DEFAULT.withInterveningIf(
                                new NotCondition(new TargetPlayerLostLifeThisTurn(8))),
                        new LoseLifeEffect(new Sum(new Fixed(8),
                                new Scaled(new LifeLostThisTurn(CountScope.TARGET_PLAYER), -1)),
                                LoseLifeRecipient.TRIGGERING_PLAYER)), EMBLEM_TEXT)),''')],
        ["model.TriggeredAbility", "model.trigger.TriggerOptions", C + "NotCondition",
         C + "TargetPlayerLostLifeThisTurn", A + "Sum", A + "Fixed", A + "Scaled", A + "LifeLostThisTurn",
         A + "CountScope", E + "LoseLifeRecipient"],
        [E + "LoseLifeToThresholdOnCombatDamageEffect"]),
    "MaarikaBrutalGladiator": (
        [('''        addTrigger(Triggers.creatureYouControlDealsDamageToCreature(),
                new SacrificePermanentsEffect(1, noncreatureNonland, SacrificeRecipient.TARGET_PLAYER));''',
          '''        addTrigger(Triggers.dealsDamageToCreature(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new TriggeringPermanentMatches(new PermanentDealtExcessDamageThisTurnPredicate())),
                new SacrificePermanentsEffect(1, noncreatureNonland,
                        SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER));''')],
        ["model.trigger.TriggerOptions", C + "TriggeringPermanentMatches",
         F + "PermanentDealtExcessDamageThisTurnPredicate"],
        []),
}

GONE = ["OtherChosenPlayerLosesLifeEffect", "GhyrsonStarnKelermorphEffect", "LoseLifeToThresholdOnCombatDamageEffect",
        "EmblemCombatDamageTriggerEffect"]


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
