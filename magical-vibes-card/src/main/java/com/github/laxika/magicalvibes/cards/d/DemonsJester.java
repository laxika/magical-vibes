package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "DIS", collectorNumber = "42")
public class DemonsJester extends Card {

    public DemonsJester() {
        // This creature gets +2/+1 as long as you have no cards in hand.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHandEmpty(),
                new StaticBoostEffect(2, 1, GrantScope.SELF)));
    }
}
