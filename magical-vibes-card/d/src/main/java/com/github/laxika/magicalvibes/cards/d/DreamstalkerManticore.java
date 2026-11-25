package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "131")
public class DreamstalkerManticore extends Card {

    public DreamstalkerManticore() {
        addTrigger(Triggers.youCastSpell().duringOpponentsTurn().nthThisTurn(1), DealDamageEffect.toAnyTarget(1));
    }
}
