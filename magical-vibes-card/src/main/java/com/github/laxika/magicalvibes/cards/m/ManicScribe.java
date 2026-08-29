package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "SOI", collectorNumber = "73")
public class ManicScribe extends Card {

    public ManicScribe() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(3, MillRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new ConditionalEffect(new Delirium(), new MillEffect(3, MillRecipient.TARGET_PLAYER)));
    }
}
