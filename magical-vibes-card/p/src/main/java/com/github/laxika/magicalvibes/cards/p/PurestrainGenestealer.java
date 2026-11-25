package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "97")
public class PurestrainGenestealer extends Card {

    public PurestrainGenestealer() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        RemoveCountersEffect.fromSelf(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new ConditionalEffect(HappenedThisWay.removedCounters(), new SearchLibraryEffect(
                                CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED))),
                "Remove a +1/+1 counter from Purestrain Genestealer?"));
    }
}
