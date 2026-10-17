package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "ORI", collectorNumber = "154")
public class MageRingBully extends Card {

    public MageRingBully() {
        // This creature attacks each combat if able. (Prowess is a Scryfall-loaded keyword.)
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
