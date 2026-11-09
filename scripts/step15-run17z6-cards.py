#!/usr/bin/env python3
"""Step 15, run 17z6: the leftover death markers, the craft-exile wrapper, Monkey Cage's baked mana value and Firebender
Ascension's record are abilities written as printed, with existing effects reading the stack entry.

- Verdant Succession: `DyingCreatureControllerMaySearchLibraryForSameNameEffect` (the death collector bound the dying
  card's name into a `SearchTargetLibraryEffect`) is `MayEffect(SearchLibraryEffect(1, new
  CardSharesNameWithTriggeringPermanentPredicate(), BATTLEFIELD, TRIGGERING_PERMANENT_CONTROLLER), ...,
  MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER)`, the name read from the entry's triggering permanent as the ability
  resolves;
- Pattern of Rebirth: `EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect` is the `MayEffect`
  its collector built (the same search, asked of the enchanted creature's controller);
- Market Gnome: `SelfExiledWhileActivatingCraftAbilityEffect` (a trigger condition written as a wrapper) is
  `Triggers.exiledWhileActivatingCraftAbility(ObjectRelation.THIS)` with the card's own effect;
- Monkey Cage: `SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect` (the collector baked the entering creature's
  mana value as the event value) is `SequenceEffect.of(new SacrificeSelfEffect(), monkeys.withAmount(new
  TriggeringPermanentManaValue()))`, read as the ability resolves;
- Firebender Ascension: `FirebenderAscensionEffect` (a class-keyed collector built the ability) is the printed ability,
  `PutCountersOnSourceCardEffect(QUEST)` then `ConditionalEffect(SourceCounterThreshold(4, QUEST),
  MayEffect(CopyTriggeringSpellOrAbilityEffect()))`, the triggering ability the entry's triggering spell snapshot;
- Professor Dellian Fel: the emblem marker `EmblemLifeGainTriggerEffect.Marker` (read by class, its "target opponent"
  never chosen) is an emblem `TriggeredAbility` on `Triggers.youGainLife()` with `LoseLifeEffect(EventValue,
  TARGET_PLAYER)` and an opponent target filter;
- Syr Carah, the Bold: `ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect` (a class-keyed
  handler fanned the instant-or-sorcery damage event out per damaged player) is the new
  `Triggers.instantOrSorceryYouControlDealsDamageToPlayer()` with the card's `ExileTopCardMayPlayThisTurnEffect(false)`.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17z6-cards.py <letter> [<letter> ...]   (or "all")
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
T = "model.trigger."

GONE = ["DyingCreatureControllerMaySearchLibraryForSameNameEffect",
        "EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect",
        "SelfExiledWhileActivatingCraftAbilityEffect",
        "SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect",
        "FirebenderAscensionEffect",
        "EmblemLifeGainTriggerEffect",
        "ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect"]

CARDS = {
    "VerdantSuccession": (
        [("""                new DyingCreatureControllerMaySearchLibraryForSameNameEffect());""",
          """                new MayEffect(new SearchLibraryEffect(new Fixed(1),
                        new CardSharesNameWithTriggeringPermanentPredicate(), LibrarySearchDestination.BATTLEFIELD,
                        LibrarySearchPlayer.TRIGGERING_PERMANENT_CONTROLLER),
                        "Search your library for a card with the same name and put it onto the battlefield?",
                        null, MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));""")],
        [E + "MayEffect", E + "SearchLibraryEffect", A + "Fixed", F + "CardSharesNameWithTriggeringPermanentPredicate",
         "model.LibrarySearchDestination", "model.LibrarySearchPlayer", "model.MayChoicePlayer"],
        [E + "DyingCreatureControllerMaySearchLibraryForSameNameEffect"]),
    "PatternOfRebirth": (
        [("""                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect());""",
          """                // When enchanted creature dies, that creature's controller may search their library for a creature
                // card, put that card onto the battlefield, then shuffle.
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new MayEffect(new SearchLibraryEffect(new Fixed(1), new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD,
                                LibrarySearchPlayer.TRIGGERING_PERMANENT_CONTROLLER),
                                "Search your library for a creature card and put it onto the battlefield?",
                                null, MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));""")],
        [E + "MayEffect", E + "SearchLibraryEffect", A + "Fixed", F + "CardTypePredicate", "model.CardType",
         "model.LibrarySearchDestination", "model.LibrarySearchPlayer", "model.MayChoicePlayer"],
        [E + "EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect"]),
    "MarketGnome": (
        [("""        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new SelfExiledWhileActivatingCraftAbilityEffect(gainLifeAndDraw));""",
          """        addTrigger(Triggers.exiledWhileActivatingCraftAbility(ObjectRelation.THIS), gainLifeAndDraw);""")],
        [],
        [E + "SelfExiledWhileActivatingCraftAbilityEffect"]),
    "MonkeyCage": (
        [("""                new SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect(
                        new CreateTokenEffect("Monkey", 2, 2, CardColor.GREEN,
                                List.of(CardSubtype.MONKEY), Set.of(), Set.of())));""",
          """                SequenceEffect.of(new SacrificeSelfEffect(),
                        new CreateTokenEffect("Monkey", 2, 2, CardColor.GREEN,
                                List.of(CardSubtype.MONKEY), Set.of(), Set.of())
                                .withAmount(new TriggeringPermanentManaValue())));""")],
        [E + "SequenceEffect", E + "SacrificeSelfEffect", A + "TriggeringPermanentManaValue"],
        [E + "SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect"]),
    "FirebenderAscension": (
        [("""        addTrigger(Triggers.attackingCreatureYouControlAbilityTriggers(), new FirebenderAscensionEffect());""",
          """        // Whenever a creature you control attacking causes a triggered ability of that creature to trigger, put a
        // quest counter on this enchantment. Then if it has four or more quest counters on it, you may copy that
        // ability. You may choose new targets for the copy.
        addTrigger(Triggers.attackingCreatureYouControlAbilityTriggers(),
                new PutCountersOnSourceCardEffect(CounterType.QUEST),
                new ConditionalEffect(new SourceCounterThreshold(4, CounterType.QUEST),
                        new MayEffect(new CopyTriggeringSpellOrAbilityEffect(), "Copy that triggered ability?")));""")],
        [E + "PutCountersOnSourceCardEffect", E + "ConditionalEffect", E + "MayEffect",
         E + "CopyTriggeringSpellOrAbilityEffect", C + "SourceCounterThreshold", "model.CounterType"],
        [E + "FirebenderAscensionEffect"]),
    "ProfessorDellianFel": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new EmblemLifeGainTriggerEffect.Marker(List.of(
                                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER)
                        ))),
                        "Whenever you gain life, target opponent loses that much life."
                )),""",
          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youGainLife(),
                                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER))
                                .withTargetFilter(new PlayerPredicateTargetFilter(
                                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                        "Target must be an opponent")),
                        "Whenever you gain life, target opponent loses that much life."
                )),""")],
        ["model.TriggeredAbility", T + "Triggers", F + "PlayerPredicateTargetFilter", F + "PlayerRelation",
         F + "PlayerRelationPredicate"],
        [E + "EmblemLifeGainTriggerEffect"]),
    "SyrCarahTheBold": (
        [("""        addTrigger(Triggers.instantOrSorceryYouControlDealsDamage(),
                new ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect());""",
          """        addTrigger(Triggers.instantOrSorceryYouControlDealsDamageToPlayer(),
                new ExileTopCardMayPlayThisTurnEffect(false));""")],
        [],
        [E + "ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect"]),
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
