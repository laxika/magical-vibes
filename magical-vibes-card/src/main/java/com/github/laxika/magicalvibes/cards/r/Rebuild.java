package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ULG", collectorNumber = "40")
public class Rebuild extends Card {

    public Rebuild() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.allPermanentsMatching(new PermanentIsArtifactPredicate()));
        addCycling("{2}");
    }
}
