package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MetzaliTowerOfTriumph;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "165")
public class PathOfMettle extends Card {

    private static final PermanentPredicate FAST_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentAnyOfPredicate(List.of(
                    new PermanentHasKeywordPredicate(Keyword.FIRST_STRIKE),
                    new PermanentHasKeywordPredicate(Keyword.DOUBLE_STRIKE),
                    new PermanentHasKeywordPredicate(Keyword.VIGILANCE),
                    new PermanentHasKeywordPredicate(Keyword.HASTE)
            ))
    ));

    private static final PermanentPredicate CREATURE_WITHOUT_SPEED = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentAnyOfPredicate(List.of(
                    new PermanentHasKeywordPredicate(Keyword.FIRST_STRIKE),
                    new PermanentHasKeywordPredicate(Keyword.DOUBLE_STRIKE),
                    new PermanentHasKeywordPredicate(Keyword.VIGILANCE),
                    new PermanentHasKeywordPredicate(Keyword.HASTE)
            )))
    ));

    public PathOfMettle() {
        setBackFaceCard(new MetzaliTowerOfTriumph());

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToEachMatchingPermanentEffect(1, CREATURE_WITHOUT_SPEED, EachPermanentScope.ALL_PLAYERS));
        addTrigger(Triggers.youAttack(),
                new ConditionalEffect(new MinimumMatchingAttackers(2, FAST_CREATURE), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "MetzaliTowerOfTriumph";
    }
}
