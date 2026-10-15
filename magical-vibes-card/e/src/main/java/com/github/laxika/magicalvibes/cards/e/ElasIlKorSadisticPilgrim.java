package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "DMU", collectorNumber = "198")
public class ElasIlKorSadisticPilgrim extends Card {

    public ElasIlKorSadisticPilgrim() {
        // Whenever another creature you control enters, you gain 1 life.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));

        // Whenever another creature you control dies, each opponent loses 1 life.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
