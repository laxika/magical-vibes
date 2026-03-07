package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "NPH", collectorNumber = "128")
public class JorKadeenThePrevailer extends Card {

    public JorKadeenThePrevailer() {
        addEffect(EffectSlot.STATIC, new MetalcraftConditionalEffect(new StaticBoostEffect(3, 0, GrantScope.OWN_CREATURES)));
    }
}
