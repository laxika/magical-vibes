package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TempestEfreetAnteExchangeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "225")
public class TempestEfreet extends Card {

    public TempestEfreet() {
        // {T}, Sacrifice this creature: Target opponent may pay 10 life. If that player doesn't, they
        // reveal a card at random from their hand. Exchange ownership of the revealed card and Tempest
        // Efreet. Put the revealed card into your hand and Tempest Efreet from anywhere into that
        // player's graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new TempestEfreetAnteExchangeEffect(10)),
                "{T}, Sacrifice Tempest Efreet: Target opponent may pay 10 life. If that player doesn't, "
                        + "they reveal a card at random from their hand. Exchange ownership of the revealed "
                        + "card and Tempest Efreet. Put the revealed card into your hand and Tempest Efreet "
                        + "from anywhere into that player's graveyard. This change in ownership is permanent.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
