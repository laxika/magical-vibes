package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "40K", collectorNumber = "97")
public class PurestrainGenestealer extends Card {

    public PurestrainGenestealer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new RemoveCounterFromSourceThenEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new SearchLibraryEffect(
                                CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                "Remove a +1/+1 counter from Purestrain Genestealer?"));
    }
}
