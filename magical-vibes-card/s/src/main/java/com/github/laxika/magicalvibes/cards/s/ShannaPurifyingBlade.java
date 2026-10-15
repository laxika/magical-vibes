package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.PayXManaDrawXCardsEffect;

@CardRegistration(set = "DMU", collectorNumber = "218")
public class ShannaPurifyingBlade extends Card {

    public ShannaPurifyingBlade() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PayXManaDrawXCardsEffect(new LifeGainedThisTurn(CountScope.CONTROLLER)));
    }
}
