package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaThenDrawsEffect;

@CardRegistration(set = "CMD", collectorNumber = "51")
public class MindsAglow extends Card {

    public MindsAglow() {
        addEffect(EffectSlot.SPELL, new EachPlayerPaysAnyManaThenDrawsEffect());
    }
}
