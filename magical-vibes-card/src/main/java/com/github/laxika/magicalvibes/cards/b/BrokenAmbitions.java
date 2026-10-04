package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "LRW", collectorNumber = "54")
public class BrokenAmbitions extends Card {

    public BrokenAmbitions() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(0, true, false));
        addEffect(EffectSlot.SPELL,
                new ClashEffect(new MillEffect(4, MillRecipient.TARGET_SPELL_CONTROLLER)));
    }
}
