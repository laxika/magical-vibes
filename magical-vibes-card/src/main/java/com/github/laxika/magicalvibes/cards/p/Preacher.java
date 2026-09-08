package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureOfChosenPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "16")
public class Preacher extends Card {

    public Preacher() {
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        ActivatedAbility ability = new ActivatedAbility(
                true,
                null,
                List.of(new GainControlOfTargetCreatureOfChosenPlayerEffect(
                        ControlDuration.WHILE_SOURCE_REMAINS_TAPPED, 1)),
                "{T}: For as long as this creature remains tapped, gain control of target creature "
                        + "of an opponent's choice they control.",
                List.of(
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent"),
                        TargetFilters.creature()),
                2,
                2
        );
        ability.withOpponentChosenTargetByController(1, TargetFilters.creature())
                .withMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET);
        addActivatedAbility(ability);
    }
}
