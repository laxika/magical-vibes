package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "OGW", collectorNumber = "153")
public class CliffhavenVampire extends Card {

    public CliffhavenVampire() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
