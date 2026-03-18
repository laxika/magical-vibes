package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "MBS", collectorNumber = "67")
public class IntoTheCore extends Card {

    public IntoTheCore() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact"
        ), 2, 2).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
