package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "645")
public class VultureFeatheredFiend extends Card {

    public VultureFeatheredFiend() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of(
                        new PutCounterOnEachMatchingPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                                new PermanentTruePredicate(), EachPermanentScope.TRIGGERING_PERMANENTS),
                        new DrawCardEffect(1)));
    }
}
