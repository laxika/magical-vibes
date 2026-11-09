#!/usr/bin/env python3
"""Step 15, run 17z13b: the emblems whose triggered ability the engine built from a marker's class write the ability.

Four engine sites built an emblem's ability from a marker in its static-effect list: Tamiyo, the Moon Sage's
`ReturnCardPutIntoGraveyardToHandEffect` (`GraveyardService`), Daretti, Scrap Savant's
`EmblemArtifactGraveyardReturnTriggerEffect` (`DeathTriggerCollectorService`), Teferi, Hero of Dominaria's
`ExileTargetOpponentPermanentOnDrawEffect` (`DrawService`, with an "opponent controls" choice flag) and Huatli, Radiant
Champion's `DrawOnControlledCreatureEntersEffect` (a Beck draw source). Sephiroth, One-Winged Angel's
`EmblemCreatureDeathTriggerEffect` wrapped its ability's effects and target filter for a class-keyed emblem handler.
Each card now writes the emblem's ability (`CreateEmblemEffect(TriggeredAbility, reminderText)`), read by the emblem
readers:

- Tamiyo: "whenever a card is put into your graveyard from anywhere, you may return it to your hand" is
  `Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` (a token isn't a card) and
  `MayEffect(ReturnTriggeringCardToOwnerHandEffect)`;
- Daretti: "whenever an artifact is put into your graveyard from the battlefield, return that card to the battlefield at
  the beginning of the next end step" is `Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT,
  ObjectRelation.YOU_OWN)` and the Lifeline-form `RegisterDelayedReturnDyingCreatureUnderControlEffect` (under the
  card's owner's control, the emblem's controller);
- Teferi: "whenever you draw a card, exile target permanent an opponent controls" is `Triggers.youDrawCard()` and
  `ExileTargetPermanentEffect` with `TargetFilters.permanentAnOpponentControls()` as the ability's target filter;
- Huatli: "whenever a creature you control enters, you may draw a card" is `Triggers.enters(Triggers.CREATURE,
  ObjectRelation.YOU_CONTROL)` and `MayEffect(DrawCardEffect(1))`;
- Sephiroth: "whenever a creature dies, target opponent loses 1 life and you gain 1 life" is
  `Triggers.creatureDies(ObjectRelation.ANY)` and the drain with its "target opponent" filter as the ability's.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17z13b-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."
T = "model.trigger."

EMBLEM_IMPORTS = ["model.TriggeredAbility", T + "Triggers", T + "ObjectRelation"]

GONE = ["ReturnCardPutIntoGraveyardToHandEffect", "EmblemArtifactGraveyardReturnTriggerEffect",
        "ExileTargetOpponentPermanentOnDrawEffect", "DrawOnControlledCreatureEntersEffect",
        "EmblemCreatureDeathTriggerEffect"]

CARDS = {
    "TamiyoTheMoonSage": (
        [("""                        new CreateEmblemEffect(
                                List.of(new ReturnCardPutIntoGraveyardToHandEffect()),
                                "Whenever a card is put into your graveyard from anywhere, you may "
                                        + "return it to your hand.")),""",
          """                        new CreateEmblemEffect(
                                new TriggeredAbility(
                                        Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN,
                                                ObjectRelation.YOU_OWN),
                                        new MayEffect(new ReturnTriggeringCardToOwnerHandEffect(),
                                                "Return that card to your hand?")),
                                "Whenever a card is put into your graveyard from anywhere, you may "
                                        + "return it to your hand.")),""")],
        EMBLEM_IMPORTS + [E + "MayEffect", E + "ReturnTriggeringCardToOwnerHandEffect"],
        [E + "ReturnCardPutIntoGraveyardToHandEffect"]),
    "DarettiScrapSavant": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new EmblemArtifactGraveyardReturnTriggerEffect()), EMBLEM_TEXT)),""",
          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(
                                Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT,
                                        ObjectRelation.YOU_OWN),
                                new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                                        false, null, 0, null, null, false, true, false)),
                        EMBLEM_TEXT)),""")],
        EMBLEM_IMPORTS + ["model.Zone", E + "RegisterDelayedReturnDyingCreatureUnderControlEffect"],
        [E + "EmblemArtifactGraveyardReturnTriggerEffect"]),
    "TeferiHeroOfDominaria": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new ExileTargetOpponentPermanentOnDrawEffect()),
                        "Whenever you draw a card, exile target permanent an opponent controls.")),""",
          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youDrawCard(), new ExileTargetPermanentEffect())
                                .withTargetFilter(TargetFilters.permanentAnOpponentControls()),
                        "Whenever you draw a card, exile target permanent an opponent controls.")),""")],
        ["model.TriggeredAbility", T + "Triggers", E + "ExileTargetPermanentEffect", F + "TargetFilters"],
        [E + "ExileTargetOpponentPermanentOnDrawEffect"]),
    "HuatliRadiantChampion": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new DrawOnControlledCreatureEntersEffect()),
                        "Whenever a creature you control enters, you may draw a card.")),""",
          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                                new MayEffect(new DrawCardEffect(1), "Draw a card?")),
                        "Whenever a creature you control enters, you may draw a card.")),""")],
        EMBLEM_IMPORTS + [E + "MayEffect", E + "DrawCardEffect"],
        [E + "DrawOnControlledCreatureEntersEffect"]),
    "SephirothOneWingedAngel": (
        [("""        addStaticEffect(new AsTransformsIntoThisEffect(new CreateEmblemEffect(
                List.of(new EmblemCreatureDeathTriggerEffect(
                        List.of(SephirothFabledSoldier.DRAIN),
                        SephirothFabledSoldier.OPPONENT_TARGET)),
                "Whenever a creature dies, target opponent loses 1 life and you gain 1 life.")));""",
          """        addStaticEffect(new AsTransformsIntoThisEffect(new CreateEmblemEffect(
                new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), SephirothFabledSoldier.DRAIN)
                        .withTargetFilter(SephirothFabledSoldier.OPPONENT_TARGET),
                "Whenever a creature dies, target opponent loses 1 life and you gain 1 life.")));""")],
        EMBLEM_IMPORTS,
        [E + "EmblemCreatureDeathTriggerEffect"]),
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
