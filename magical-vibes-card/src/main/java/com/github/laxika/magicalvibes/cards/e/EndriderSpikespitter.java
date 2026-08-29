package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "DFT", collectorNumber = "125")
public class EndriderSpikespitter extends Card {

    public EndriderSpikespitter() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new MaxSpeed(), new ExileTopCardMayPlayThisTurnEffect(false)));
    }
}
