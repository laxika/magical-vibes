package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsForDestroyedPermanentControllersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "85")
public class IndomitableCreativity extends Card {

    public IndomitableCreativity() {
        targetX(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate())),
                "Targets must be artifacts and/or creatures"), 100)
                .addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
        addEffect(EffectSlot.SPELL,
                new RevealUntilCountMatchingCardsForDestroyedPermanentControllersEffect(
                        Set.of(CardType.ARTIFACT, CardType.CREATURE)));
    }
}
