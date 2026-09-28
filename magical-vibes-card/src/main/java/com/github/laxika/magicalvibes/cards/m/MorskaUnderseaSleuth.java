package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "MKC", collectorNumber = "3")
@CardRegistration(set = "MKC", collectorNumber = "51")
@CardRegistration(set = "MKC", collectorNumber = "317")
public class MorskaUnderseaSleuth extends Card {

    public MorskaUnderseaSleuth() {
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CreateTokenEffect.ofClueToken(1));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
