package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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
                List.of(UnlessPaysEffect.unlessPays(Subjects.targetPlayerDeclaredElsewhere(),
                        AnyOfCost.of(new SacrificePermanentCost(new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                "nonland permanent", false), new DiscardCardTypeCost(null, null)),
                        new LoseLifeEffect(3, Subjects.targetPlayerDeclaredElsewhere()))),
                "{T}: Target player loses 3 life unless they sacrifice a nonland permanent of their choice or discard a card.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(new UntapPermanentsEffect(Subjects.source())),
                "{3}{B}: Untap Tergrid's Lantern."
        ));
    }
}
