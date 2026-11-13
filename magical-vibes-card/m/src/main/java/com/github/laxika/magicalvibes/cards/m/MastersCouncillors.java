package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GraveyardsAtLeast;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "47")
public class MastersCouncillors extends Card {

    public MastersCouncillors() {
        GraveyardsAtLeast sevenCardGraveyards = new GraveyardsAtLeast(7);
        addStaticEffect(new DynamicStaticBoostEffect(
                new Scaled(sevenCardGraveyards, 2), new Fixed(0), GrantScope.SELF));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addTrigger(Triggers.youDrawSecondCardEachTurn(),
                new MillEffect(3, MillRecipient.TARGET_PLAYER));
    }
}
