package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "SPM", collectorNumber = "160")
@CardRegistration(set = "OM1", collectorNumber = "146")
public class WraithViciousVigilante extends Card {

    public WraithViciousVigilante() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
