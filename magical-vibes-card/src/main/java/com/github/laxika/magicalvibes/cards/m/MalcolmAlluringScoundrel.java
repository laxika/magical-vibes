package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "LCI", collectorNumber = "63")
@CardRegistration(set = "LCI", collectorNumber = "293")
public class MalcolmAlluringScoundrel extends Card {

    public MalcolmAlluringScoundrel() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.CHORUS),
                        new DrawCardEffect(1),
                        new DiscardCardThenEffect(null,
                                new MayCastDiscardedCardWithoutPayingManaCostEffect(),
                                "a card")));
    }
}
