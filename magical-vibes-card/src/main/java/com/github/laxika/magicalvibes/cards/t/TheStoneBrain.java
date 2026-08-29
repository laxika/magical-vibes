package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "247")
public class TheStoneBrain extends Card {

    public TheStoneBrain() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new ExileSelfCost(),
                        new ChooseCardNameAndExileFromZonesEffect(List.of(), null, 4, true)
                ),
                "{2}, {T}, Exile The Stone Brain: Choose a card name. Search target opponent's graveyard, "
                        + "hand, and library for up to four cards with that name and exile them. That player "
                        + "shuffles, then draws a card for each card exiled from their hand this way. Activate "
                        + "only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
