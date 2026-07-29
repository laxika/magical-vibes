package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachDestroyedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "MIR", collectorNumber = "160")
public class BuildersBane extends Card {

    public BuildersBane() {
        // Destroy X target artifacts. ~ deals damage to each player equal to the number of artifacts
        // they controlled that were put into a graveyard this way. The destroy effect stamps the
        // controller of every artifact actually destroyed onto the entry's per-player tally, which
        // the damage rider reads to give each player their own amount.
        targetX(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Targets must be artifacts"
        ), 100).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());

        addEffect(EffectSlot.SPELL, new DealDamageToEachDestroyedPermanentControllerEffect());
    }
}
