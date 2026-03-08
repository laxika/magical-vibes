package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreatureEnteringDontCauseTriggersEffect;

@CardRegistration(set = "NPH", collectorNumber = "162")
public class TorporOrb extends Card {

    public TorporOrb() {
        addEffect(EffectSlot.STATIC, new CreatureEnteringDontCauseTriggersEffect());
    }
}
