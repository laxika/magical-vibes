package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "AKH", collectorNumber = "150")
public class ThresherLizard extends Card {

    public ThresherLizard() {
        // This creature gets +1/+2 as long as you have one or fewer cards in hand.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new CardsInHandAtMost(1), new StaticBoostEffect(1, 2, GrantScope.SELF)));
    }
}
