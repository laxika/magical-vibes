package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "M21", collectorNumber = "106")
public class KaervekTheSpiteful extends Card {

    public KaervekTheSpiteful() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.ALL_CREATURES));
    }
}
