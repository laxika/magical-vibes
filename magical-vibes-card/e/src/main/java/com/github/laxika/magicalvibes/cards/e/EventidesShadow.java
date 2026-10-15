package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RemoveAnyNumberOfCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ECC", collectorNumber = "8")
@CardRegistration(set = "ECC", collectorNumber = "28")
public class EventidesShadow extends Card {

    public EventidesShadow() {
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new RemoveAnyNumberOfCountersFromAllPermanentsEffect(),
                new DrawCardEffect(new EventValue()),
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER)));
    }
}
