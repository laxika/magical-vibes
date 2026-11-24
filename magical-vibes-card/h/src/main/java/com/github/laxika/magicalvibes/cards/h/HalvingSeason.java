package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.HalveTokenCreationEffect;

@CardRegistration(set = "MB2", collectorNumber = "309")
@CardRegistration(set = "MB2", collectorNumber = "545")
public class HalvingSeason extends Card {

    public HalvingSeason() {
        addStaticEffect(new HalveTokenCreationEffect());
        addStaticEffect(AddOnePlusOneCountersEffect.halfRoundedDown(CounterType.ANY, AddOnePlusOneCountersEffect.anyPermanent())
                .alsoOnPlayers().placedByAnOpponent());
    }
}
