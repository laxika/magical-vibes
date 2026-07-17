package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "5ED", collectorNumber = "29")
public class DustToDust extends Card {

    public DustToDust() {
        // Exile two target artifacts.
        // Two distinct target groups (shared targets not allowed) so the two
        // artifacts must differ, each exiled by its own effect.
        target(artifactFilter("First target must be an artifact"))
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
        target(artifactFilter("Second target must be an artifact"))
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }

    private static PermanentPredicateTargetFilter artifactFilter(String description) {
        return new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), description);
    }
}
