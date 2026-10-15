package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "EOE", collectorNumber = "87")
public class AlpharaelStonechosen extends Card {

    public AlpharaelStonechosen() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessDiscardsEffect(true));
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new VoidCondition(),
                new LoseLifeEffect(
                        new HalvedRoundedUp(new TargetPlayerLifeTotal()),
                        LoseLifeRecipient.DEFENDING_PLAYER)));
    }
}
