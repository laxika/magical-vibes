package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberEffect;

@CardRegistration(set = "ODY", collectorNumber = "99")
public class RitesOfRefusal extends Card {

    public RitesOfRefusal() {
        addEffect(EffectSlot.SPELL, new DiscardAnyNumberEffect());
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(new Scaled(new EventValue(), 3)));
    }
}
