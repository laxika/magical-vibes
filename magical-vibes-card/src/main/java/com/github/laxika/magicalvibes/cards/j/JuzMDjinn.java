package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "ME1", collectorNumber = "74")
public class JuzMDjinn extends Card {

    public JuzMDjinn() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}
