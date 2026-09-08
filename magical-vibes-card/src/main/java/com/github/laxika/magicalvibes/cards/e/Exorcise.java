package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "8")
public class Exorcise extends Card {

    public Exorcise() {
        var artifactEnchantmentOrLargeCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4)
                ))
        ));
        target(new PermanentPredicateTargetFilter(
                artifactEnchantmentOrLargeCreature,
                "Target must be an artifact, enchantment, or creature with power 4 or greater"
        )).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect(artifactEnchantmentOrLargeCreature));
    }
}
