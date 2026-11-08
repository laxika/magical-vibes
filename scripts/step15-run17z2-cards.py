#!/usr/bin/env python3
"""Step 15, run 17z2: the death, life-gain and crew trigger-condition records are written as abilities whose
existing effects read the stack entry.

`DeathTriggerCollectorService` read three dying-controller records by class and built the effect that went on the
stack with the dying creature's controller bound in; `MiscTriggerCollectorService` read Firesong and Sunspeaker's and
Tiana's records by class, judged their condition and put the record's `triggeredEffect()` on the stack. Each card now
writes the ability:

- Bereavement: "its controller discards a card" is `DiscardEffect(1, DiscardRecipient.TRIGGERING_PERMANENT_CONTROLLER)`;
- Earthlink, Burning Sands: "that creature's controller sacrifices a land of their choice" is
  `SacrificePermanentsEffect(1, land, SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER)`;
- Fecundity: "that creature's controller may draw a card" is `MayEffect(DrawCardForTriggeringPermanentControllerEffect(),
  "Draw a card?", null, MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER)`;
- Firesong and Sunspeaker: "whenever a white instant or sorcery spell causes you to gain life" is
  `Triggers.youGainLife().causedBySpell(white instant or sorcery)`, and "deals 3 damage to target creature or player"
  `DealDamageToTargetCreatureOrPlayerEffect(3)`;
- Tiana, Angelic Mechanic: "when Tiana or another legendary creature you control crews a Vehicle" is
  `Triggers.creatureYouControlCrewsVehicle().withObjectFilter(...)`, and "that Vehicle perpetually gets +1/+0"
  `PerpetuallyBoostCardEffect.ofTriggeringPermanent(1, 0)`.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17z2-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."
T = "model.trigger."

LAND_SACRIFICE = (
    """                new DyingCreatureControllerSacrificesPermanentsEffect(1, new PermanentIsLandPredicate()));""",
    """                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(),
                        SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER));""")

CARDS = {
    "Bereavement": (
        [("""                new DyingCreatureControllerDiscardsCardEffect());""",
          """                new DiscardEffect(1, DiscardRecipient.TRIGGERING_PERMANENT_CONTROLLER));""")],
        [E + "DiscardEffect", E + "DiscardRecipient"],
        [E + "DyingCreatureControllerDiscardsCardEffect"]),
    "Earthlink": (
        [LAND_SACRIFICE],
        [E + "SacrificePermanentsEffect", E + "SacrificeRecipient"],
        [E + "DyingCreatureControllerSacrificesPermanentsEffect"]),
    "BurningSands": (
        [LAND_SACRIFICE],
        [E + "SacrificePermanentsEffect", E + "SacrificeRecipient"],
        [E + "DyingCreatureControllerSacrificesPermanentsEffect"]),
    "Fecundity": (
        [("""        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), new DyingCreatureControllerMayDrawCardEffect());""",
          """        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new MayEffect(new DrawCardForTriggeringPermanentControllerEffect(), "Draw a card?", null,
                        MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));""")],
        [E + "MayEffect", E + "DrawCardForTriggeringPermanentControllerEffect", "model.MayChoicePlayer"],
        [E + "DyingCreatureControllerMayDrawCardEffect"]),
    "FiresongAndSunspeaker": (
        [("""        addTrigger(Triggers.youGainLife(), new DealDamageOnSpellLifeGainEffect(3, CardColor.WHITE));""",
          """        // Whenever a white instant or sorcery spell causes you to gain life, this deals 3 damage to target
        // creature or player.
        addTrigger(Triggers.youGainLife().causedBySpell(new CardAllOfPredicate(List.of(
                        new CardColorPredicate(CardColor.WHITE),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)))))),
                new DealDamageToTargetCreatureOrPlayerEffect(3));""")],
        [F + "CardAllOfPredicate", F + "CardAnyOfPredicate", F + "CardColorPredicate", F + "CardTypePredicate",
         "model.CardType", E + "DealDamageToTargetCreatureOrPlayerEffect", "java:java.util.List"],
        [E + "DealDamageOnSpellLifeGainEffect"]),
    "TianaAngelicMechanic": (
        [("""        addTrigger(Triggers.creatureYouControlCrewsVehicle(),
                new PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsSourceCardPredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))), 1, 0));""",
          """        // When Tiana or another legendary creature you control crews a Vehicle, that Vehicle perpetually gets
        // +1/+0.
        addTrigger(Triggers.creatureYouControlCrewsVehicle().withObjectFilter(new PermanentAnyOfPredicate(List.of(
                        new PermanentIsSourceCardPredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)))),
                PerpetuallyBoostCardEffect.ofTriggeringPermanent(1, 0));""")],
        [E + "PerpetuallyBoostCardEffect"],
        [E + "PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect"]),
}

GONE = ["DyingCreatureControllerDiscardsCardEffect", "DyingCreatureControllerSacrificesPermanentsEffect",
        "DyingCreatureControllerMayDrawCardEffect", "DealDamageOnSpellLifeGainEffect",
        "PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect"]


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
