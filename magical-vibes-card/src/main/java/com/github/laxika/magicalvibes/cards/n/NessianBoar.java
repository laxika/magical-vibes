package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerDrawsCardEffect;

@CardRegistration(set = "THB", collectorNumber = "181")
public class NessianBoar extends Card {

    public NessianBoar() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect());
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new TargetPermanentControllerDrawsCardEffect(), TriggerMode.PER_BLOCKER);
    }
}
