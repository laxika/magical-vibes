package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "MIR", collectorNumber = "232")
public class NettletoothDjinn extends Card {

    public NettletoothDjinn() {
        // "At the beginning of your upkeep, this creature deals 1 damage to you."
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}
