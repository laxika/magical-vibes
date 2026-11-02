package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "125")
public class EndriderSpikespitter extends Card {

    public EndriderSpikespitter() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), ConditionalEffect.atTriggerTime(
                new MaxSpeed(), new ExileTopCardMayPlayThisTurnEffect(false)));
    }
}
