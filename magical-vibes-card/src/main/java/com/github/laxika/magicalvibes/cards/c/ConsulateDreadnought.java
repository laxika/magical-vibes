package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "146")
public class ConsulateDreadnought extends Card {

    public ConsulateDreadnought() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(6), AnimatePermanentsEffect.crew()),
                "Crew 6"
        ));
    }
}
