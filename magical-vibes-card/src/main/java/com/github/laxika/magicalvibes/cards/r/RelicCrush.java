package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "179")
public class RelicCrush extends Card {

    public RelicCrush() {
        target(artifactOrEnchantmentFilter("First target must be an artifact or enchantment"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(artifactOrEnchantmentFilter("Second target must be an artifact or enchantment"), 0, 1)
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }

    private static PermanentPredicateTargetFilter artifactOrEnchantmentFilter(String description) {
        return new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                description
        );
    }
}
