package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "161")
public class GreaterGargadon extends Card {

    public GreaterGargadon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsLandPredicate())),
                                "Sacrifice an artifact, creature, or land",
                                false),
                        new RemoveTimeCounterFromExiledCardEffect(getId())),
                "Sacrifice an artifact, creature, or land: Remove a time counter from this card. Activate only if this card is suspended.")
                .withActivationCondition(new SourceCardSuspended(),
                        "Activate only if this card is suspended")
                .withExileOnly());
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(),
                "Suspend 10—{R}")
                .withSuspendsSourceFromHand(10));
    }
}
