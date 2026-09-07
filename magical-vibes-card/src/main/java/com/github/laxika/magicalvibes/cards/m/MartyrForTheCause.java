package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "WAR", collectorNumber = "23")
public class MartyrForTheCause extends Card {

    public MartyrForTheCause() {
        addEffect(EffectSlot.ON_DEATH, new ProliferateEffect());
    }
}
