package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "131")
public class CapriciousEfreet extends Card {

    public CapriciousEfreet() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DestroyOneOfTargetsAtRandomEffect());
    }
}
