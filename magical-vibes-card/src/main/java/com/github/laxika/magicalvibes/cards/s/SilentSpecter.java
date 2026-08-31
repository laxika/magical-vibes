package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "ONS", collectorNumber = "169")
public class SilentSpecter extends Card {

    public SilentSpecter() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));
    }
}
