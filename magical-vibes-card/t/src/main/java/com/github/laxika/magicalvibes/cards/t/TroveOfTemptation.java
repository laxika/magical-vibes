package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsMustAttackControllerEffect;

@CardRegistration(set = "XLN", collectorNumber = "171")
public class TroveOfTemptation extends Card {

    public TroveOfTemptation() {
        // Each opponent must attack you or a planeswalker you control with at least one creature each combat if able.
        addEffect(EffectSlot.STATIC, new OpponentsMustAttackControllerEffect());

        // At the beginning of your end step, create a Treasure token.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, CreateTokenEffect.ofTreasureToken(1));
    }
}
