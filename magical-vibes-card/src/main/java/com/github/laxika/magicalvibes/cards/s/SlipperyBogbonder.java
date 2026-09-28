package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MoveAnyCountersFromControlledCreaturesToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NCC", collectorNumber = "312")
public class SlipperyBogbonder extends Card {

    public SlipperyBogbonder() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.HEXPROOF, 1, new PermanentIsCreaturePredicate()),
                new MoveAnyCountersFromControlledCreaturesToTargetCreatureEffect()));
    }
}
