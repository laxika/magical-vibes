package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "YBLB", collectorNumber = "23")
public class DazzlingFlameweaver extends Card {

    public DazzlingFlameweaver() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect(
                                List.of(
                                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.CardPrintingReference("RVR", "67"),
                                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.CardPrintingReference("NCC", "34"),
                                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.CardPrintingReference("RNA", "71"),
                                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.CardPrintingReference("RNA", "73"),
                                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.CardPrintingReference("RNA", "181"),
                                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.CardPrintingReference("RVR", "117"),
                                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.CardPrintingReference("RNA", "196"),
                                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.CardPrintingReference("RNA", "109"),
                                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.CardPrintingReference("OTP", "26"),
                                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.CardPrintingReference("RNA", "85"),
                                        new ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect.CardPrintingReference("RNA", "118")
                                )),
                        false,
                        true));
    }
}
