package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "DMU", collectorNumber = "140")
public class PhoenixChick extends Card {

    public PhoenixChick() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new MinimumAttackers(3), new MayPayManaEffect("{R}{R}",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .enterAttacking(true)
                                .enterWithCounter(CounterType.PLUS_ONE_PLUS_ONE)
                                .enterWithCounterCount(1)
                                .build(),
                        "Pay {R}{R} to return Phoenix Chick from your graveyard to the battlefield tapped and attacking with a +1/+1 counter on it?")));
    }
}
