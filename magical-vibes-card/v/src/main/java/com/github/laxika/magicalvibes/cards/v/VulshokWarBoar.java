package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "DST", collectorNumber = "72")
public class VulshokWarBoar extends Card {

    public VulshokWarBoar() {
        // When this creature enters, sacrifice it unless you sacrifice an artifact.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentIsArtifactPredicate(), "an artifact"));
    }
}
