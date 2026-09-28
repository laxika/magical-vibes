package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.AddFoodTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MSC", collectorNumber = "734")
public class TippyToeTerrificPartner extends Card {

    public TippyToeTerrificPartner() {
        addEffect(EffectSlot.STATIC, new AddFoodTokenToTokenCreationEffect());
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(),
                new DrawCardEffect(1)));
    }
}
