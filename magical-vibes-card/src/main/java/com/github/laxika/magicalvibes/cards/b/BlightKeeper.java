package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "92")
public class BlightKeeper extends Card {

    public BlightKeeper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}{B}",
                List.of(new SacrificeSelfCost(), new TargetPlayerLosesLifeAndControllerGainsLifeEffect(4, 4)),
                "{7}{B}, {T}, Sacrifice Blight Keeper: Target opponent loses 4 life and you gain 4 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
