#!/usr/bin/env python3
"""Step 15, run 17z5: the draw records are a static reveal and filtered draw or reveal abilities.

`DrawService` built Rowen's and Booby Trap's triggered abilities from static markers (`RevealFirstDrawDrawOnBasicLandEffect`,
`BoobyTrapEffect`), and the draw collector bound the drawn card into the effect it put on the stack
(`FirstDrawRevealTriggerEffect.effectFor`, `DrawRevealTriggerEffect.effectFor`, `DrawnCardTriggerEffect.effectForDrawnCard`,
`RevealDrawnCardIfInstantOrSorceryEffect.triggeredEffect()`). Each card now writes its abilities as printed:

- the reveal is the static `RevealDrawnCardsEffect` (`firstEachTurn()`, `firstOnEachOfYourTurns()`,
  `mayRevealFirstEachTurn()`, `eachByChosenPlayer()`, `eachByEveryPlayer()`);
- "whenever you reveal a [card] this way" is `Triggers.youRevealCardThisWay().withFilter(...)` (Rowen, Primitive
  Etchings, Keranos, God-Eternal Kefnet, Inquisitor Eisenhorn), "whenever you draw a Dragon card" and "whenever a player
  draws a land card" draw triggers with the drawn card's filter (Darigaaz's Whelp, Sea of Sand), "when the chosen player
  draws a card with the chosen name" the draw triggers with `CardHasSourceChosenNamePredicate` and the event condition
  `TargetPlayerIsChosenPlayer` (Booby Trap);
- the effects read the stack entry's triggering card (`CopyDrawnInstantOrSorceryAndMayCastCopyEffect()`,
  `PerpetuallyBoostCardEffect.ofTriggeringCard(1, 1)`) or its triggering player
  (`DealDamageToPlayersEffect(10, DamageRecipient.TRIGGERING_PLAYER)`).

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17z5-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."
C = "model.condition."
T = "model.trigger."

BASIC_LAND = """new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND), new CardSupertypePredicate(CardSupertype.BASIC)))"""
INSTANT_OR_SORCERY = """new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)))"""

CARDS = {
    "Rowen": (
        [("""        // "Reveal the first card you draw each turn. Whenever you reveal a basic land card this way,
        // draw a card." Both the reveal and the draw trigger are handled in DrawService.
        addStaticEffect(new RevealFirstDrawDrawOnBasicLandEffect());""",
          f"""        // Reveal the first card you draw each turn. Whenever you reveal a basic land card this way, draw a card.
        addStaticEffect(RevealDrawnCardsEffect.firstEachTurn());
        addTrigger(Triggers.youRevealCardThisWay().withFilter({BASIC_LAND}),
                new DrawCardEffect(1));""")],
        [E + "RevealDrawnCardsEffect", E + "DrawCardEffect", T + "Triggers", F + "CardAllOfPredicate",
         F + "CardTypePredicate", F + "CardSupertypePredicate", "model.CardType", "model.CardSupertype",
         "java:java.util.List"],
        [E + "RevealFirstDrawDrawOnBasicLandEffect"]),
    "PrimitiveEtchings": (
        [("""        addTrigger(Triggers.youDrawCard(), new RevealFirstDrawCreatureEffect());""",
          """        // Reveal the first card you draw each turn. Whenever you reveal a creature card this way, draw a card.
        addStaticEffect(RevealDrawnCardsEffect.firstEachTurn());
        addTrigger(Triggers.youRevealCardThisWay().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new DrawCardEffect(1));""")],
        [E + "RevealDrawnCardsEffect", E + "DrawCardEffect", F + "CardTypePredicate", "model.CardType"],
        [E + "RevealFirstDrawCreatureEffect"]),
    "KeranosGodOfStorms": (
        [("""        addTrigger(Triggers.youDrawCard(), new RevealFirstDrawEffect(
                new DrawCardEffect(1),
                new DealDamageToAnyTargetEffect(3)));""",
          """        // Reveal the first card you draw on each of your turns. Whenever you reveal a land card this way, draw a
        // card. Whenever you reveal a nonland card this way, Keranos deals 3 damage to any target.
        addStaticEffect(RevealDrawnCardsEffect.firstOnEachOfYourTurns());
        addTrigger(Triggers.youRevealCardThisWay().withFilter(new CardTypePredicate(CardType.LAND)),
                new DrawCardEffect(1));
        addTrigger(Triggers.youRevealCardThisWay().withFilter(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                new DealDamageToAnyTargetEffect(3));""")],
        [E + "RevealDrawnCardsEffect", F + "CardTypePredicate", F + "CardNotPredicate"],
        [E + "RevealFirstDrawEffect"]),
    "InquisitorEisenhorn": (
        [("""        addTrigger(Triggers.youDrawCard(),
                new RevealFirstDrawInstantOrSorceryCreateTokenEffect(cherubael));""",
          f"""        // You may reveal the first card you draw each turn as you draw it. Whenever you reveal an instant or
        // sorcery card this way, create Cherubael.
        addStaticEffect(RevealDrawnCardsEffect.mayRevealFirstEachTurn());
        addTrigger(Triggers.youRevealCardThisWay().withFilter({INSTANT_OR_SORCERY}),
                cherubael);""")],
        [E + "RevealDrawnCardsEffect", F + "CardAnyOfPredicate", F + "CardTypePredicate"],
        [E + "RevealFirstDrawInstantOrSorceryCreateTokenEffect"]),
    "GodEternalKefnet": (
        [("""        addTrigger(Triggers.youDrawCard(), new RevealFirstDrawInstantOrSorceryEffect());""",
          f"""        // You may reveal the first card you draw each turn as you draw it. Whenever you reveal an instant or
        // sorcery card this way, copy that card and you may cast the copy. That copy costs {{2}} less to cast.
        addStaticEffect(RevealDrawnCardsEffect.mayRevealFirstEachTurn());
        addTrigger(Triggers.youRevealCardThisWay().withFilter({INSTANT_OR_SORCERY}),
                new CopyDrawnInstantOrSorceryAndMayCastCopyEffect());""")],
        [E + "RevealDrawnCardsEffect", E + "CopyDrawnInstantOrSorceryAndMayCastCopyEffect", F + "CardAnyOfPredicate",
         F + "CardTypePredicate", "model.CardType", "java:java.util.List"],
        [E + "RevealFirstDrawInstantOrSorceryEffect", "java:java.util.Set"]),
    "DarigaazsWhelp": (
        [("""        addTrigger(Triggers.youDrawCard(), new PerpetuallyBoostDrawnDragonEffect());""",
          """        addTrigger(Triggers.youDrawCard().withFilter(new CardSubtypePredicate(CardSubtype.DRAGON)),
                PerpetuallyBoostCardEffect.ofTriggeringCard(1, 1));""")],
        [E + "PerpetuallyBoostCardEffect", F + "CardSubtypePredicate", "model.CardSubtype"],
        [E + "PerpetuallyBoostDrawnDragonEffect"]),
    "BoobyTrap": (
        [("""        // "The chosen player reveals each card they draw." + the draw trigger — handled in DrawService.
        addStaticEffect(new BoobyTrapEffect());""",
          """        // The chosen player reveals each card they draw.
        addStaticEffect(RevealDrawnCardsEffect.eachByChosenPlayer());
        // When the chosen player draws a card with the chosen name, sacrifice this artifact. If you do, this artifact
        // deals 10 damage to that player.
        addTrigger(List.of(Triggers.opponentDrawsCard().withFilter(new CardHasSourceChosenNamePredicate()),
                        Triggers.youDrawCard().withFilter(new CardHasSourceChosenNamePredicate())),
                TriggerOptions.DEFAULT.withEventCondition(new TargetPlayerIsChosenPlayer()),
                new SacrificeSelfThenEffect(new DealDamageToPlayersEffect(10, DamageRecipient.TRIGGERING_PLAYER)));""")],
        [E + "RevealDrawnCardsEffect", E + "SacrificeSelfThenEffect", E + "DealDamageToPlayersEffect",
         E + "DamageRecipient", F + "CardHasSourceChosenNamePredicate", C + "TargetPlayerIsChosenPlayer",
         T + "TriggerOptions", T + "Triggers"],
        [E + "BoobyTrapEffect"]),
    "SeaOfSand": (
        [("""        CardEffect landDraw = new GainLifeEffect(new Fixed(3), GainLifeRecipient.TRIGGERING_PLAYER);
        CardEffect nonlandDraw = new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PLAYER);
        RevealEachDrawEffect drawEffect = new RevealEachDrawEffect(landDraw, nonlandDraw);

        addTrigger(Triggers.youDrawCard(), drawEffect);
        addTrigger(Triggers.opponentDrawsCard(), drawEffect);""",
          """        // Players reveal each card they draw.
        addStaticEffect(RevealDrawnCardsEffect.eachByEveryPlayer());
        // Whenever a player draws a land card, that player gains 3 life.
        CardPredicate land = new CardTypePredicate(CardType.LAND);
        addTrigger(List.of(Triggers.youDrawCard().withFilter(land), Triggers.opponentDrawsCard().withFilter(land)),
                new GainLifeEffect(new Fixed(3), GainLifeRecipient.TRIGGERING_PLAYER));
        // Whenever a player draws a nonland card, that player loses 3 life.
        CardPredicate nonland = new CardNotPredicate(land);
        addTrigger(List.of(Triggers.youDrawCard().withFilter(nonland), Triggers.opponentDrawsCard().withFilter(nonland)),
                new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PLAYER));""")],
        [E + "RevealDrawnCardsEffect", F + "CardPredicate", F + "CardTypePredicate", F + "CardNotPredicate",
         "model.CardType", "java:java.util.List"],
        [E + "RevealEachDrawEffect", E + "CardEffect"]),
}

GONE = ["RevealFirstDrawDrawOnBasicLandEffect", "BoobyTrapEffect", "RevealFirstDrawEffect", "RevealFirstDrawCreatureEffect",
        "RevealFirstDrawInstantOrSorceryEffect", "RevealFirstDrawInstantOrSorceryCreateTokenEffect", "RevealEachDrawEffect",
        "PerpetuallyBoostDrawnDragonEffect"]


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
