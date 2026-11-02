package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VMA", collectorNumber = "256")
@CardRegistration(set = "SLD", collectorNumber = "1452")
@CardRegistration(set = "SLD", collectorNumber = "2009")
@CardRegistration(set = "2X2", collectorNumber = "248")
@CardRegistration(set = "C17", collectorNumber = "177")
public class MarchesaTheBlackRose extends Card {

    public MarchesaTheBlackRose() {
        // Other creatures you control have dethrone.
        addStaticEffect(new GrantKeywordEffect(Keyword.DETHRONE, GrantScope.OWN_CREATURES));

        // Whenever a creature you control with a +1/+1 counter on it dies, return that card to the
        // battlefield under your control at the beginning of the next end step.
        addTrigger(Triggers.creatureDies(
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                        ObjectRelation.YOU_CONTROL),
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null));

    }
}
