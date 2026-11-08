#!/usr/bin/env python3
"""Step 15, run 17z: the cast family's trigger-condition records are cast-trigger filters and existing effects.

`SpellCastTriggerCollectorService` read eight records by class on the unfiltered cast triggers and built the effect that
went on the stack from them, and `TriggerCollectionService` did the same for Casualty's
`CopyThisSpellIfCasualtyPaidEffect` and for two emblem markers. Each card now writes the trigger's filter and the
existing effect, which reads the stack entry's triggering spell as it resolves:

- the color counters (`PutPlusOnePlusOneCounterOnSource…ColorSpellCastEffect`): the filter "a [color] spell" /
  "a spell of the chosen color" (`CardColorPredicate`, `CardHasSourceChosenColorPredicate`) or `.anySpell()`, and
  `PutCountersOnSourceEffect(1, 1, 1)`;
- the chosen-color life (`GainLifeForEachChosenColorSpellCastEffect`): Prism Ring and Diamond Mare "a spell of the
  chosen color" and `GainLifeEffect(1)`; Tablet of the Guilds "if it's at least one of the chosen colors" the filter
  and `GainLifeEffect(new TriggeringSpellChosenColorCount())`;
- Bloodbond March: "a creature spell" and `EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect` with
  `CardSharesNameWithTriggeringSpellPredicate`;
- Don't Worry About It: `StackEntryIsCardEnchantedBySourcePredicate` and `CopyTriggeringSpellEffect`;
- Erayo's Essence and Jace, Unraveler of Secrets' emblem: "their first spell each turn" `.nthThisTurn(1)` and
  `CounterSpellEffect`; Garruk, Caller of Beasts' emblem: "a creature spell" and its "you may search";
- Hand of the Praetors: "a creature spell with infect" and `GivePoisonCountersEffect(1, TARGET_PLAYER)`;
- Casualty: the intervening "if" `CasualtyCostPaid` and `CopyTriggeringSpellEffect` (Ob Nixilis, the Adversary's
  token copy that isn't legendary and has loyalty X);
- Discord, Lord of Disharmony: its `DiscordCopyCastTriggerEffect` trigger never triggered (casting the copy queues the
  copied ability and consumes the link first), so it is gone.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17z-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."
A = "model.amount."
C = "model.condition."
T = "model.trigger."

COLOR_RECORD = E + "PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect"
CHOSEN_COLOR_RECORD = E + "PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect"
LIFE_RECORD = E + "GainLifeForEachChosenColorSpellCastEffect"
CASUALTY_RECORD = E + "CopyThisSpellIfCasualtyPaidEffect"
COUNTERS = "new PutCountersOnSourceEffect(1, 1, 1)"
CASUALTY_IMPORTS = [T + "TriggerOptions", C + "CasualtyCostPaid", E + "CopyTriggeringSpellEffect"]
CASUALTY_EDIT = ("        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellIfCasualtyPaidEffect());",
                 "        addTrigger(Triggers.youCastThisSpell(), TriggerOptions.DEFAULT.withInterveningIf(new CasualtyCostPaid()),\n"
                 "                new CopyTriggeringSpellEffect());")
CASUALTY_CARDS = ["AudaciousSwap", "ALittleChat", "CutYourLosses", "CutOfTheProfits", "DigUpTheBody",
                  "FlawlessForgery", "GrislySigil", "IllicitShipment", "JoinTheMaestros", "LightEmUp", "MakeDisappear",
                  "RooftopNuisance", "RobTheArchives", "ShatteringFinale", "XandersPact"]
CHOSEN_COLOR_LIFE = (
    "        addTrigger(Triggers.youCastSpell(), new GainLifeForEachChosenColorSpellCastEffect());",
    "        addTrigger(Triggers.youCastSpell().withFilter(new CardHasSourceChosenColorPredicate()),\n"
    "                new GainLifeEffect(1));")

CARDS = {
    "TitaniasChosen": (
        [("""        addTrigger(Triggers.playerCastsSpell(),
                new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                        Set.of(CardColor.GREEN), 1, false));""",
          """        addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.GREEN)),
                """ + COUNTERS + ");")],
        [F + "CardColorPredicate", E + "PutCountersOnSourceEffect"],
        [COLOR_RECORD, "java:java.util.Set"]),
    "QuirionDryad": (
        [("""        addTrigger(Triggers.youCastSpell(),
                new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                        Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED),
                        1,
                        true
                ));""",
          """        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardColorPredicate(CardColor.WHITE),
                        new CardColorPredicate(CardColor.BLUE),
                        new CardColorPredicate(CardColor.BLACK),
                        new CardColorPredicate(CardColor.RED)))),
                """ + COUNTERS + ");")],
        [F + "CardAnyOfPredicate", F + "CardColorPredicate", E + "PutCountersOnSourceEffect", "java:java.util.List"],
        [COLOR_RECORD, "java:java.util.Set"]),
    "ManagorgerHydra": (
        [("""        // Empty color set = any spell, including colorless ones and the controller's own.
        addTrigger(Triggers.playerCastsSpell(),
                new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(Set.of(), 1, false));""",
          """        addTrigger(Triggers.playerCastsSpell().anySpell(), """ + COUNTERS + ");")],
        [E + "PutCountersOnSourceEffect"],
        [COLOR_RECORD, "java:java.util.Set"]),
    "IshaiOjutaiDragonspeaker": (
        [("""        addTrigger(Triggers.opponentCastsSpell(),
                new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(Set.of(), 1, false));""",
          """        addTrigger(Triggers.opponentCastsSpell().anySpell(), """ + COUNTERS + ");")],
        [E + "PutCountersOnSourceEffect"],
        [COLOR_RECORD, "java:java.util.Set"]),
    "ThunderingMightmare": (
        [("""                        new TriggeredAbility(Triggers.opponentCastsSpell(),
                        new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(Set.of(), 1, false)),""",
          """                        new TriggeredAbility(Triggers.opponentCastsSpell().anySpell(),
                        """ + COUNTERS + "),")],
        [E + "PutCountersOnSourceEffect"],
        [COLOR_RECORD, "java:java.util.Set"]),
    "DiamondKnight": (
        [("""        addTrigger(Triggers.youCastSpell(),
                new PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect(1));""",
          """        addTrigger(Triggers.youCastSpell().withFilter(new CardHasSourceChosenColorPredicate()),
                """ + COUNTERS + ");")],
        [F + "CardHasSourceChosenColorPredicate", E + "PutCountersOnSourceEffect"],
        [CHOSEN_COLOR_RECORD]),
    "PrismRing": (
        [CHOSEN_COLOR_LIFE],
        [F + "CardHasSourceChosenColorPredicate", E + "GainLifeEffect"],
        [LIFE_RECORD]),
    "DiamondMare": (
        [CHOSEN_COLOR_LIFE],
        [F + "CardHasSourceChosenColorPredicate", E + "GainLifeEffect"],
        [LIFE_RECORD]),
    "TabletOfTheGuilds": (
        [("        addTrigger(Triggers.youCastSpell(), new GainLifeForEachChosenColorSpellCastEffect());",
          """        // "If it's at least one of the chosen colors": the filter as the ability triggers, and the count (no life
        // for a spell that is none of them any more) as it resolves.
        addTrigger(Triggers.youCastSpell().withFilter(new CardHasSourceChosenColorPredicate()),
                new GainLifeEffect(new TriggeringSpellChosenColorCount()));""")],
        [F + "CardHasSourceChosenColorPredicate", E + "GainLifeEffect", A + "TriggeringSpellChosenColorCount"],
        [LIFE_RECORD]),
    "BloodbondMarch": (
        [("""        addTrigger(Triggers.playerCastsSpell(),
                new ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect());""",
          """        addTrigger(Triggers.playerCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                        Integer.MAX_VALUE, new CardSharesNameWithTriggeringSpellPredicate()));""")],
        ["model.CardType", F + "CardTypePredicate", F + "CardSharesNameWithTriggeringSpellPredicate",
         E + "EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect"],
        [E + "ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect"]),
    "DontWorryAboutIt": (
        [("        addTrigger(Triggers.youCastSpell(), new CopyEnchantedHandCardOnCastEffect());",
          "        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryIsCardEnchantedBySourcePredicate()),\n"
          "                new CopyTriggeringSpellEffect());")],
        [F + "StackEntryIsCardEnchantedBySourcePredicate", E + "CopyTriggeringSpellEffect"],
        [E + "CopyEnchantedHandCardOnCastEffect"]),
    "ErayosEssence": (
        [("""        addTrigger(Triggers.opponentCastsSpell(),
                new CounterOpponentFirstSpellEachTurnEffect.Marker());""",
          """        addTrigger(Triggers.opponentCastsSpell().nthThisTurn(1), new CounterSpellEffect());""")],
        [E + "CounterSpellEffect"],
        [E + "CounterOpponentFirstSpellEachTurnEffect"]),
    "JaceUnravelerOfSecrets": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new CounterOpponentFirstSpellEachTurnEffect.Marker()),
                        "Whenever an opponent casts their first spell each turn, counter that spell.")),""",
          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.opponentCastsSpell().nthThisTurn(1), new CounterSpellEffect()),
                        "Whenever an opponent casts their first spell each turn, counter that spell.")),""")],
        ["model.TriggeredAbility", T + "Triggers", E + "CounterSpellEffect"],
        [E + "CounterOpponentFirstSpellEachTurnEffect"]),
    "GarrukCallerOfBeasts": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect.Marker()),
                        "Whenever you cast a creature spell, you may search your library for a "
                                + "creature card, put it onto the battlefield, then shuffle.")),""",
          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                                new MayEffect(new SearchLibraryEffect(new CardTypePredicate(CardType.CREATURE),
                                        LibrarySearchDestination.BATTLEFIELD),
                                        "Search your library for a creature card and put it onto the battlefield?")),
                        "Whenever you cast a creature spell, you may search your library for a "
                                + "creature card, put it onto the battlefield, then shuffle.")),""")],
        ["model.TriggeredAbility", T + "Triggers", E + "SearchLibraryEffect", "model.LibrarySearchDestination"],
        [E + "SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect"]),
    "HandOfThePraetors": (
        [("""        addTrigger(Triggers.youCastSpell(),
                new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER, new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardKeywordPredicate(Keyword.INFECT)
                ))));""",
          """        addTrigger(Triggers.youCastSpell().withFilter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardKeywordPredicate(Keyword.INFECT)))),
                new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER));""")],
        [],
        []),
    "DiscordLordOfDisharmony": (
        [("        addTrigger(Triggers.youCastSpell(), new DiscordCopyCastTriggerEffect());\n", "")],
        [],
        [E + "DiscordCopyCastTriggerEffect"]),
    "ObNixilisTheAdversary": (
        [("""        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellIfCasualtyPaidEffect(
                Set.of(CardSupertype.LEGENDARY), true, true));""",
          """        addTrigger(Triggers.youCastThisSpell(), TriggerOptions.DEFAULT.withInterveningIf(new CasualtyCostPaid()),
                new CopyTriggeringSpellEffect(true).withoutSupertypes(Set.of(CardSupertype.LEGENDARY))
                        .withStartingLoyaltyFromX());""")],
        CASUALTY_IMPORTS,
        [CASUALTY_RECORD]),
}
for casualty in CASUALTY_CARDS:
    CARDS[casualty] = ([CASUALTY_EDIT], CASUALTY_IMPORTS, [CASUALTY_RECORD])

GONE = ["PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect",
        "PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect", "GainLifeForEachChosenColorSpellCastEffect",
        "ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect", "CopyEnchantedHandCardOnCastEffect",
        "CounterOpponentFirstSpellEachTurnEffect", "SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect",
        "DiscordCopyCastTriggerEffect", "JodahTheUnifierTriggerEffect", "CopyThisSpellIfCasualtyPaidEffect"]


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
