package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.CastSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "C21", collectorNumber = "68")
public class SproutbackTrudge extends Card {

    public SproutbackTrudge() {
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new LifeGainedThisTurn(CountScope.CONTROLLER)));
        addEffect(EffectSlot.GRAVEYARD_CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new GainedLifeThisTurn(), new CastSourceCardFromGraveyardEffect()));
    }
}
