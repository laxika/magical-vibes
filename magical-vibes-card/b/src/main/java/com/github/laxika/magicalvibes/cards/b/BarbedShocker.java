package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsHandThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "144")
public class BarbedShocker extends Card {

    public BarbedShocker() {
        addTrigger(Triggers.dealsDamageToPlayer(),
                new TargetPlayerDiscardsHandThenDrawsThatManyEffect());
    }
}
