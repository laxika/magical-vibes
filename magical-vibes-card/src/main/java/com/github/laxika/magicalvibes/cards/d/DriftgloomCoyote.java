package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BLB", collectorNumber = "11")
public class DriftgloomCoyote extends Card {

    public DriftgloomCoyote() {
        // When this creature enters, exile target creature an opponent controls until this
        // creature leaves the battlefield. If that creature had power 2 or less, put a +1/+1
        // counter on this creature.
        target(TargetFilters.creatureAnOpponentControls()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SequenceEffect.of(
                        ConditionalEffect.unless(
                                new TargetPermanentMatches(new PermanentPowerAtMostPredicate(2)),
                                new PutCountersOnSourceEffect(1, 1, 1)),
                        new ExileTargetPermanentUntilSourceLeavesEffect()));
    }
}
