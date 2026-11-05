package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GN2", collectorNumber = "2")
@CardRegistration(set = "BLC", collectorNumber = "175")
@CardRegistration(set = "SCD", collectorNumber = "61")
public class SphinxOfEnlightenment extends Card {

    public SphinxOfEnlightenment() {
        var drawCardForTargetPlayer = new DrawCardForTargetPlayerEffect(1, false, true);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).bindEffect(drawCardForTargetPlayer);
        addTrigger(Triggers.enters(ObjectRelation.THIS), drawCardForTargetPlayer, new DrawCardEffect(3));
    }
}
