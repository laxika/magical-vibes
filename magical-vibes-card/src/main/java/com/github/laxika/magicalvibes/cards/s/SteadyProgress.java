package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "SOM", collectorNumber = "45")
public class SteadyProgress extends Card {

    public SteadyProgress() {
        addEffect(EffectSlot.SPELL, new ProliferateEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
