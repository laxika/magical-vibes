package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "HOB", collectorNumber = "4")
public class BelladonnaTook extends Card {

    public BelladonnaTook() {
        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD, SequenceEffect.of(
                new ConditionalEffect(new NthAbilityResolutionThisTurn(1), new GainLifeEffect(1)),
                new ConditionalEffect(new NthAbilityResolutionThisTurn(2), new DrawCardEffect(1)),
                new ConditionalEffect(new NthAbilityResolutionThisTurn(3),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()))
        ));
    }
}
