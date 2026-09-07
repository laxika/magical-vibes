package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GnarledGrovestrider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOW", collectorNumber = "198")
public class DormantGrove extends Card {

    public DormantGrove() {
        setBackFaceCard(new GnarledGrovestrider());

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        ConditionalEffect.unless(
                                new TargetPermanentMatches(new PermanentToughnessAtLeastPredicate(6)),
                                new TransformSelfEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "GnarledGrovestrider";
    }
}
