package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;

@CardRegistration(set = "SOI", collectorNumber = "94")
public class UninvitedGeist extends Card {

    public UninvitedGeist() {
        setBackFaceCard(new UnimpededTrespasser());

        // When this creature deals combat damage to a player, transform it.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new TransformToBackFaceEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "UnimpededTrespasser";
    }
}
