package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "AKH", collectorNumber = "123")
public class ByForce extends Card {

    public ByForce() {
        // Destroy X target artifacts.
        // Single X-scaled target group: the number of artifacts targeted is bounded by
        // X at cast time, and every chosen target is destroyed.
        targetX(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Targets must be artifacts"
        ), 100).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
