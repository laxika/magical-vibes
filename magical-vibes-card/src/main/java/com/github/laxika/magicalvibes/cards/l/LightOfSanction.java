package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControlledCreaturesFromControlledSourcesEffect;

@CardRegistration(set = "RAV", collectorNumber = "24")
public class LightOfSanction extends Card {

    public LightOfSanction() {
        addEffect(EffectSlot.STATIC, new PreventDamageToControlledCreaturesFromControlledSourcesEffect());
    }
}
