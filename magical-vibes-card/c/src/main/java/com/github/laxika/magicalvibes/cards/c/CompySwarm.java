package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;

@CardRegistration(set = "REX", collectorNumber = "9")
@CardRegistration(set = "REX", collectorNumber = "34")
public class CompySwarm extends Card {

    public CompySwarm() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new Morbid(), CreateTokenCopyOfSourceEffect.tapped(1)));
    }
}
