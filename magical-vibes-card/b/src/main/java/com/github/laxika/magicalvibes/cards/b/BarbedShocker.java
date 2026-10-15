package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsHandThenDrawsThatManyEffect;

@CardRegistration(set = "TSP", collectorNumber = "144")
public class BarbedShocker extends Card {

    public BarbedShocker() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                new TargetPlayerDiscardsHandThenDrawsThatManyEffect());
    }
}
