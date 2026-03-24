package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "247")
public class SleekSchooner extends Card {

    public SleekSchooner() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), new AnimateSelfAsCreatureEffect()),
                "Crew 1"
        ));
    }
}
