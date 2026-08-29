package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessSacrificeNonlandOrDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

public class TergridsLantern extends Card {

    public TergridsLantern() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new LoseLifeUnlessSacrificeNonlandOrDiscardEffect(3,
                        LoseLifeRecipient.TARGET_PLAYER)),
                "{T}: Target player loses 3 life unless they sacrifice a nonland permanent of their choice or discard a card.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{3}{B}: Untap Tergrid's Lantern."
        ));
    }
}
