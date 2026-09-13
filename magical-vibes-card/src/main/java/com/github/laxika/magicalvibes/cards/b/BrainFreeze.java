package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

@CardRegistration(set = "SCG", collectorNumber = "29")
@CardRegistration(set = "VMA", collectorNumber = "57")
public class BrainFreeze extends Card {

    public BrainFreeze() {
        addEffect(EffectSlot.SPELL, new MillEffect(3, MillRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
