package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOM", collectorNumber = "103")
@CardRegistration(set = "M10", collectorNumber = "155")
public class Shatter extends Card {

    public Shatter() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
