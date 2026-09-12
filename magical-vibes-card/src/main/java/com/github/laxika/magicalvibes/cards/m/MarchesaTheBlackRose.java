package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "VMA", collectorNumber = "256")
public class MarchesaTheBlackRose extends Card {

    public MarchesaTheBlackRose() {
        // Other creatures you control have dethrone.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DETHRONE, GrantScope.OWN_CREATURES));

        // Whenever a creature you control with a +1/+1 counter on it dies, return that card to the
        // battlefield under your control at the beginning of the next end step.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                        new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                                false, null, 0, null, null)));

        // Marchesa herself is also a creature you control for the death trigger.
        addEffect(EffectSlot.ON_DEATH,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                        new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                                false, null, 0, null, null)));
    }
}
