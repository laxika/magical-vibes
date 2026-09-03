package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "145")
public class FightRigging extends Card {

    public FightRigging() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ImprintFromTopCardsEffect(5, true));

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new ConditionalEffect(
                                new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentPowerAtLeastPredicate(7)
                                ))),
                                new PlayImprintedCardWithoutPayingManaCostEffect())
                ));
    }
}
