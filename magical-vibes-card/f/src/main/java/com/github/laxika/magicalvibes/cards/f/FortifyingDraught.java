package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "STX", collectorNumber = "132")
public class FortifyingDraught extends Card {

    public FortifyingDraught() {
        // You gain 2 life. Target creature gets +X/+X until end of turn, where X is the amount
        // of life you gained this turn.
        addSpellEffect(new GainLifeEffect(2));
        var lifeGained = new LifeGainedThisTurn(CountScope.CONTROLLER);
        addSpellEffect(new BoostTargetCreatureEffect(lifeGained, lifeGained));
    }
}
