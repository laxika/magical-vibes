package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DistinctManaCostsAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "M20", collectorNumber = "98")
public class EmbodimentOfAgonies extends Card {

    public EmbodimentOfAgonies() {
        addStaticEffect(new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new DistinctManaCostsAmongCardsInGraveyard()));
    }
}
