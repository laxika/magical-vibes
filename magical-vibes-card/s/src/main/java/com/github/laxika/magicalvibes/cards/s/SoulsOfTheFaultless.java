package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "131")
public class SoulsOfTheFaultless extends Card {

    public SoulsOfTheFaultless() {
        addTrigger(Triggers.dealtCombatDamage(), SequenceEffect.of(
                new GainLifeEffect(new EventValue()),
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.ACTIVE_PLAYER)));
    }
}
