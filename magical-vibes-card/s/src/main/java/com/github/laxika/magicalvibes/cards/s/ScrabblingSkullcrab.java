package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "DSK", collectorNumber = "71")
public class ScrabblingSkullcrab extends Card {

    public ScrabblingSkullcrab() {
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new MillEffect(2, MillRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED,
                new MillEffect(2, MillRecipient.TARGET_PLAYER));
    }
}
