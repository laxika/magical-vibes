package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "P02", collectorNumber = "108")
@CardRegistration(set = "5DN", collectorNumber = "72")
@CardRegistration(set = "C15", collectorNumber = "161")
@CardRegistration(set = "CM2", collectorNumber = "111")
public class MagmaGiant extends Card {

    public MagmaGiant() {
        // When this creature enters, it deals 2 damage to each creature and each player.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MassDamageEffect(2, true));
    }
}
