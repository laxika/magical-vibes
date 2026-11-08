#!/usr/bin/env python3
"""Step 15, run 17v2: the enchanted-death, enchanted-leaves and death-collector rebuilds of row 17v2, written as the
cards' own abilities whose effects read the stack entry's triggering card or permanent.

- Mask of Griselbrand (MID): "you may pay X life, where X is its power. If you do, draw X cards" is the existing
  `MayPayLifeEqualToAmountEffect` around `DrawCardEffect`, both with a `TriggeringPermanentPower` amount, not
  `MayPayLifeAndDrawEqualToDyingPowerEffect`.
- Faces of the Past (SCG): "tap all untapped creatures that share a creature type with it or untap all tapped creatures
  that share a creature type with it" is `ChooseOneAtResolutionEffect` of the existing `TapPermanentsEffect` /
  `UntapPermanentsEffect` with the new `PermanentSharesCreatureTypeWithTriggeringPermanentPredicate`.
- Hateful Eidolon (THB): "whenever an enchanted creature dies" is the trigger's filter (`PermanentIsEnchantedPredicate`),
  "draw a card for each Aura you controlled that was attached to it" `DrawCardEffect` with the new
  `AurasAttachedToTriggeringPermanent(true)` amount.
- Reins of the Vinesteed (MOR): its "you may return this card" is the card's own `MayEffect`, which the collector wrapped
  around the effect.
- Journey to Eternity (RIX): "return it to the battlefield under your control, then return Journey to Eternity to the
  battlefield transformed" is one `SequenceEffect` of the existing `ReturnEnchantedCreatureToBattlefieldOnDeathEffect`
  and `ReturnSourceTransformedFromGraveyardEffect`, not `ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect`.
- Kaya's Ghostform (WAR): "when enchanted creature or planeswalker dies or is put into exile" is the trigger
  (`Triggers.putIntoGraveyardOrExileFromBattlefield`), not a leaves trigger whose collector skipped other zones.
- The `ReturnEnchantedCreatureToOwnerHandOnDeathEffect` cards (Demonic Vigor, Squee's Embrace, Puppet Master,
  Tezzeret's Touch and the six Zendikons): the deleted class-keyed handler triggered only if the enchanted permanent was
  a creature as it died; that gate is the trigger's creature filter (`Triggers.creatureDies(ENCHANTED)`).

Idempotent.

Usage: step15-run17v2-cards.py <letter module dir>...
"""
import pathlib
import sys

P = "import com.github.laxika.magicalvibes.model."


def imp(name):
    return P + name + ";\n"


EDITS = {
    "MaskOfGriselbrand": [
        (imp("effect.MayPayLifeAndDrawEqualToDyingPowerEffect"),
         imp("amount.TriggeringPermanentPower") + imp("effect.DrawCardEffect")
         + imp("effect.MayPayLifeEqualToAmountEffect")),
        ("        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED), new MayPayLifeAndDrawEqualToDyingPowerEffect());\n",
         """        // Whenever equipped creature dies, you may pay X life, where X is its power. If you do, draw X cards.
        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED),
                new MayPayLifeEqualToAmountEffect(new TriggeringPermanentPower(),
                        new DrawCardEffect(new TriggeringPermanentPower()),
                        "Pay life equal to its power to draw that many cards?"));
"""),
    ],
    "FacesOfThePast": [
        (imp("effect.TapOrUntapCreaturesSharingDyingCreatureTypeEffect"),
         imp("effect.ChooseOneAtResolutionEffect") + imp("effect.ChooseOneEffect")
         + imp("effect.TapPermanentsEffect") + imp("effect.TapUntapScope") + imp("effect.UntapPermanentsEffect")
         + imp("filter.PermanentSharesCreatureTypeWithTriggeringPermanentPredicate")),
        (imp("trigger.Triggers"), imp("trigger.Triggers") + "\nimport java.util.List;\n"),
        ("""        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new TapOrUntapCreaturesSharingDyingCreatureTypeEffect());
""", """        // Whenever a creature dies, tap all untapped creatures that share a creature type with it or untap all
        // tapped creatures that share a creature type with it (chosen as the ability resolves).
        var sharesTypeWithIt = new PermanentSharesCreatureTypeWithTriggeringPermanentPredicate();
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new ChooseOneAtResolutionEffect(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Tap all untapped creatures that share a creature type with it",
                                new TapPermanentsEffect(TapUntapScope.ALL_CREATURES, sharesTypeWithIt)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Untap all tapped creatures that share a creature type with it",
                                new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES, sharesTypeWithIt))))));
"""),
    ],
    "HatefulEidolon": [
        (imp("effect.DrawCardForEachAuraAttachedToDyingCreatureEffect"),
         imp("amount.AurasAttachedToTriggeringPermanent") + imp("effect.DrawCardEffect")
         + imp("filter.PermanentIsEnchantedPredicate")),
        ("""        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new DrawCardForEachAuraAttachedToDyingCreatureEffect());
""", """        // Whenever an enchanted creature dies, draw a card for each Aura you controlled that was attached to it.
        addTrigger(Triggers.creatureDies(new PermanentIsEnchantedPredicate(), ObjectRelation.ANY),
                new DrawCardEffect(new AurasAttachedToTriggeringPermanent(true)));
"""),
    ],
    "ReinsOfTheVinesteed": [
        (imp("effect.GrantScope"), imp("effect.GrantScope") + imp("effect.MayEffect")),
        ("""                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnSourceAuraToSharedTypeCreatureOnDeathEffect());
""", """                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new MayEffect(new ReturnSourceAuraToSharedTypeCreatureOnDeathEffect(),
                                "return it to the battlefield attached to a creature that shares a creature type"
                                        + " with the creature that died?"));
"""),
    ],
    "JourneyToEternity": [
        (imp("effect.ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect"),
         imp("effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect")
         + imp("effect.ReturnSourceTransformedFromGraveyardEffect") + imp("effect.SequenceEffect")),
        ("""                        new ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect());""",
         """                        SequenceEffect.of(new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true),
                                new ReturnSourceTransformedFromGraveyardEffect()));"""),
    ],
    "KayasGhostform": [
        ("Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED)",
         "Triggers.putIntoGraveyardOrExileFromBattlefield(null, ObjectRelation.ENCHANTED)"),
    ],
}

OWNER_HAND = ["DemonicVigor", "SqueesEmbrace", "PuppetMaster", "TezzeretsTouch", "CrusherZendikon",
              "CorruptedZendikon", "GuardianZendikon", "NissasZendikon", "VastwoodZendikon", "WindZendikon"]
for owner_hand_card in OWNER_HAND:
    EDITS[owner_hand_card] = [("Triggers.dies(ObjectRelation.ENCHANTED)",
                               "Triggers.creatureDies(ObjectRelation.ENCHANTED)")]


def main(dirs):
    for d in dirs:
        root = pathlib.Path(d)
        for name, edits in EDITS.items():
            for path in root.rglob(f"cards/*/{name}.java"):
                text = path.read_text()
                original = text
                done = edits[-1][1] in text
                if not done:
                    for old, new in edits:
                        if old not in text:
                            raise SystemExit(f"{path}: expected text not found: {old!r}")
                        text = text.replace(old, new, 1)
                if text != original:
                    path.write_text(text)
                    print(f"converted {path}")


if __name__ == "__main__":
    main(sys.argv[1:])
