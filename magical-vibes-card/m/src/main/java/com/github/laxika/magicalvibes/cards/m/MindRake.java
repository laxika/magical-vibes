package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

/**
 * Target player discards two cards.
 * <p>
 * Overload {1}{B} changes the targeted effect to make each player discard two cards instead.
 */
@CardRegistration(set = "MH1", collectorNumber = "96")
public class MindRake extends Card {

    public MindRake() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}"))));
        addSpellEffect(new ConditionalReplacementEffect(
                new Overloaded(),
                new DiscardEffect(2, Subjects.targetPlayer()),
                new DiscardEffect(2, Subjects.eachPlayer())));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ));
    }
}
