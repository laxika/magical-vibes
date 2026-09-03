package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "FUT", collectorNumber = "99")
public class GathanRaiders extends Card {

    public GathanRaiders() {
        addMorph("{0}", new DiscardCardTypeCost(null, null));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerHandEmpty(),
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
