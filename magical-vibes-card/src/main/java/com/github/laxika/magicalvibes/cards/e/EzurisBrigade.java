package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "121")
public class EzurisBrigade extends Card {

    public EzurisBrigade() {
        // Metalcraft — As long as you control three or more artifacts, this creature gets +4/+4 and has trample.
        addEffect(EffectSlot.STATIC, new MetalcraftConditionalEffect(
                new StaticBoostEffect(4, 4, Set.of(Keyword.TRAMPLE), GrantScope.SELF)));
    }
}
