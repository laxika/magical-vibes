package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "ONE", collectorNumber = "15")
public class IncisorGlider extends Card {

    public IncisorGlider() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new OpponentPoisoned(3),
                new BoostAllOwnCreaturesEffect(1, 1)));
    }
}
