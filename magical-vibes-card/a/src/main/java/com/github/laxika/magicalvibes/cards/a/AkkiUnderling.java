package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "93")
public class AkkiUnderling extends Card {

    public AkkiUnderling() {
        // As long as you have seven or more cards in hand, this creature gets +2/+1 and has first strike.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CardsInHandAtLeast(7),
                new StaticBoostEffect(2, 1, Set.of(Keyword.FIRST_STRIKE), GrantScope.SELF)));
    }
}
