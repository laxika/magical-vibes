package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "218")
public class CranialArchive extends Card {

    public CranialArchive() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ExileSelfCost(),
                        new ShuffleGraveyardIntoLibraryEffect(true),
                        new DrawCardEffect(1)
                ),
                "{2}, Exile Cranial Archive: Target player shuffles their graveyard into their library. Draw a card.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
