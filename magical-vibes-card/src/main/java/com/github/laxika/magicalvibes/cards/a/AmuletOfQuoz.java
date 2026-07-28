package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AmuletOfQuozAnteEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "308")
public class AmuletOfQuoz extends Card {

    public AmuletOfQuoz() {
        // "Remove this card from your deck before playing if you're not playing for ante" is a
        // deck-construction instruction with no in-game effect.

        // {T}, Sacrifice this artifact: Target opponent may ante the top card of their library. If they
        // don't, you flip a coin. If you win the flip, that player loses the game. If you lose the flip,
        // you lose the game. Activate only during your upkeep.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AmuletOfQuozAnteEffect()),
                "{T}, Sacrifice Amulet of Quoz: Target opponent may ante the top card of their library. "
                        + "If they don't, you flip a coin. If you win the flip, that player loses the game. "
                        + "If you lose the flip, you lose the game. Activate only during your upkeep.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                ),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
