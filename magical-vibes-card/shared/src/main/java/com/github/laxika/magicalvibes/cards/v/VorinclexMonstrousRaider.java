package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;

@CardRegistration(set = "KHM", collectorNumber = "199")
@CardRegistration(set = "KHM", collectorNumber = "320")
@CardRegistration(set = "KHM", collectorNumber = "333")
@CardRegistration(set = "KHM", collectorNumber = "406")
@CardRegistration(set = "KHM", collectorNumber = "407")
public class VorinclexMonstrousRaider extends Card {

    public VorinclexMonstrousRaider() {
        addStaticEffect(AddOnePlusOneCountersEffect.twice(CounterType.ANY, AddOnePlusOneCountersEffect.anyPermanent())
                .alsoOnPlayers().placedByYou());
        addStaticEffect(AddOnePlusOneCountersEffect.halfRoundedDown(CounterType.ANY, AddOnePlusOneCountersEffect.anyPermanent())
                .alsoOnPlayers().placedByAnOpponent());
    }
}
