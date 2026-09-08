package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "81")
public class PilferingImp extends Card {

    public PilferingImp() {
        // {1}{B}, {T}, Sacrifice this creature: Target opponent reveals their hand. You choose a
        // nonland card from it. That player discards that card. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), HandChoiceDestination.DISCARD)
                ),
                "{1}{B}, {T}, Sacrifice this creature: Target opponent reveals their hand. You choose a nonland card from it. "
                        + "That player discards that card. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                ),
                null, null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
