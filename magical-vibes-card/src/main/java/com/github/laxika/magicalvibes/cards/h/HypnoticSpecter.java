package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RandomDiscardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "151")
public class HypnoticSpecter extends Card {

    public HypnoticSpecter() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new RandomDiscardEffect());
    }
}
