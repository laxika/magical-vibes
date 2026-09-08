package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "DSK", collectorNumber = "84")
public class BalemurkLeech extends Card {

    public BalemurkLeech() {
        LoseLifeEffect lifeLoss = new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT);
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, lifeLoss);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, lifeLoss);
    }
}
