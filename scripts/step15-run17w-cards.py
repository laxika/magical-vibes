#!/usr/bin/env python3
"""Step 15, run 17w: the spell-cast mana-value family reads the stack entry's triggering spell.

Rewrites the card classes whose cast trigger was a trigger-condition record the spell-cast collector rebuilt with the
spell's mana value (or the mana spent on it, or the source's counters) baked in: each becomes a filtered cast trigger
whose existing effect reads the triggering spell as the ability resolves (`TargetSpellManaValue`, `ManaSpentToCast`).
Idempotent: a file already converted is left alone. Per letter module.

Usage: step15-run17w-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"


def imp(cls):
    return f"import {PKG}.{cls};\n"


# card -> (letter, [(old, new)], imports to add, imports to remove)
CARDS = {
    "RashmiEternitiesCrafter": ("r", [
        ("""        // Whenever you cast your first spell each turn, reveal the top card of your library and
        // use the triggering spell's mana value to determine whether it may be cast for free.
        addTrigger(Triggers.youCastSpell(), new RashmiTriggerEffect());""",
         """        // Whenever you cast your first spell each turn, reveal the top card of your library. If it's a nonland
        // card with mana value less than that spell's, you may cast it without paying its mana cost. If you don't
        // cast the revealed card, put it into your hand.
        addTrigger(Triggers.youCastSpell().nthThisTurn(1), new RashmiRevealTopCardEffect());"""),
    ], ["model.effect.RashmiRevealTopCardEffect"], ["model.effect.RashmiTriggerEffect"]),
    "ThorGodOfThunder": ("t", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new DealDamageEqualToSpellManaValueToAnyTargetEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))
                ));""",
         """        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new DealDamageToAnyTargetEffect(new TargetSpellManaValue()));"""),
    ], ["model.amount.TargetSpellManaValue", "model.effect.DealDamageToAnyTargetEffect"],
        ["model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect"]),
    "RageExtractor": ("r", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new DealDamageEqualToSpellManaValueToAnyTargetEffect(new PhyrexianManaPredicate()));""",
         """        addTrigger(Triggers.youCastSpell().withFilter(new PhyrexianManaPredicate()),
                new DealDamageToAnyTargetEffect(new TargetSpellManaValue()));"""),
    ], ["model.amount.TargetSpellManaValue", "model.effect.DealDamageToAnyTargetEffect"],
        ["model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect"]),
    "KaervekTheMerciless": ("k", [
        ("""        addTrigger(Triggers.opponentCastsSpell(),
                new DealDamageEqualToSpellManaValueToAnyTargetEffect(null));""",
         """        addTrigger(Triggers.opponentCastsSpell().anySpell(),
                new DealDamageToAnyTargetEffect(new TargetSpellManaValue()));"""),
    ], ["model.amount.TargetSpellManaValue", "model.effect.DealDamageToAnyTargetEffect"],
        ["model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect"]),
    "HammerheadTyrant": ("h", [
        ("""        target(TargetFilters.nonlandPermanentAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.youCastSpell(),
                        new ReturnTargetPermanentByCastSpellManaValueEffect());""",
         """        // "With mana value X or less, where X is that spell's mana value": the target is chosen as the ability goes
        // on the stack, against the spell's mana value as the ability's X.
        target(new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentMaxManaValueXPredicate())),
                        "Target must be a nonland permanent an opponent controls with mana value X or less"), 0, 1)
                .addTrigger(Triggers.youCastSpell().anySpell(), ReturnToHandEffect.target());"""),
    ], ["model.effect.ReturnToHandEffect", "model.filter.PermanentAllOfPredicate",
        "model.filter.PermanentControlledBySourceControllerPredicate", "model.filter.PermanentIsLandPredicate",
        "model.filter.PermanentMaxManaValueXPredicate", "model.filter.PermanentNotPredicate",
        "model.filter.PermanentPredicateTargetFilter", "java:java.util.List"],
        ["model.effect.ReturnTargetPermanentByCastSpellManaValueEffect", "model.filter.TargetFilters"]),
    "ChandraDressedToKill": ("c", [
        ("""                        new CreateEmblemEffect(
                                List.of(new DealDamageEqualToManaSpentToCastToAnyTargetEffect(
                                        new CardColorPredicate(CardColor.RED))),
                                "Whenever you cast a red spell, this emblem deals X damage to any \"""",
         """                        new CreateEmblemEffect(
                                new TriggeredAbility(
                                        Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.RED)),
                                        new DealDamageToAnyTargetEffect(new ManaSpentToCast())),
                                "Whenever you cast a red spell, this emblem deals X damage to any \""""),
    ], ["model.TriggeredAbility", "model.amount.ManaSpentToCast", "model.effect.DealDamageToAnyTargetEffect",
        "model.trigger.Triggers"],
        ["model.effect.DealDamageEqualToManaSpentToCastToAnyTargetEffect"]),
    "SkitteringCicada": ("s", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new BoostSelfByCastSpellManaValueEffect(new CardIsColorlessPredicate(), Keyword.TRAMPLE));""",
         """        addTrigger(Triggers.youCastSpell().withFilter(new CardIsColorlessPredicate()),
                new BoostSelfEffect(new TargetSpellManaValue(), new TargetSpellManaValue()),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF));"""),
    ], ["model.amount.TargetSpellManaValue", "model.effect.BoostSelfEffect", "model.effect.GrantKeywordEffect",
        "model.effect.GrantScope"],
        ["model.effect.BoostSelfByCastSpellManaValueEffect"]),
    "RenegadeBull": ("r", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new BoostSelfByCastSpellManaValueEffect(instantOrSorcery, false));""",
         """        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery),
                new BoostSelfEffect(new TargetSpellManaValue(), new Fixed(0)));"""),
    ], ["model.amount.Fixed", "model.amount.TargetSpellManaValue", "model.effect.BoostSelfEffect"],
        ["model.effect.BoostSelfByCastSpellManaValueEffect"]),
    "Manaplasm": ("m", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new BoostSelfByCastSpellManaValueEffect(null));""",
         """        addTrigger(Triggers.youCastSpell().anySpell(),
                new BoostSelfEffect(new TargetSpellManaValue(), new TargetSpellManaValue()));"""),
    ], ["model.amount.TargetSpellManaValue", "model.effect.BoostSelfEffect"],
        ["model.effect.BoostSelfByCastSpellManaValueEffect"]),
    "JamieMcCrimmon": ("j", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new BoostSelfByCastSpellManaValueEffect(new CardIsHistoricPredicate()));""",
         """        addTrigger(Triggers.youCastSpell().withFilter(new CardIsHistoricPredicate()),
                new BoostSelfEffect(new TargetSpellManaValue(), new TargetSpellManaValue()));"""),
    ], ["model.amount.TargetSpellManaValue", "model.effect.BoostSelfEffect"],
        ["model.effect.BoostSelfByCastSpellManaValueEffect"]),
    "ErraticCyclops": ("e", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new BoostSelfByCastSpellManaValueEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        false));""",
         """        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))),
                new BoostSelfEffect(new TargetSpellManaValue(), new Fixed(0)));"""),
    ], ["model.amount.Fixed", "model.amount.TargetSpellManaValue", "model.effect.BoostSelfEffect"],
        ["model.effect.BoostSelfByCastSpellManaValueEffect"]),
    "ImminentDoom": ("i", [
        ("""        // Damage is snapshotted at trigger time (CountersOnSource → Fixed in the collector).
        addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryManaValueEqualsSourceCountersPredicate(CounterType.DOOM)),
                new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.DOOM)),""",
         """        // "That much" is the number of doom counters as the ability triggered (its ruling), which is the triggering
        // spell's mana value.
        addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryManaValueEqualsSourceCountersPredicate(CounterType.DOOM)),
                new DealDamageToAnyTargetEffect(new TargetSpellManaValue()),"""),
    ], ["model.amount.TargetSpellManaValue"], ["model.amount.CountersOnSource"]),
}


def import_line(name):
    return f"import {name[5:]};\n" if name.startswith("java:") else imp(name)


def convert(name, letter, edits, add, remove):
    path = ROOT / f"magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
    text = path.read_text()
    original = text
    for old, new in edits:
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
        text = text.replace(old, new)
    for cls in remove:
        text = text.replace(import_line(cls), "")
    lines = text.split("\n")
    for cls in add:
        line = import_line(cls).rstrip("\n")
        if line in lines:
            continue
        imports = [i for i, l in enumerate(lines) if l.startswith("import ")]
        same_kind = [i for i in imports if lines[i].startswith("import java.") == line.startswith("import java.")]
        if same_kind:
            position = next((i for i in same_kind if lines[i] > line), same_kind[-1] + 1)
        else:
            position = imports[-1] + 1
            lines.insert(position, "")
            position += 1
        lines.insert(position, line)
    text = "\n".join(lines)
    if text != original:
        path.write_text(text)
        print(f"converted {name}")


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    for name, (letter, edits, add, remove) in CARDS.items():
        if letter in letters:
            convert(name, letter, edits, add, remove)


if __name__ == "__main__":
    main()
