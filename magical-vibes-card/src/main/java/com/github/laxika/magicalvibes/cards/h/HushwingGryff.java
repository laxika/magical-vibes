package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreatureEnteringDontCauseTriggersEffect;

@CardRegistration(set = "M15", collectorNumber = "15")
public class HushwingGryff extends Card {

    public HushwingGryff() {
        addEffect(EffectSlot.STATIC, new CreatureEnteringDontCauseTriggersEffect());
    }
}
