package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "50")
public class MidnightMangler extends Card {

    public MidnightMangler() {
        addStaticEffect(new ConditionalEffect(new NotControllerTurn(),
                new AnimatePermanentsEffect(null, null, List.of(), Set.of(), null, Set.of(),
                        Subjects.source(), Duration.UNTIL_END_OF_TURN, null)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
