package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreatureEnteringDontCauseTriggersEffect;

@CardRegistration(set = "XLN", collectorNumber = "42")
public class TocatliHonorGuard extends Card {

    public TocatliHonorGuard() {
        addEffect(EffectSlot.STATIC, new CreatureEnteringDontCauseTriggersEffect());
    }
}
