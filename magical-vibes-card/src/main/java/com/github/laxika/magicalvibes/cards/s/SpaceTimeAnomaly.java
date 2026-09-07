package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "EOE", collectorNumber = "229")
public class SpaceTimeAnomaly extends Card {

    public SpaceTimeAnomaly() {
        addEffect(EffectSlot.SPELL, new MillEffect(new ControllerLifeTotal(), MillRecipient.TARGET_PLAYER));
    }
}
