package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "FRF", collectorNumber = "115")
public class SmolderingEfreet extends Card {

    public SmolderingEfreet() {
        // When this creature dies, it deals 2 damage to you.
        addEffect(EffectSlot.ON_DEATH, new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER));
    }
}
