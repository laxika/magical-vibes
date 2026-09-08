package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "EOE", collectorNumber = "94")
public class DecodeTransmissions extends Card {

    public DecodeTransmissions() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new VoidCondition(),
                SequenceEffect.of(
                        new DrawCardEffect(2),
                        new LoseLifeEffect(2, LoseLifeRecipient.CONTROLLER)),
                SequenceEffect.of(
                        new DrawCardEffect(2),
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT))));
    }
}
