package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "DKA", collectorNumber = "49")
public class Shriekgeist extends Card {

    public Shriekgeist() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MillEffect(2, MillRecipient.TARGET_PLAYER));
    }
}
