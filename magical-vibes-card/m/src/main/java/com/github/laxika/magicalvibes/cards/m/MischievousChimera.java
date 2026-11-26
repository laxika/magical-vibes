package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "223")
public class MischievousChimera extends Card {

    public MischievousChimera() {
        addTrigger(Triggers.youCastSpell().duringOpponentsTurn().nthThisTurn(1),
                DealDamageEffect.to(Subjects.eachOpponent(), 1),
                new ScryEffect(1));
    }
}
