package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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
        addTrigger(Triggers.enters(ObjectRelation.THIS), MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_OPPONENT,
                new DealDamageToPlayersEffect(4, Subjects.acceptedPlayers()),
                "Have it deal 4 damage to you? If a player does, it is sacrificed.",
                new SacrificeSelfEffect(), null));
    }
}
