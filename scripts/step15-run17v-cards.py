#!/usr/bin/env python3
"""Step 15, run 17v: the "enchanted creature dies / leaves" and death-collector rebuilds that baked the dying
creature's last-known power, toughness or controller into the effect, written as existing effects that read the stack
entry's triggering permanent (the creature as it last existed on the battlefield, CR 603.10a).

- Banewasp Affliction (ALA), Decomposition (MIR): "that creature's controller loses life equal to its toughness" /
  "its controller loses 2 life" is the existing `LoseLifeEffect` with `TRIGGERING_PERMANENT_CONTROLLER` (and a
  `TriggeringPermanentToughness` amount), not `EnchantedCreatureControllerLosesLifeEffect`.
- Death Watch (VIS): "its controller loses life equal to its power and you gain life equal to its toughness" is one
  ability of `LoseLifeEffect(TriggeringPermanentPower, TRIGGERING_PERMANENT_CONTROLLER)` and
  `GainLifeEffect(TriggeringPermanentToughness)`, not a marker the collector rebuilt.
- Fruit of the First Tree (FRF): "you gain X life and draw X cards, where X is its toughness" is one ability of
  `GainLifeEffect` and `DrawCardEffect` with a `TriggeringPermanentToughness` amount.
- Creature Bond (4ED): "deals damage equal to that creature's toughness to the creature's controller" is
  `DealDamageToPlayersEffect(TriggeringPermanentToughness, TRIGGERING_PERMANENT_CONTROLLER)`.
- Dead Man's Chest (RIX): "exile cards equal to its power" is the effect's `TriggeringPermanentPower` count, its owner
  read from the entry.
- Funeral March (5ED): "its controller sacrifices a creature of their choice" is the existing
  `SacrificePermanentsEffect` with `TRIGGERING_PERMANENT_CONTROLLER`.
- Kresh the Bloodbraided (ALA), Vincent Valentine (FIN): "put X +1/+1 counters on this, where X is that creature's
  power" is the existing `PutCountersOnSelfEffect` with a `TriggeringPermanentPower` amount (Kresh's behind its "you
  may"), not `PutCountersOnSourceEqualToDyingPowerEffect`.

Takklemaggot, Traveling Plague and Murder Investigation keep their constructors (their effects read the entry).

Idempotent.

Usage: step15-run17v-cards.py <letter module dir>...
"""
import pathlib
import sys

P = "import com.github.laxika.magicalvibes.model."


def imp(name):
    return P + name + ";\n"


TRIGGERS = imp("trigger.Triggers")

EDITS = {
    "BanewaspAffliction": [
        (imp("effect.EnchantedCreatureControllerLosesLifeEffect"),
         imp("amount.TriggeringPermanentToughness") + imp("effect.LoseLifeEffect") + imp("effect.LoseLifeRecipient")),
        ("""        // Enchant creature. When enchanted creature dies, that creature's controller loses life equal
        // to its toughness. The amount is baked from the dying creature's toughness at trigger time.
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new EnchantedCreatureControllerLosesLifeEffect(0));
""", """        // Enchant creature. When enchanted creature dies, that creature's controller loses life equal
        // to its toughness (the creature as it last existed on the battlefield).
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new LoseLifeEffect(new TriggeringPermanentToughness(),
                        LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
"""),
    ],
    "Decomposition": [
        (imp("effect.EnchantedCreatureControllerLosesLifeEffect"), ""),
        (imp("effect.GrantScope"), imp("effect.GrantScope") + imp("effect.LoseLifeEffect")
         + imp("effect.LoseLifeRecipient")),
        ("new EnchantedCreatureControllerLosesLifeEffect(2)",
         "new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)"),
    ],
    "DeathWatch": [
        (imp("effect.EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect"),
         imp("amount.TriggeringPermanentPower") + imp("amount.TriggeringPermanentToughness")
         + imp("effect.GainLifeEffect") + imp("effect.LoseLifeEffect") + imp("effect.LoseLifeRecipient")
         + imp("effect.SequenceEffect")),
        ("""        // Enchant creature. When enchanted creature dies, its controller loses life equal to its
        // power and you gain life equal to its toughness. Both amounts are last-known information
        // baked at trigger time onto one stack entry (loss then gain).
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect());
""", """        // Enchant creature. When enchanted creature dies, its controller loses life equal to its
        // power and you gain life equal to its toughness: one ability, the creature as it last
        // existed on the battlefield.
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                SequenceEffect.of(
                        new LoseLifeEffect(new TriggeringPermanentPower(),
                                LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER),
                        new GainLifeEffect(new TriggeringPermanentToughness())));
"""),
    ],
    "FruitOfTheFirstTree": [
        (imp("effect.EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect"),
         imp("amount.TriggeringPermanentToughness") + imp("effect.DrawCardEffect") + imp("effect.GainLifeEffect")
         + imp("effect.SequenceEffect")),
        ("""                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect());
""", """                // When enchanted creature dies, you gain X life and draw X cards, where X is its toughness.
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED), SequenceEffect.of(
                        new GainLifeEffect(new TriggeringPermanentToughness()),
                        new DrawCardEffect(new TriggeringPermanentToughness())));
"""),
    ],
    "CreatureBond": [
        (imp("Card"), imp("Card") + imp("amount.TriggeringPermanentToughness")),
        ("""        // Enchant creature. When enchanted creature dies, this Aura deals damage equal to that
        // creature's toughness to the creature's controller. The creature is gone by resolution, so
        // the death collector bakes its last-known toughness as the amount; ENCHANTED_PERMANENT_CONTROLLER
        // routes the damage to that creature's controller (baked as the stack targetId).
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new DealDamageToPlayersEffect(0, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
""", """        // Enchant creature. When enchanted creature dies, this Aura deals damage equal to that
        // creature's toughness to the creature's controller (the creature as it last existed on the
        // battlefield).
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new DealDamageToPlayersEffect(new TriggeringPermanentToughness(),
                        DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
"""),
    ],
    "DeadMansChest": [
        (imp("Card"), imp("Card") + imp("amount.TriggeringPermanentPower")),
        ("new ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect()",
         "new ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect(new TriggeringPermanentPower())"),
    ],
    "FuneralMarch": [
        (imp("effect.EnchantedControllerSacrificesCreatureOnLeaveEffect"),
         imp("effect.SacrificePermanentsEffect") + imp("effect.SacrificeRecipient")
         + imp("filter.PermanentIsCreaturePredicate")),
        ("new EnchantedControllerSacrificesCreatureOnLeaveEffect()",
         "new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),\n"
         "                        SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER)"),
    ],
    "KreshTheBloodbraided": [
        (imp("effect.PutCountersOnSourceEqualToDyingPowerEffect"),
         imp("CounterType") + imp("amount.TriggeringPermanentPower") + imp("effect.MayEffect")
         + imp("effect.PutCountersOnSelfEffect")),
        ("""        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER),
                new PutCountersOnSourceEqualToDyingPowerEffect(1, 1, true));
""", """        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new TriggeringPermanentPower()),
                        "Put +1/+1 counters equal to that creature's power on Kresh the Bloodbraided?"));
"""),
    ],
    "VincentValentine": [
        (imp("effect.PutCountersOnSourceEqualToDyingPowerEffect"),
         imp("effect.PutCountersOnSelfEffect")),
        (imp("Card"), imp("Card") + imp("CounterType") + imp("amount.TriggeringPermanentPower")),
        ("new PutCountersOnSourceEqualToDyingPowerEffect(1, 1, false)",
         "new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new TriggeringPermanentPower())"),
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
                    text = text.replace(old, new, 1)
                if text != original:
                    path.write_text(text)
                    print(f"converted {path}")


if __name__ == "__main__":
    main(sys.argv[1:])
