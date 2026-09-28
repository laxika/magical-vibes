package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CoercivePortalEffect;

@CardRegistration(set = "VMA", collectorNumber = "266")
@CardRegistration(set = "LCC", collectorNumber = "109")
public class CoercivePortal extends Card {

    public CoercivePortal() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CoercivePortalEffect());
    }
}
