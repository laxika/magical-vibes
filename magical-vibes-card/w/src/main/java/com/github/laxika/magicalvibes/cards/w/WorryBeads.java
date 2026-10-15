package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "MMQ", collectorNumber = "315")
public class WorryBeads extends Card {

    public WorryBeads() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new MillEffect(1, MillRecipient.ACTIVE_PLAYER));
    }
}
