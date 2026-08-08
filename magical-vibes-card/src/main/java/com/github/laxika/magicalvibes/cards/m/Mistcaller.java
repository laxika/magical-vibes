package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileUncastEnteringCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "62")
public class Mistcaller extends Card {

    public Mistcaller() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new ExileUncastEnteringCreaturesEffect(true)
                ),
                "Sacrifice this creature: Until end of turn, if a nontoken creature would enter and it wasn't cast, exile it instead."
        ));
    }
}
