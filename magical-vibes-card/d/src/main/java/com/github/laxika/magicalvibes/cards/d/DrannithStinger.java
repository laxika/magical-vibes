package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "113")
public class DrannithStinger extends Card {

    public DrannithStinger() {
        addTrigger(Triggers.youCycleCard(),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addCycling("{1}");
    }
}
