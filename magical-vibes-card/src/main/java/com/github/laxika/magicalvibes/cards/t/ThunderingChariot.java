package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "239")
public class ThunderingChariot extends Card {

    public ThunderingChariot() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1"
        ));
    }
}
