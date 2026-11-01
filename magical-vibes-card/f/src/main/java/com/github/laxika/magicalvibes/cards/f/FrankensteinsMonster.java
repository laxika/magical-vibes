package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect;

@CardRegistration(set = "DRK", collectorNumber = "45")
public class FrankensteinsMonster extends Card {

    public FrankensteinsMonster() {
        addStaticEffect(new ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect(
                        CounterType.PLUS_TWO_PLUS_ZERO,
                        CounterType.PLUS_ONE_PLUS_ONE,
                        CounterType.PLUS_ZERO_PLUS_TWO));
    }
}
