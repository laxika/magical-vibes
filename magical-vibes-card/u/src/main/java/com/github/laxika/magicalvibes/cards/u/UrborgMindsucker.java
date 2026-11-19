package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "VIS", collectorNumber = "71")
@CardRegistration(set = "MGB", collectorNumber = "5")
public class UrborgMindsucker extends Card {

    public UrborgMindsucker() {
        // {B}, Sacrifice this creature: Target opponent discards a card at random. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false, "{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new DiscardEffect(1, Subjects.targetPlayer(), true)
                ),
                "{B}, Sacrifice this creature: Target opponent discards a card at random. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                ),
                null, null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
