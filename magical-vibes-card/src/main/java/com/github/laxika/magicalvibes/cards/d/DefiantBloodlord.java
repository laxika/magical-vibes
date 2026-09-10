package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "BFZ", collectorNumber = "107")
public class DefiantBloodlord extends Card {

    public DefiantBloodlord() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER));
    }
}
