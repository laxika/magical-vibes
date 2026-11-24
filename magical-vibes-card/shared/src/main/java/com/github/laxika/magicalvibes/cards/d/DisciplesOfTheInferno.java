package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

public class DisciplesOfTheInferno extends Card {

    public DisciplesOfTheInferno() {
        addStaticEffect(new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(2)
                .fromSources(new PermanentNotPredicate(new PermanentIsCreaturePredicate()))
                .to(Subjects.eachOpponent(), Subjects.controlledBy(Subjects.eachPlayer(), new PermanentAnyOfPredicate(
                        List.of(new PermanentIsCreaturePredicate(), new PermanentIsBattlePredicate())))));
    }
}
