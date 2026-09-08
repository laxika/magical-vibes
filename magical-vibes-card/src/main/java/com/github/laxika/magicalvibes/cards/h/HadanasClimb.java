package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WingedTempleOfOrazca;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "158")
public class HadanasClimb extends Card {

    public HadanasClimb() {
        setBackFaceCard(new WingedTempleOfOrazca());

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new ConditionalEffect(
                                new TargetPermanentMatches(
                                        new PermanentHasAtLeastCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE, 3)),
                                new TransformSelfEffect(),
                                false));
    }

    @Override
    public String getBackFaceClassName() {
        return "WingedTempleOfOrazca";
    }
}
