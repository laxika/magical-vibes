package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerDiscardsHandThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "209")
public class RobberFly extends Card {

    public RobberFly() {
        addTrigger(Triggers.becomesBlocked(),
                new DefendingPlayerDiscardsHandThenDrawsThatManyEffect());
    }
}
