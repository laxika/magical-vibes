package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.Channel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Yavimaya Bloomsage // Channel (SOC 44). */
@CardRegistration(set = "SOC", collectorNumber = "44")
@CardRegistration(set = "SOC", collectorNumber = "92")
public class YavimayaBloomsageChannel extends Card {

    public YavimayaBloomsageChannel() {
        setBackFaceCard(new Channel());

        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.endStep(ActivePlayer.YOU), SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new ConditionalEffect(
                                new TargetPermanentMatches(new PermanentPowerAtLeastPredicate(7)),
                                new BecomePreparedEffect(), false)));
    }

    @Override
    public String getBackFaceClassName() {
        return "Channel";
    }
}
