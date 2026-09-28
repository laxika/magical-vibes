package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MSC", collectorNumber = "649")
public class BobReluctantHYDRAAgent extends Card {

    public BobReluctantHYDRAAgent() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new AttacksAlone(),
                new ReturnSelfToHandThenEffect(SequenceEffect.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2)))));
    }
}
