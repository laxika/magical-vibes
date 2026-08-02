package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessTargetSacrificesCreatureOrPaysLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "M15", collectorNumber = "101")
public class IndulgentTormentor extends Card {

    public IndulgentTormentor() {
        // At the beginning of your upkeep, draw a card unless target opponent sacrifices a creature
        // of their choice or pays 3 life.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DrawCardUnlessTargetSacrificesCreatureOrPaysLifeEffect(3));
    }
}
