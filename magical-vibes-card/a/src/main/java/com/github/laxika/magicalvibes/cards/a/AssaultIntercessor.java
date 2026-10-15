package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "40K", collectorNumber = "104")
public class AssaultIntercessor extends Card {

    public AssaultIntercessor() {
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER));
    }
}
