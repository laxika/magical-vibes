package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "134")
public class BrazenDwarf extends Card {

    public BrazenDwarf() {
        addTrigger(Triggers.youRollOneOrMoreDice(),
                DealDamageEffect.to(Subjects.eachOpponent(), 1));
    }
}
