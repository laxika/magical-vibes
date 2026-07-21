package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "143")
public class BantSureblade extends Card {

    public BantSureblade() {
        // As long as you control another multicolored permanent, Bant Sureblade gets +1/+1 and has first strike.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControlsAnotherPermanent(new PermanentIsMulticoloredPredicate()), new StaticBoostEffect(1, 1, Set.of(Keyword.FIRST_STRIKE), GrantScope.SELF)));
    }
}
