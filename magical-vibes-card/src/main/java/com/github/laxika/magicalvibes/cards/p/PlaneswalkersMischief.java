package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromTargetHandMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "29")
public class PlaneswalkersMischief extends Card {

    public PlaneswalkersMischief() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new ExileRandomInstantOrSorceryFromTargetHandMayCastFreeEffect()),
                "{3}{U}: Target opponent reveals a card at random from their hand. If it's an instant "
                        + "or sorcery card, exile it. You may cast it without paying its mana cost for "
                        + "as long as it remains exiled. At the beginning of the next end step, if you "
                        + "haven't cast it, return it to its owner's hand. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
