package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "M19", collectorNumber = "220")
public class PoisonTipArcher extends Card {

    public PoisonTipArcher() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
