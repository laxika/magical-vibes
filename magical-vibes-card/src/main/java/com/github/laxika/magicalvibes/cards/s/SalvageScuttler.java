package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "AER", collectorNumber = "43")
public class SalvageScuttler extends Card {

    public SalvageScuttler() {
        // Whenever this creature attacks, return an artifact you control to its owner's hand.
        addEffect(EffectSlot.ON_ATTACK, new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentIsArtifactPredicate(),
                "artifact"
        ));
    }
}
