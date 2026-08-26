package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "PLC", collectorNumber = "162")
public class RadhaHeirToKeld extends Card {

    public RadhaHeirToKeld() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new AwardManaEffect(ManaColor.RED, 2), "Add {R}{R}?"));
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
    }
}
