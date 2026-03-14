package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "170")
public class AvacynsPilgrim extends Card {

    public AvacynsPilgrim() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.WHITE));
    }
}
