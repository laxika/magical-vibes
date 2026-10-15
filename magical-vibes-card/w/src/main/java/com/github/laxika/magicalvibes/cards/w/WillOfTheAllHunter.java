package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "38")
public class WillOfTheAllHunter extends Card {

    public WillOfTheAllHunter() {
        var blocking = new TargetPermanentMatches(new PermanentIsBlockingPredicate());
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new NotCondition(blocking),
                        new BoostTargetCreatureEffect(2, 2)))
                .addEffect(EffectSlot.SPELL,
                        PutCounterOnTargetPermanentEffect.withResolutionCondition(
                                CounterType.PLUS_ONE_PLUS_ONE, 2, blocking.filter()));

        addCycling("{2}");
    }
}
