package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryForCardToExileWithPlayPermissionEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "288")
public class GrinningTotem extends Card {

    public GrinningTotem() {
        // {2}, {T}, Sacrifice Grinning Totem: Search target opponent's library for a card and exile it.
        // Then that player shuffles. Until the beginning of your next upkeep, you may play that card. At the
        // beginning of your next upkeep, if you haven't played it, put it into its owner's graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(),
                        new SearchTargetLibraryForCardToExileWithPlayPermissionEffect(true)),
                "{2}, {T}, Sacrifice Grinning Totem: Search target opponent's library for a card and exile it. "
                        + "Then that player shuffles. Until the beginning of your next upkeep, you may play that card. "
                        + "At the beginning of your next upkeep, if you haven't played it, put it into its owner's graveyard.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "You must target an opponent.")));
    }
}
