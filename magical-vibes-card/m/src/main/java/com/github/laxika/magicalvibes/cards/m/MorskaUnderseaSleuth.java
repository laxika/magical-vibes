package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKC", collectorNumber = "3")
@CardRegistration(set = "MKC", collectorNumber = "51")
@CardRegistration(set = "MKC", collectorNumber = "317")
public class MorskaUnderseaSleuth extends Card {

    public MorskaUnderseaSleuth() {
        addStaticEffect(new NoMaximumHandSizeEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), CreateTokenEffect.ofClueToken(1));
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
