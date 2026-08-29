package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "42")
public class Renounce extends Card {

    public Renounce() {
        addEffect(EffectSlot.SPELL, new SacrificeAnyNumberOfPermanentsEffect(new PermanentTruePredicate()));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(new EventValue(), 2)));
    }
}
