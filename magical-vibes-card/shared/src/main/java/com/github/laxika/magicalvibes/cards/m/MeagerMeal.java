package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class MeagerMeal extends Card {

    public MeagerMeal() {
        SpellTarget playerTarget = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"));
        SpellTarget creatureTarget = target(TargetFilters.creature(), 0, 1);

        creatureTarget.addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        playerTarget.addSpellEffect(new GainLifeEffect(2, Subjects.targetPlayer()));
    }
}
