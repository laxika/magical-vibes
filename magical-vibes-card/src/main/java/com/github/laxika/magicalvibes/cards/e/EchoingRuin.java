package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DST", collectorNumber = "59")
public class EchoingRuin extends Card {

    public EchoingRuin() {
        PermanentIsArtifactPredicate artifactPredicate = new PermanentIsArtifactPredicate();
        target(TargetFilters.artifact()).addEffect(
                EffectSlot.SPELL,
                new DestroyTargetPermanentAndAllWithSameNameEffect(artifactPredicate));
    }
}
