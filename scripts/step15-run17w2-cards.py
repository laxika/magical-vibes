#!/usr/bin/env python3
"""Step 15, run 17w2: the cast triggers' player and characteristic rebuilds read the stack entry's triggering spell.

Rewrites the card classes whose cast trigger was a trigger-condition record the spell-cast collector rebuilt with the
casting player, the spell's name, its mana value or the colors spent on it bound in: each becomes a filtered cast
trigger whose existing effects read the triggering player (`TRIGGERING_PLAYER`, the entry's "that player") or the
triggering spell (`TargetSpellManaValue`, `CardSharesNameWithTriggeringSpellPredicate`, the spell's mana value or its
colors spent as the ability's X). Idempotent: a file already converted is left alone. Per letter module.

Usage: step15-run17w2-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"


def imp(cls):
    return f"import {PKG}.{cls};\n"


LOSE = "model.effect.LoseLifeEffect"
LOSE_R = "model.effect.LoseLifeRecipient"
DMG = "model.effect.DealDamageToPlayersEffect"
DMG_R = "model.effect.DamageRecipient"
SAME_NAME = "model.filter.CardSharesNameWithTriggeringSpellPredicate"
IN_GRAVEYARDS = "model.amount.CardsInGraveyard"
SCOPE = "model.amount.CountScope"

# card -> (letter, [(old, new)], imports to add, imports to remove)
CARDS = {
    "DragonlordKolaghan": ("d", [
        ("""        addTrigger(Triggers.opponentCastsSpell(), new CasterLosesLifeOnSpellCastEffect(
                new CardAllOfPredicate(List.of(creatureOrPlaneswalker,
                        new CardNameInControllerGraveyardPredicate())), 10));""",
         """        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardAllOfPredicate(List.of(creatureOrPlaneswalker,
                        new CardNameInControllerGraveyardPredicate()))),
                new LoseLifeEffect(10, LoseLifeRecipient.TRIGGERING_PLAYER));"""),
    ], [LOSE, LOSE_R], ["model.effect.CasterLosesLifeOnSpellCastEffect"]),
    "MageHunter": ("m", [
        ("""        addTrigger(Triggers.opponentCastsSpell(),
                new CasterLosesLifeOnSpellCastEffect(instantOrSorcery, 1));""",
         """        addTrigger(Triggers.opponentCastsSpell().withFilter(instantOrSorcery),
                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER));"""),
    ], [], ["model.effect.CasterLosesLifeOnSpellCastEffect"]),
    "SootImp": ("s", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CasterLosesLifeOnSpellCastEffect(
                        new CardNotPredicate(new CardColorPredicate(CardColor.BLACK)), 1));""",
         """        addTrigger(Triggers.playerCastsSpell().withFilter(new CardNotPredicate(new CardColorPredicate(CardColor.BLACK))),
                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER));"""),
    ], [LOSE, LOSE_R], ["model.effect.CasterLosesLifeOnSpellCastEffect"]),
    "TreacherousBlessing": ("t", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new CasterLosesLifeOnSpellCastEffect(null, 1));""",
         """        addTrigger(Triggers.youCastSpell().anySpell(), new LoseLifeEffect(1));"""),
    ], [LOSE], ["model.effect.CasterLosesLifeOnSpellCastEffect"]),
    "TevalArbiterOfVirtue": ("t", [
        ("""        addTrigger(Triggers.youCastSpell(),
                CasterLosesLifeOnSpellCastEffect.equalToSpellManaValue(null));""",
         """        addTrigger(Triggers.youCastSpell().anySpell(),
                new LoseLifeEffect(new TargetSpellManaValue(), LoseLifeRecipient.CONTROLLER));"""),
    ], ["model.amount.TargetSpellManaValue", LOSE, LOSE_R], ["model.effect.CasterLosesLifeOnSpellCastEffect"]),
    "CurseOfWizardry": ("c", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CasterLosesLifeOnChosenColorSpellCastEffect(1));""",
         """        addTrigger(Triggers.playerCastsSpell().withFilter(new CardHasSourceChosenColorPredicate()),
                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER));"""),
    ], ["model.filter.CardHasSourceChosenColorPredicate", LOSE, LOSE_R],
        ["model.effect.CasterLosesLifeOnChosenColorSpellCastEffect"]),
    "Havoc": ("h", [
        ("""        addTrigger(Triggers.opponentCastsSpell(),
                new SpellCastLifeDrainEffect(2, 0, new CardColorPredicate(CardColor.WHITE)));""",
         """        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardColorPredicate(CardColor.WHITE)),
                new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER));"""),
    ], [LOSE, LOSE_R], ["model.effect.SpellCastLifeDrainEffect"]),
    "KambalConsulOfAllocation": ("k", [
        ("""        addTrigger(Triggers.opponentCastsSpell(),
                new SpellCastLifeDrainEffect(2, 2,
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));""",
         """        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER),
                new GainLifeEffect(2));"""),
    ], ["model.effect.GainLifeEffect", LOSE, LOSE_R], ["model.effect.SpellCastLifeDrainEffect"]),
    "YawgmothsEdict": ("y", [
        ("""        addTrigger(Triggers.opponentCastsSpell(),
                new SpellCastLifeDrainEffect(1, 1, new CardColorPredicate(CardColor.WHITE)));""",
         """        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardColorPredicate(CardColor.WHITE)),
                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER),
                new GainLifeEffect(1));"""),
    ], ["model.effect.GainLifeEffect", LOSE, LOSE_R], ["model.effect.SpellCastLifeDrainEffect"]),
    "AetherSting": ("a", [
        ("""        addTrigger(Triggers.opponentCastsSpell(),
                new SpellCastDamageToCasterEffect(1, new CardTypePredicate(CardType.CREATURE)));""",
         """        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER));"""),
    ], [DMG, DMG_R], ["model.effect.SpellCastDamageToCasterEffect"]),
    "AdrenalineJockey": ("a", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                SpellCastDamageToCasterEffect.whenCasterIsNotActiveTurn(4));""",
         """        // Whenever a player casts a spell, if it's not their turn, this creature deals 4 damage to them.
        addTrigger(Triggers.playerCastsSpell().notDuringCastersTurn(),
                new DealDamageToPlayersEffect(4, DamageRecipient.TRIGGERING_PLAYER));"""),
    ], [DMG, DMG_R], ["model.effect.SpellCastDamageToCasterEffect"]),
    "ScytheclawRaptor": ("s", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                SpellCastDamageToCasterEffect.whenCasterIsNotActiveTurn(4));""",
         """        // Whenever a player casts a spell, if it's not their turn, this creature deals 4 damage to them.
        addTrigger(Triggers.playerCastsSpell().notDuringCastersTurn(),
                new DealDamageToPlayersEffect(4, DamageRecipient.TRIGGERING_PLAYER));"""),
    ], [DMG, DMG_R], ["model.effect.SpellCastDamageToCasterEffect"]),
    "Cindervines": ("c", [
        ("""        addTrigger(Triggers.opponentCastsSpell(),
                new SpellCastDamageToCasterEffect(1, new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));""",
         """        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER));"""),
    ], [], ["model.effect.SpellCastDamageToCasterEffect"]),
    "IshiIshiAkkiCrackshot": ("i", [
        ("""        addTrigger(Triggers.opponentCastsSpell(), new SpellCastDamageToCasterEffect(2,
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardSubtypePredicate(CardSubtype.ARCANE)))));""",
         """        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardSubtypePredicate(CardSubtype.ARCANE)))),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER));"""),
    ], [DMG, DMG_R], ["model.effect.SpellCastDamageToCasterEffect"]),
    "Mindsparker": ("m", [
        ("""        addTrigger(Triggers.opponentCastsSpell(), new SpellCastDamageToCasterEffect(2,
                new CardAllOfPredicate(List.of(
                        new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.WHITE),
                                new CardColorPredicate(CardColor.BLUE))),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))))));""",
         """        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardAllOfPredicate(List.of(
                        new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.WHITE),
                                new CardColorPredicate(CardColor.BLUE))),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))))),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER));"""),
    ], [DMG, DMG_R], ["model.effect.SpellCastDamageToCasterEffect"]),
    "MagebaneLizard": ("m", [
        ("""        addTrigger(Triggers.playerCastsSpell(), new SpellCastDamageToCasterEffect(
                new SpellsCastThisTurn(noncreatureSpell, CountScope.TARGET_PLAYER), noncreatureSpell));""",
         """        addTrigger(Triggers.playerCastsSpell().withFilter(noncreatureSpell), new DealDamageToPlayersEffect(
                new SpellsCastThisTurn(noncreatureSpell, CountScope.TARGET_PLAYER), DamageRecipient.TRIGGERING_PLAYER));"""),
    ], [DMG, DMG_R], ["model.effect.SpellCastDamageToCasterEffect"]),
    "ScabClanBerserker": ("s", [
        ("""        addTrigger(Triggers.opponentCastsSpell(), new SpellCastDamageToCasterEffect(2,
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), new SourceIsRenowned()));""",
         """        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsRenowned()),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER));"""),
    ], ["model.trigger.TriggerOptions", DMG, DMG_R], ["model.effect.SpellCastDamageToCasterEffect"]),
    "NantukoShrine": ("n", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect());""",
         """        // Whenever a player casts a spell, that player creates X 1/1 green Squirrel creature tokens, where X is the
        // number of cards in all graveyards with the same name as that spell.
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new CreateTokenForTriggeringPlayerEffect(new CreateTokenEffect(
                        new CardsInGraveyard(new CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER),
                        "Squirrel", 1, 1, CardColor.GREEN, List.of(CardSubtype.SQUIRREL), Set.of(), Set.of())));"""),
    ], ["model.CardColor", "model.CardSubtype", IN_GRAVEYARDS, SCOPE, "model.effect.CreateTokenEffect",
        "model.effect.CreateTokenForTriggeringPlayerEffect", SAME_NAME, "java:java.util.List", "java:java.util.Set"],
        ["model.effect.CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect"]),
    "DwarvenShrine": ("d", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect());""",
         """        addTrigger(Triggers.playerCastsSpell().anySpell(), new DealDamageToPlayersEffect(
                new Scaled(new CardsInGraveyard(new CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER), 2),
                DamageRecipient.TRIGGERING_PLAYER));"""),
    ], [IN_GRAVEYARDS, SCOPE, "model.amount.Scaled", DMG, DMG_R, SAME_NAME],
        ["model.effect.DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect"]),
    "AvenShrine": ("a", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect());""",
         """        addTrigger(Triggers.playerCastsSpell().anySpell(), new GainLifeEffect(
                new CardsInGraveyard(new CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER),
                GainLifeRecipient.TRIGGERING_PLAYER));"""),
    ], [IN_GRAVEYARDS, SCOPE, "model.effect.GainLifeEffect", "model.effect.GainLifeRecipient", SAME_NAME],
        ["model.effect.GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect"]),
    "CabalShrine": ("c", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new DiscardForSameNameCardsInGraveyardsOnSpellCastEffect());""",
         """        addTrigger(Triggers.playerCastsSpell().anySpell(), new DiscardEffect(
                new CardsInGraveyard(new CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER),
                DiscardRecipient.TRIGGERING_PLAYER));"""),
    ], [IN_GRAVEYARDS, SCOPE, "model.effect.DiscardEffect", "model.effect.DiscardRecipient", SAME_NAME],
        ["model.effect.DiscardForSameNameCardsInGraveyardsOnSpellCastEffect"]),
    "InfernalKirin": ("i", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new DiscardAllCardsWithCastSpellManaValueEffect(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardSubtypePredicate(CardSubtype.ARCANE)
                ))));""",
         """        // "With that spell's mana value": the ability's X, which the cast collector makes the spell's mana value.
        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardSubtypePredicate(CardSubtype.ARCANE)
                ))),
                new RevealHandAndDiscardMatchingCardsEffect(new CardManaValueEqualsXPredicate()));"""),
    ], ["model.effect.RevealHandAndDiscardMatchingCardsEffect", "model.filter.CardManaValueEqualsXPredicate"],
        ["model.effect.DiscardAllCardsWithCastSpellManaValueEffect"]),
    "CelestialKirin": ("c", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new DestroyAllPermanentsWithCastSpellManaValueEffect(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardSubtypePredicate(CardSubtype.ARCANE)
                ))));""",
         """        // "With that spell's mana value": the ability's X, which the cast collector makes the spell's mana value.
        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardSubtypePredicate(CardSubtype.ARCANE)
                ))),
                new DestroyAllPermanentsEffect(new PermanentManaValueEqualsXPredicate()));"""),
    ], ["model.effect.DestroyAllPermanentsEffect", "model.filter.PermanentManaValueEqualsXPredicate"],
        ["model.effect.DestroyAllPermanentsWithCastSpellManaValueEffect"]),
    "VeiledSentry": ("v", [
        ("""        addTrigger(Triggers.opponentCastsSpell(), new BecomeCreatureByCastSpellManaValueEffect());""",
         """        // When an opponent casts a spell, if this permanent is an enchantment, it becomes an Illusion creature with
        // power and toughness each equal to that spell's mana value.
        addTrigger(Triggers.opponentCastsSpell().anySpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()),
                new BecomeCreatureEffect(new TargetSpellManaValue(), new TargetSpellManaValue(), CardSubtype.ILLUSION));"""),
    ], ["model.CardSubtype", "model.amount.TargetSpellManaValue", "model.condition.SourceIsEnchantment",
        "model.effect.BecomeCreatureEffect", "model.trigger.TriggerOptions"],
        ["model.effect.BecomeCreatureByCastSpellManaValueEffect"]),
    "MagmabloodArchaic": ("m", [
        ("""        addTrigger(Triggers.youCastSpell(),
                new BoostAllOwnCreaturesByColorsSpentOnSpellCastEffect(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                ))));""",
         """        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                ))),
                new BoostAllOwnCreaturesEffect(new ColorsSpentToCast(), new Fixed(0)));"""),
    ], ["model.amount.ColorsSpentToCast", "model.amount.Fixed", "model.effect.BoostAllOwnCreaturesEffect"],
        ["model.effect.BoostAllOwnCreaturesByColorsSpentOnSpellCastEffect"]),
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
