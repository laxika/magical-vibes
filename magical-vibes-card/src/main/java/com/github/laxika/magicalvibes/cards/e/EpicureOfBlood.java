package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "M19", collectorNumber = "95")
public class EpicureOfBlood extends Card {

    public EpicureOfBlood() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
