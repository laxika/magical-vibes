package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "35")
public class VotaryOfTheConclave extends Card {

    public VotaryOfTheConclave() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new RegenerateEffect()),
                "{2}{G}: Regenerate this creature."
        ));
    }
}
