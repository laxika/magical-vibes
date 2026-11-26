package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "101")
@CardRegistration(set = "IMA", collectorNumber = "94")
@CardRegistration(set = "DDR", collectorNumber = "53")
@CardRegistration(set = "SCD", collectorNumber = "81")
public class IndulgentTormentor extends Card {

    public IndulgentTormentor() {
        // At the beginning of your upkeep, draw a card unless target opponent sacrifices a creature
        // of their choice or pays 3 life.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessPays(Subjects.targetPlayer(), AnyOfCost.of(
                                new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "creature", false),
                                new PayLifeCost(new Fixed(3))),
                        new DrawCardEffect(1)));
    }
}
