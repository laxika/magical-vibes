package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "OTJ", collectorNumber = "98")
public class PitilessCarnage extends Card {

    public PitilessCarnage() {
        addEffect(EffectSlot.SPELL, new SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect(
                new PermanentTruePredicate()));
    }
}
