#!/usr/bin/env python3
"""Step 15, run 17w3: the cast triggers' counter and exile rebuilds read the stack entry's triggering spell.

Rewrites the card classes whose cast trigger was a trigger-condition record the spell-cast collector rebuilt with the
spell's name, mana cost or stack object, or the casting player, bound in: each becomes a filtered cast trigger whose
effects read the triggering spell and player from the stack entry. Cephalid Shrine is `CounterUnlessPaysEffect` with
`CardSharesNameWithTriggeringSpellPredicate`; Chalice of the Void and Void Mirror are `CounterSpellEffect` behind a
stack-entry filter (`StackEntryManaValueEqualsSourceCountersPredicate`, no `StackEntryColoredManaSpentPredicate`);
Ice Cave, Knowledge Pool, Possibility Storm and Eye of the Storm keep their resolution effects, which lost their bound
ids; the two cards that still imported the deleted `SpellCastTriggerEffect` lose the import. Idempotent: a file
already converted is left alone. Per letter module.

Usage: step15-run17w3-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"


def imp(cls):
    return f"import {PKG}.{cls};\n"


COUNTER = "model.effect.CounterSpellEffect"
STACK_FILTER_NOT = "model.filter.StackEntryNotPredicate"

# card -> (letter, [(old, new)], imports to add, imports to remove)
CARDS = {
    "CephalidShrine": ("c", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect());""",
         """        addTrigger(Triggers.playerCastsSpell().anySpell(), new CounterUnlessPaysEffect(
                new CardsInGraveyard(new CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER)));"""),
    ], ["model.amount.CardsInGraveyard", "model.amount.CountScope", "model.effect.CounterUnlessPaysEffect",
        "model.filter.CardSharesNameWithTriggeringSpellPredicate"],
        ["model.effect.CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect"]),
    "IceCave": ("i", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect());""",
         """        // Whenever a player casts a spell, any other player may pay that spell's mana cost. If a player does, counter
        // the spell.
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect());"""),
    ], [], []),
    "ChaliceOfTheVoid": ("c", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CounterSpellIfManaValueEqualsSourceCountersEffect(CounterType.CHARGE));""",
         """        // Whenever a player casts a spell with mana value equal to the number of charge counters on this artifact,
        // counter that spell. The counters matter only as the spell is cast (a trigger condition, not an "if").
        addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryManaValueEqualsSourceCountersPredicate(CounterType.CHARGE)),
                new CounterSpellEffect());"""),
    ], [COUNTER, "model.filter.StackEntryManaValueEqualsSourceCountersPredicate"],
        ["model.effect.CounterSpellIfManaValueEqualsSourceCountersEffect"]),
    "VoidMirror": ("v", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CounterSpellIfNoColoredManaSpentEffect());""",
         """        // Whenever a player casts a spell, if no colored mana was spent to cast it, counter that spell. What was spent
        // can't change once the spell is cast, so the stack-entry filter judges the "if" as it would resolve too.
        addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryColoredManaSpentPredicate())),
                new CounterSpellEffect());"""),
    ], [COUNTER, "model.filter.StackEntryColoredManaSpentPredicate", STACK_FILTER_NOT],
        ["model.effect.CounterSpellIfNoColoredManaSpentEffect"]),
    "KnowledgePool": ("k", [
        ("""        addTrigger(Triggers.playerCastsSpell(), new KnowledgePoolCastTriggerEffect());""",
         """        addTrigger(Triggers.playerCastsSpell().castFrom(Zone.HAND), new KnowledgePoolExileAndCastEffect());"""),
    ], ["model.Zone", "model.effect.KnowledgePoolExileAndCastEffect"],
        ["model.effect.KnowledgePoolCastTriggerEffect"]),
    "PossibilityStorm": ("p", [
        ("""        addTrigger(Triggers.playerCastsSpell(), new PossibilityStormCastTriggerEffect());""",
         """        addTrigger(Triggers.playerCastsSpell().castFrom(Zone.HAND), new PossibilityStormExileAndCastEffect());"""),
    ], ["model.Zone", "model.effect.PossibilityStormExileAndCastEffect"],
        ["model.effect.PossibilityStormCastTriggerEffect"]),
    "EyeOfTheStorm": ("e", [
        ("""        addTrigger(Triggers.playerCastsSpell(), new EyeOfTheStormCastTriggerEffect());""",
         """        // Whenever a player casts an instant or sorcery card (a cast copy isn't a card), exile it. Then that player
        // copies each instant or sorcery card exiled with this enchantment and may cast the copies for free.
        addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryIsCopyPredicate(true))),
                new EyeOfTheStormExileAndCopyEffect());"""),
    ], ["model.CardType", "model.effect.EyeOfTheStormExileAndCopyEffect", "model.filter.CardAnyOfPredicate",
        "model.filter.CardTypePredicate", "model.filter.StackEntryIsCopyPredicate", STACK_FILTER_NOT,
        "java:java.util.List"],
        ["model.effect.EyeOfTheStormCastTriggerEffect"]),
    # The deleted SpellCastTriggerEffect record: two cards still imported it without using it.
    "AishaOfSparksAndSmoke": ("a", [], [], ["model.effect.SpellCastTriggerEffect"]),
    "InventiveIteration": ("i", [], [], ["model.effect.SpellCastTriggerEffect"]),
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
