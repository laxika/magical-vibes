#!/usr/bin/env python3
"""Step 15, run 17x5b: the card classes that copied an activated ability through `CopyControllerActivatedAbilityTriggerEffect`.

The record was a trigger condition written as an effect: the activation collectors read its fields (a mana cost, a
stack-entry source filter, "equipped creature only", "loyalty only", a target predicate, "{X} in the cost", "exhaust
only", a source-permanent filter, "life paid", "a permanent sacrificed", "optional") and rebuilt a
`CopyControllerActivatedAbilityEffect`, wrapped in a `MayPayManaEffect`, `MayEffect` or `MayPayLifeEqualToAmountEffect`.
Each card now writes an activation trigger whose filters are the conditions (`AbilityTrigger.withKind` LOYALTY, EXHAUST
or WITH_X_IN_COST, `withSource`, `withStackEntryFilter`, `withPayment` LIFE or SACRIFICE; "an ability of equipped
creature" the new `Triggers.playerActivatesNonManaAbility()` with `PermanentIsHostOfSourceAuraPredicate` as its source)
and the existing `CopyTriggeringSpellOrAbilityEffect`, wrapped as the Oracle text says ("you may pay {2}" a
`MayPayManaEffect`, "you may copy" a `MayEffect`, "you may pay that much life again" a `MayPayLifeEqualToAmountEffect`
of the entry's event value).

"An ability of an artifact" (Kurkesh) and "of an artifact or creature" (Ashnod) are the permanent whose ability it is
(CR 109.2), a source filter, where the record judged the stack entry's card type.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17x5b-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

COPY = "new CopyTriggeringSpellOrAbilityEffect()"
OLD = "model.effect.CopyControllerActivatedAbilityTriggerEffect"
NEW = "model.effect.CopyTriggeringSpellOrAbilityEffect"
KIND = "model.trigger.AbilityTrigger"


def may_pay(cost):
    return f'new MayPayManaEffect("{cost}", {COPY},\n                        "Pay {cost} to copy that ability?")'


CARDS = {
    "RingsOfBrighthearth": (
        [('''        addTrigger(Triggers.youActivateNonManaAbility(),
                new CopyControllerActivatedAbilityTriggerEffect("{2}"));''',
          f'''        addTrigger(Triggers.youActivateNonManaAbility(),
                {may_pay("{2}")});''')],
        [NEW, "model.effect.MayPayManaEffect"], [OLD]),
    "KurkeshOnakkeAncient": (
        [('''        addTrigger(Triggers.youActivateNonManaAbility(),
                new CopyControllerActivatedAbilityTriggerEffect("{R}",
                        new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT))));''',
          f'''        addTrigger(Triggers.youActivateNonManaAbility().withSource(new PermanentIsArtifactPredicate()),
                {may_pay("{R}")});''')],
        [NEW, "model.effect.MayPayManaEffect", "model.filter.PermanentIsArtifactPredicate"],
        [OLD, "model.filter.StackEntryCardTypeInPredicate", "model.CardType", "java:java.util.Set"]),
    "IllusionistsBracers": (
        [('''        addTrigger(Triggers.youActivateNonManaAbility(),
                new CopyControllerActivatedAbilityTriggerEffect(null, null, true));''',
          f'''        addTrigger(Triggers.playerActivatesNonManaAbility().withSource(new PermanentIsHostOfSourceAuraPredicate()),
                {COPY});''')],
        [NEW, "model.filter.PermanentIsHostOfSourceAuraPredicate"], [OLD]),
    "BattlemagesBracers": (
        [('''        addTrigger(Triggers.youActivateNonManaAbility(),
                new CopyControllerActivatedAbilityTriggerEffect("{1}", null, true));''',
          f'''        addTrigger(Triggers.playerActivatesNonManaAbility().withSource(new PermanentIsHostOfSourceAuraPredicate()),
                {may_pay("{1}")});''')],
        [NEW, "model.effect.MayPayManaEffect", "model.filter.PermanentIsHostOfSourceAuraPredicate"], [OLD]),
    "ChandrasRegulator": (
        [('''        addTrigger(Triggers.youActivateNonManaAbility(),
                new CopyControllerActivatedAbilityTriggerEffect(
                        "{1}",
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryCardTypeInPredicate(Set.of(CardType.PLANESWALKER)),
                                new StackEntrySubtypeInPredicate(Set.of(CardSubtype.CHANDRA)))),
                        false,
                        true));''',
          f'''        addTrigger(Triggers.youActivateNonManaAbility()
                        .withKind(AbilityTrigger.AbilityKind.LOYALTY)
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryCardTypeInPredicate(Set.of(CardType.PLANESWALKER)),
                                new StackEntrySubtypeInPredicate(Set.of(CardSubtype.CHANDRA))))),
                {may_pay("{1}")});''')],
        [NEW, "model.effect.MayPayManaEffect", KIND], [OLD]),
    "LeoriSparktouchedHunter": (
        [('''        addTrigger(Triggers.youActivateAbility(),
                new CopyControllerActivatedAbilityTriggerEffect(
                        null,
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryCardTypeInPredicate(Set.of(CardType.PLANESWALKER)),
                                new StackEntryHasSourceChosenSubtypePredicate()))));''',
          f'''        addTrigger(Triggers.youActivateAbility().withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                        new StackEntryCardTypeInPredicate(Set.of(CardType.PLANESWALKER)),
                        new StackEntryHasSourceChosenSubtypePredicate()))),
                {COPY});''')],
        [NEW], [OLD]),
    "ErthaJoFrontierMentor": (
        [('''        addTrigger(Triggers.youActivateNonManaAbility(),
                new CopyControllerActivatedAbilityTriggerEffect(
                        null,
                        null,
                        false,
                        false,
                        new StackEntryAnyOfPredicate(List.of(
                                new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate()),
                                new StackEntryTargetsAnyPlayerPredicate()))));''',
          f'''        addTrigger(Triggers.youActivateNonManaAbility().withStackEntryFilter(new StackEntryAnyOfPredicate(List.of(
                        new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate()),
                        new StackEntryTargetsAnyPlayerPredicate()))),
                {COPY});''')],
        [NEW], [OLD]),
    "RowansTalent": (
        [('''        addTrigger(Triggers.youActivateNonManaAbility(),
                new CopyControllerActivatedAbilityTriggerEffect(
                        null,
                        null,
                        false,
                        true,
                        null,
                        false,
                        false,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsPlaneswalkerPredicate(),
                                new PermanentIsHostOfSourceAuraPredicate()))));''',
          f'''        addTrigger(Triggers.youActivateNonManaAbility()
                        .withKind(AbilityTrigger.AbilityKind.LOYALTY)
                        .withSource(new PermanentAllOfPredicate(List.of(
                                new PermanentIsPlaneswalkerPredicate(),
                                new PermanentIsHostOfSourceAuraPredicate()))),
                {COPY});''')],
        [NEW, KIND], [OLD]),
    "SalaDeckBoss": (
        [('''        addTrigger(Triggers.youActivateNonManaAbility(),
                new CopyControllerActivatedAbilityTriggerEffect(
                        null, null, false, false, null, false, true));''',
          f'''        addTrigger(Triggers.youActivateNonManaAbility().withKind(AbilityTrigger.AbilityKind.EXHAUST),
                {COPY});''')],
        [NEW, KIND], [OLD]),
    "UnboundFlourishing": (
        [('''        addTrigger(Triggers.youActivateAbility(),
                new CopyControllerActivatedAbilityTriggerEffect(null, null, false, false, null, true));''',
          f'''        addTrigger(Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.WITH_X_IN_COST),
                {COPY});''')],
        [NEW, KIND], [OLD]),
    "LocusOfEnlightenment": (
        [('''        addTrigger(Triggers.youActivateNonManaAbility(),
                new CopyControllerActivatedAbilityTriggerEffect(null));''',
          f'''        addTrigger(Triggers.youActivateNonManaAbility(),
                {COPY});''')],
        [NEW], [OLD]),
    "VerrakWarpedSengir": (
        [('''        addTrigger(Triggers.youActivateNonManaAbility(),
                CopyControllerActivatedAbilityTriggerEffect.whenLifeIsPaid());''',
          f'''        addTrigger(Triggers.youActivateNonManaAbility().withPayment(AbilityTrigger.ActivationPayment.LIFE),
                new MayPayLifeEqualToAmountEffect(new EventValue(), {COPY},
                        "Pay that much life to copy that ability?"));''')],
        [NEW, KIND, "model.effect.MayPayLifeEqualToAmountEffect", "model.amount.EventValue"], [OLD]),
    "AshnodTheUncaring": (
        [('''        addTrigger(Triggers.youActivateNonManaAbility(),
                CopyControllerActivatedAbilityTriggerEffect.whenPermanentWasSacrificed(
                        new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT, CardType.CREATURE))));''',
          f'''        addTrigger(Triggers.youActivateNonManaAbility()
                        .withSource(new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate())))
                        .withPayment(AbilityTrigger.ActivationPayment.SACRIFICE),
                new MayEffect({COPY}, "Copy that ability?"));''')],
        [NEW, KIND, "model.effect.MayEffect", "model.filter.PermanentAnyOfPredicate",
         "model.filter.PermanentIsArtifactPredicate", "model.filter.PermanentIsCreaturePredicate", "java:java.util.List"],
        [OLD, "model.filter.StackEntryCardTypeInPredicate", "model.CardType", "java:java.util.Set"]),
}


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
    for fqcn in imports:
        text = add_import(text, fqcn)
    for fqcn in removed:
        text = remove_unused_import(text, fqcn)
    text = text.replace(";\n\n\n@CardRegistration", ";\n\n@CardRegistration").replace(";\n\n\npublic class", ";\n\npublic class")
    if "CopyControllerActivatedAbilityTriggerEffect" in text:
        raise SystemExit(f"{name}: still names CopyControllerActivatedAbilityTriggerEffect")
    if text != original:
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
