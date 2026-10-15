package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "121")
public class AinokGuide extends Card {

    private static final String COUNTER_MODE = "Put a +1/+1 counter on this creature";
    private static final String LAND_MODE = "Search your library for a basic land card, reveal it, then shuffle and put that card on top";

    public AinokGuide() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseModeOnEnterEffect(List.of(COUNTER_MODE, LAND_MODE)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new SourceHasChosenMode(COUNTER_MODE),
                        new PutCountersOnSourceEffect(1, 1, 1)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new SourceHasChosenMode(LAND_MODE),
                        new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.TOP_OF_LIBRARY)));
    }
}
