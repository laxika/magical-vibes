package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "RTR", collectorNumber = "150")
public class CollectiveBlessing extends Card {

    public CollectiveBlessing() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 3, GrantScope.OWN_CREATURES));
    }
}
