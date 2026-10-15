package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsThenAddManaPerSacrificedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

/**
 * Mana Seism — {1}{R} sorcery: "Sacrifice any number of lands, then add that much {C}."
 */
@CardRegistration(set = "CHK", collectorNumber = "179")
public class ManaSeism extends Card {

    public ManaSeism() {
        addEffect(EffectSlot.SPELL, new SacrificeAnyNumberOfPermanentsThenAddManaPerSacrificedEffect(
                new PermanentIsLandPredicate(), ManaColor.COLORLESS));
    }
}
