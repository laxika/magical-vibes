package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "94")
public class UninvitedGeist extends Card {

    public UninvitedGeist() {
        setBackFaceCard(new UnimpededTrespasser());

        // When this creature deals combat damage to a player, transform it.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new TransformToBackFaceEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "UnimpededTrespasser";
    }
}
