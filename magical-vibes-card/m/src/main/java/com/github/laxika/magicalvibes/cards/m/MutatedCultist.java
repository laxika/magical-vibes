package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentOrPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M3C", collectorNumber = "53")
public class MutatedCultist extends Card {

    public MutatedCultist() {
        target(new AnyTargetPredicateTargetFilter(
                new PermanentTruePredicate(),
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be a permanent or opponent"
        ), 0, 1)
                .addTrigger(Triggers.youCastThisSpell(), new RemoveAllCountersFromTargetPermanentOrPlayerEffect())
                .addTrigger(Triggers.youCastThisSpell(),
                        new ReduceCastCostForNextMatchingSpellEffect(new CardTruePredicate(), new EventValue()));
    }
}
