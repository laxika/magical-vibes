package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "SOM", collectorNumber = "142")
public class ChromeSteed extends Card {

    public ChromeSteed() {
        addEffect(EffectSlot.STATIC, new MetalcraftConditionalEffect(new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
