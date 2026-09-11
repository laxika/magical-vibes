package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "SUM", collectorNumber = "79")
@CardRegistration(set = "ME1", collectorNumber = "50")
@CardRegistration(set = "3ED", collectorNumber = "79")
@CardRegistration(set = "V09", collectorNumber = "11")
public class SerendibEfreet extends Card {

    public SerendibEfreet() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}
