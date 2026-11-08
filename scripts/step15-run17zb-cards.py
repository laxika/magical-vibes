#!/usr/bin/env python3
"""Step 15, run 17zb: the emblems' enter and tap abilities are emblem `TriggeredAbility`s, and Sunbird's Invocation's
and Kiora, Sovereign of the Deep's reveals read the triggering spell.

`TriggerCollectionService` read seven emblem markers by class and built the effect that went on the stack from them;
`SpellCastTriggerCollectorService` read two cast markers by class and built their reveal effect with the spell's mana
value bound in. Each card now writes the ability:

- the emblems' "whenever a [filter] you control enters" abilities, read by the new emblem enters reader
  (`checkEmblemEntersTriggers`): Lukka, Wayward Bonder's "it deals damage equal to its power to any target"
  `DealDamageToAnyTargetEffect.fromEnteringPermanent(new SourcePower())`, Chandra, Spark Hunter's and Koth, Fire of
  Resistance's "this emblem deals N damage to any target" `DealDamageToAnyTargetEffect(N)` behind "an artifact" /
  "a Mountain", Kiora, Master of the Depths' "you may have it fight target creature" `MayEffect` of
  `EnteringCreatureFightsTargetCreatureEffect`, Nissa, Vital Force's "you may draw a card" `MayEffect` of
  `DrawCardEffect(1)`;
- Tezzeret, Betrayer of Flesh's emblem "whenever an artifact you control becomes tapped, draw a card",
  `Triggers.permanentYouControlBecomesTapped().withFilter(Triggers.ARTIFACT)` and `DrawCardEffect(1)`, read by the
  emblem tap reader;
- Sunbird's Invocation: `Triggers.youCastSpell().castFrom(Zone.HAND)` and the fieldless
  `SunbirdsInvocationRevealAndCastEffect()`; Kiora, Sovereign of the Deep: the Kraken / Leviathan / Octopus / Serpent
  filter, `.castFrom(Zone.HAND)` and the fieldless `KioraSovereignOfTheDeepRevealAndCastEffect()`.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17zb-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."
A = "model.amount."
T = "model.trigger."

EMBLEM_IMPORTS = ["model.TriggeredAbility", T + "Triggers"]

CARDS = {
    "LukkaWaywardBonder": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new DealDamageToAnyTargetOnAllyCreatureEntersEffect.Marker()),
                        EMBLEM_TEXT)),""",
          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                                DealDamageToAnyTargetEffect.fromEnteringPermanent(new SourcePower())),
                        EMBLEM_TEXT)),""")],
        EMBLEM_IMPORTS + [T + "ObjectRelation", E + "DealDamageToAnyTargetEffect", A + "SourcePower"],
        [E + "DealDamageToAnyTargetOnAllyCreatureEntersEffect"]),
    "ChandraSparkHunter": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new DealDamageToAnyTargetOnControllerArtifactEntersEffect(3)),""",
          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL),
                                new DealDamageToAnyTargetEffect(3)),""")],
        EMBLEM_IMPORTS + [T + "ObjectRelation", E + "DealDamageToAnyTargetEffect"],
        [E + "DealDamageToAnyTargetOnControllerArtifactEntersEffect"]),
    "KothFireOfResistance": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new DealDamageToAnyTargetOnAllyLandEntersEffect(CardSubtype.MOUNTAIN, 4)),""",
          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.enters(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                                ObjectRelation.YOU_CONTROL), new DealDamageToAnyTargetEffect(4)),""")],
        EMBLEM_IMPORTS + [T + "ObjectRelation", E + "DealDamageToAnyTargetEffect"],
        [E + "DealDamageToAnyTargetOnAllyLandEntersEffect"]),
    "KioraMasterOfTheDepths": (
        [("""                        new CreateEmblemEffect(
                                List.of(new MayFightTargetCreatureOnAllyCreatureEntersEffect.Marker()),
                                EMBLEM_TEXT),""",
          """                        new CreateEmblemEffect(
                                new TriggeredAbility(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                                        new MayEffect(new EnteringCreatureFightsTargetCreatureEffect(),
                                                "Have it fight target creature?")),
                                EMBLEM_TEXT),""")],
        EMBLEM_IMPORTS + [T + "ObjectRelation", E + "MayEffect", E + "EnteringCreatureFightsTargetCreatureEffect"],
        [E + "MayFightTargetCreatureOnAllyCreatureEntersEffect"]),
    "NissaVitalForce": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new DrawCardOnAllyLandEntersEffect.Marker()), EMBLEM_TEXT)),""",
          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                                new MayEffect(new DrawCardEffect(1), "Draw a card?")),
                        EMBLEM_TEXT)),""")],
        EMBLEM_IMPORTS + [T + "ObjectRelation", E + "MayEffect", E + "DrawCardEffect"],
        [E + "DrawCardOnAllyLandEntersEffect"]),
    "TezzeretBetrayerOfFlesh": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new EmblemArtifactTapTriggerEffect()), EMBLEM_TEXT)),""",
          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.permanentYouControlBecomesTapped().withFilter(Triggers.ARTIFACT),
                                new DrawCardEffect(1)),
                        EMBLEM_TEXT)),""")],
        EMBLEM_IMPORTS,
        [E + "EmblemArtifactTapTriggerEffect"]),
    "SunbirdsInvocation": (
        [("        addTrigger(Triggers.youCastSpell(), new SunbirdsInvocationTriggerEffect());",
          "        addTrigger(Triggers.youCastSpell().castFrom(Zone.HAND), new SunbirdsInvocationRevealAndCastEffect());")],
        ["model.Zone", E + "SunbirdsInvocationRevealAndCastEffect"],
        [E + "SunbirdsInvocationTriggerEffect"]),
    "KioraSovereignOfTheDeep": (
        [("        addTrigger(Triggers.youCastSpell(), new KioraSovereignOfTheDeepTriggerEffect());",
          """        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.KRAKEN),
                        new CardSubtypePredicate(CardSubtype.LEVIATHAN),
                        new CardSubtypePredicate(CardSubtype.OCTOPUS),
                        new CardSubtypePredicate(CardSubtype.SERPENT)))).castFrom(Zone.HAND),
                new KioraSovereignOfTheDeepRevealAndCastEffect());""")],
        ["model.Zone", "model.CardSubtype", F + "CardAnyOfPredicate", F + "CardSubtypePredicate",
         E + "KioraSovereignOfTheDeepRevealAndCastEffect", "java:java.util.List"],
        [E + "KioraSovereignOfTheDeepTriggerEffect"]),
}

GONE = ["DealDamageToAnyTargetOnAllyCreatureEntersEffect", "DealDamageToAnyTargetOnControllerArtifactEntersEffect",
        "EmblemArtifactEntersTriggerEffect", "DealDamageToAnyTargetOnAllyLandEntersEffect",
        "MayFightTargetCreatureOnAllyCreatureEntersEffect", "DrawCardOnAllyLandEntersEffect",
        "EmblemArtifactTapTriggerEffect", "SunbirdsInvocationTriggerEffect", "KioraSovereignOfTheDeepTriggerEffect"]


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
        elif not new or new not in text:
            if not new and not any(re.search(r"\b" + gone + r"\b", text) for gone in GONE):
                continue
            raise SystemExit(f"{name}: expected text not found:\n{old}")
    if text == original:
        return
    for fqcn in imports:
        text = add_import(text, fqcn)
    for fqcn in removed:
        text = remove_unused_import(text, fqcn)
    text = sort_imports(text)
    text = re.sub(r"\n{3,}", "\n\n", text)
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
