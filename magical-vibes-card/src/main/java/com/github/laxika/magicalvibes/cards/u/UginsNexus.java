package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UginNexusReplacementEffect;

@CardRegistration(set = "KTK", collectorNumber = "227")
public class UginsNexus extends Card {

    public UginsNexus() {
        addEffect(EffectSlot.STATIC, new UginNexusReplacementEffect());
    }
}
