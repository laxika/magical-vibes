package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "136")
public class StallionOfAshmouth extends Card {

    public StallionOfAshmouth() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new BoostSelfEffect(1, 1)),
                "{1}{B}: This creature gets +1/+1 until end of turn."
        ).withActivationCondition(
                new Delirium(),
                "Activate only if there are four or more card types among cards in your graveyard."
        ));
    }
}
