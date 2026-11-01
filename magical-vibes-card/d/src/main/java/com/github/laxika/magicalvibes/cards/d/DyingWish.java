package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "64")
public class DyingWish extends Card {

    public DyingWish() {
        target(TargetFilters.creatureYouControl());
        addTrigger(Triggers.dies(ObjectRelation.ENCHANTED), SequenceEffect.of(
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER),
                new GainLifeEffect(new EventValue())));
    }
}
