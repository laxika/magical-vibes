package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "102")
public class PriceOfBetrayal extends Card {

    private static final PermanentPredicate TARGET_PERMANENT = new PermanentAnyOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentIsCreaturePredicate(),
            new PermanentIsPlaneswalkerPredicate()));

    public PriceOfBetrayal() {
        target(new AnyTargetPredicateTargetFilter(
                TARGET_PERMANENT,
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an artifact, creature, planeswalker, or opponent."
        )).addEffect(EffectSlot.SPELL, new RemoveUpToCountersFromTargetEffect(5, TARGET_PERMANENT));
    }
}
