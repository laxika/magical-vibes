package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "M21", collectorNumber = "100")
public class GloomSower extends Card {

    public GloomSower() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER),
                TriggerMode.PER_BLOCKER);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new GainLifeEffect(2),
                TriggerMode.PER_BLOCKER);
    }
}
