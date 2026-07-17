package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "116")
public class ReefPirates extends Card {

    public ReefPirates() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new MillEffect(1, MillRecipient.TARGET_PLAYER));
    }
}
