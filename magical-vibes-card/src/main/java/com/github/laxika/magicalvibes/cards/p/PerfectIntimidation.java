package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "115")
public class PerfectIntimidation extends Card {

    public PerfectIntimidation() {
        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");
        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent exiles two cards from their hand",
                        new TargetPlayerExilesFromHandEffect(2),
                        opponentFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Remove all counters from target creature",
                        new RemoveAllCountersFromTargetCreatureEffect(),
                        creatureFilter)
        )));
    }
}
