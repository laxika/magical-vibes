#!/usr/bin/env python3
"""Step 15, run 17u: the rebuilds that bound event data as the ability triggered, written as existing effects that read
the stack entry.

- "Its controller loses N life" of an enchanted creature that attacks, blocks or is dealt damage (Contaminated Bond
  UDS, Nettling Curse ONS, Sinister Possession MIR, Parasitic Impetus LGN, Scriv's Contract (the Aura token of Scriv,
  the Obligator), Ragged Veins MMQ): the existing `LoseLifeEffect` with `TRIGGERING_PERMANENT_CONTROLLER` (the
  creature's controller as the ability resolves, or as it last existed), not `EnchantedCreatureControllerLosesLifeEffect`
  with the controller the collectors baked in; Ragged Veins' "that much" is the event value, the damage dealt.
- "At the beginning of the upkeep of enchanted creature's controller, that player loses N life" (Numbing Dose, One
  Thousand Lashes, Stab Wound, Soul Bleed): the existing `LoseLifeEffect` with `TRIGGERING_PLAYER`, the upkeep's player
  the step reader records as the entry's non-targeting target.
- Grappling Hook (ZEN): "have target creature block it" is the equipped creature, the stack entry's triggering
  permanent (`MustBlockSourceEffect.ofTriggeringPermanent()`), not a source the collector rebound.
- Deep-Sea Kraken (TSP): "whenever an opponent casts a spell, if this card is suspended, remove a time counter from it"
  is the suspended card's ability with its intervening "if" (as Pardic Dragon writes it) and the existing
  `RemoveTimeCounterFromExiledCardEffect`, not `RemoveTimeCounterWhenOpponentCastsSpellEffect`, which its handler
  rebuilt.
- Leshrac's Sigil (ICE): "whenever an opponent casts a green spell, you may pay {B}{B}. If you do, look at that
  player's hand and choose a card from it" is a filtered cast trigger whose "you may pay" goes on the stack as it is
  (CR 603.5), its hand look `ChooseCardsFromTargetHandEffect.ofTriggeringPlayer()` (the caster, not a target), not the
  authored `SpellCastTriggerEffect` record.

Idempotent.

Usage: step15-run17u-cards.py <letter module dir>...
"""
import pathlib
import sys

P = "import com.github.laxika.magicalvibes.model."
OLD_IMPORT = P + "effect.EnchantedCreatureControllerLosesLifeEffect;\n"
NEW_IMPORTS = P + "effect.LoseLifeEffect;\n" + P + "effect.LoseLifeRecipient;\n"


def lose_life(old, new):
    return [(OLD_IMPORT, NEW_IMPORTS), (old, new)]


def combat(amount):
    return lose_life(f"new EnchantedCreatureControllerLosesLifeEffect({amount})",
                     f"new LoseLifeEffect({amount}, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)")


def upkeep(amount):
    return lose_life(f"new EnchantedCreatureControllerLosesLifeEffect({amount})",
                     f"new LoseLifeEffect({amount}, LoseLifeRecipient.TRIGGERING_PLAYER)")


EDITS = {
    "ContaminatedBond": combat(3),
    "NettlingCurse": combat(3),
    "SinisterPossession": combat(2),
    "ParasiticImpetus": combat(2),
    "ScrivTheObligator": combat(2),
    "NumbingDose": upkeep(1),
    "OneThousandLashes": upkeep(1),
    "StabWound": upkeep(2),
    "SoulBleed": upkeep(1),
    "RaggedVeins": [
        (OLD_IMPORT, P + "amount.EventValue;\n" + NEW_IMPORTS),
        ("new EnchantedCreatureControllerLosesLifeEffect(0)",
         "new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)"),
    ],
    "GrapplingHook": [
        ("new MayEffect(new MustBlockSourceEffect(null),",
         "new MayEffect(MustBlockSourceEffect.ofTriggeringPermanent(),"),
    ],
    "DeepSeaKraken": [
        (P + "Card;\n", P + "Card;\n" + P + "condition.SourceCardSuspended;\n"),
        (P + "effect.RemoveTimeCounterWhenOpponentCastsSpellEffect;\n"
         + P + "trigger.Triggers;\n",
         P + "effect.RemoveTimeCounterFromExiledCardEffect;\n"
         + P + "trigger.TriggerOptions;\n" + P + "trigger.Triggers;\n"),
        ("        addTrigger(Triggers.opponentCastsSpell(), new RemoveTimeCounterWhenOpponentCastsSpellEffect());\n",
         "        // \"Whenever an opponent casts a spell, if this card is suspended, remove a time counter from it.\"\n"
         "        addTrigger(Triggers.opponentCastsSpell(),\n"
         "                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardSuspended()),\n"
         "                new RemoveTimeCounterFromExiledCardEffect(getId()));\n"),
    ],
    "LeshracsSigil": [
        (P + "effect.SpellCastTriggerEffect;\n", ""),
        ("""        // Whenever an opponent casts a green spell, you may pay {B}{B}. If you do, look at that
        // player's hand and choose a card from it. The player discards that card.
        // (Collector stamps the casting opponent as non-targeting targetId = "that player".)
        addTrigger(Triggers.opponentCastsSpell(),
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.GREEN),
                        List.of(new MayPayManaEffect("{B}{B}",
                                ChooseCardsFromTargetHandEffect.lookAtTargetHand(
                                        new Fixed(1), HandChoiceDestination.DISCARD),
                                "Pay {B}{B} to look at that player's hand and discard a card?"))));
""", """        // Whenever an opponent casts a green spell, you may pay {B}{B}. If you do, look at that
        // player's hand and choose a card from it. The player discards that card.
        // "That player" is the caster, the stack entry's non-targeting target.
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardColorPredicate(CardColor.GREEN)),
                new MayPayManaEffect("{B}{B}",
                        ChooseCardsFromTargetHandEffect.lookAtTargetHand(
                                new Fixed(1), HandChoiceDestination.DISCARD).ofTriggeringPlayer(),
                        "Pay {B}{B} to look at that player's hand and discard a card?"));
"""),
    ],
}


def main(dirs):
    for d in dirs:
        root = pathlib.Path(d)
        for name, edits in EDITS.items():
            for path in root.rglob(f"cards/*/{name}.java"):
                text = path.read_text()
                original = text
                for old, new in edits:
                    if new and new in text or not new and old not in text:
                        continue
                    if old not in text:
                        raise SystemExit(f"{path}: expected text not found: {old!r}")
                    text = text.replace(old, new)
                if text != original:
                    path.write_text(text)
                    print(f"converted {path}")


if __name__ == "__main__":
    main(sys.argv[1:])
