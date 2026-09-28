package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllButOneDamageToControllerAndHeroesEffect;

@CardRegistration(set = "MSC", collectorNumber = "599")
public class HyperionSupremeHero extends Card {

    public HyperionSupremeHero() {
        addEffect(EffectSlot.STATIC, new PreventAllButOneDamageToControllerAndHeroesEffect());
    }
}
