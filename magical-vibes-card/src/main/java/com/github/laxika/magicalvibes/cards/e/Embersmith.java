package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnArtifactCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "SOM", collectorNumber = "87")
public class Embersmith extends Card {

    public Embersmith() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new DealDamageToAnyTargetOnArtifactCastEffect(1, 1),
                "Pay {1} to deal 1 damage to any target?"
        ));
    }
}
