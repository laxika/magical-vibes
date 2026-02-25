package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "SOM", collectorNumber = "8")
public class GhalmasWarden extends Card {

    public GhalmasWarden() {
        // Metalcraft — Ghalma's Warden gets +2/+2 as long as you control three or more artifacts.
        addEffect(EffectSlot.STATIC, new MetalcraftConditionalEffect(new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
