package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "INV", collectorNumber = "119")
public class PlagueSpitter extends Card {

    public PlagueSpitter() {
        // At the beginning of your upkeep, this creature deals 1 damage to each creature and each player.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MassDamageEffect(1, true));

        // When this creature dies, it deals 1 damage to each creature and each player.
        addEffect(EffectSlot.ON_DEATH, new MassDamageEffect(1, true));
    }
}
