package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndAllWithSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "BOK", collectorNumber = "146")
@CardRegistration(set = "UDS", collectorNumber = "121")
public class Splinter extends Card {

    public Splinter() {
        PermanentIsArtifactPredicate artifact = new PermanentIsArtifactPredicate();
        target(new PermanentPredicateTargetFilter(
                artifact,
                "Target must be an artifact"
        )).addEffect(EffectSlot.SPELL, new ExileTargetPermanentAndAllWithSameNameFromZonesEffect(artifact));
    }
}
