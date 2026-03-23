package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "236")
public class DuskLegionDreadnought extends Card {

    public DuskLegionDreadnought() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), new AnimateSelfAsCreatureEffect()),
                "Crew 2"
        ));
    }
}
