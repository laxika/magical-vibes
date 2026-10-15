package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerDiscardsHandThenDrawsThatManyEffect;

@CardRegistration(set = "MMQ", collectorNumber = "209")
public class RobberFly extends Card {

    public RobberFly() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DefendingPlayerDiscardsHandThenDrawsThatManyEffect());
    }
}
