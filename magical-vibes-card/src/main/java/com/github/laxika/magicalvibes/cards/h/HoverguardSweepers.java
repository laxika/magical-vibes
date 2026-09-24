package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5DN", collectorNumber = "32")
@CardRegistration(set = "AA4", collectorNumber = "7")
@CardRegistration(set = "C14", collectorNumber = "113")
public class HoverguardSweepers extends Card {

    public HoverguardSweepers() {
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                        ReturnToHandEffect.target(), "Return the targeted creatures to their owners' hands?"));
    }
}
