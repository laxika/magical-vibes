package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentCounterCountAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "99")
public class OrdealOfErebos extends Card {

    public OrdealOfErebos() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                        new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new ConditionalEffect(
                                new EnchantedPermanentMatches(
                                        new PermanentCounterCountAtLeastPredicate(
                                                CounterType.PLUS_ONE_PLUS_ONE, 3),
                                        "enchanted creature has three or more +1/+1 counters"),
                                new SacrificeSelfEffect())));
        addEffect(EffectSlot.ON_DEATH, DiscardEffect.sacrificeOnly(2));
    }
}
