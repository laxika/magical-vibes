package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreatureDyingDontCauseTriggersEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureEnteringDontCauseTriggersEffect;

@CardRegistration(set = "ELD", collectorNumber = "18")
public class Hushbringer extends Card {

    public Hushbringer() {
        addEffect(EffectSlot.STATIC, new CreatureEnteringDontCauseTriggersEffect());
        addEffect(EffectSlot.STATIC, new CreatureDyingDontCauseTriggersEffect());
    }
}
