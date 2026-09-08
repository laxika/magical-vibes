package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "FDN", collectorNumber = "178")
public class MaraudingBlightPriest extends Card {

    public MaraudingBlightPriest() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
