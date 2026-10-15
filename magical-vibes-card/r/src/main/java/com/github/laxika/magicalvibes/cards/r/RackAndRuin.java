package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ULG", collectorNumber = "89")
public class RackAndRuin extends Card {

    public RackAndRuin() {
        // Destroy two target artifacts.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Targets must be artifacts"
        ), 2, 2).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
