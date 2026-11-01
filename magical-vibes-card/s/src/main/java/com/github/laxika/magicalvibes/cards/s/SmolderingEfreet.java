package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "115")
public class SmolderingEfreet extends Card {

    public SmolderingEfreet() {
        // When this creature dies, it deals 2 damage to you.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER));
    }
}
