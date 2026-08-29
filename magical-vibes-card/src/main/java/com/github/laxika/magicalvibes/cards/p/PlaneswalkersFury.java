package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandDealDamageEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "70")
public class PlaneswalkersFury extends Card {

    public PlaneswalkersFury() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new RevealRandomCardFromTargetPlayerHandDealDamageEqualToManaValueEffect()),
                "{3}{R}: Target opponent reveals a card at random from their hand. This enchantment deals damage equal to that card's mana value to that player. Activate only as a sorcery.",
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
