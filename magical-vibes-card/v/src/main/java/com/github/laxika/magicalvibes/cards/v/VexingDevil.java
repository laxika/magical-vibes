package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayTakeDamageSacrificeSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "313")
@CardRegistration(set = "INR", collectorNumber = "178")
@CardRegistration(set = "AVR", collectorNumber = "164")
@CardRegistration(set = "UMA", collectorNumber = "154")
public class VexingDevil extends Card {

    public VexingDevil() {
        // When this creature enters, any opponent may have it deal 4 damage to them.
        // If a player does, sacrifice this creature.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AnyOpponentMayTakeDamageSacrificeSourceEffect(4));
    }
}
