package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "175")
public class CoriMountainStalwart extends Card {

    public CoriMountainStalwart() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                DealDamageEffect.to(Subjects.eachOpponent(), 2),
                new GainLifeEffect(2));
    }
}
