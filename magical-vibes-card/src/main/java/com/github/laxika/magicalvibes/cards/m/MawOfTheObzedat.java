package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "83")
public class MawOfTheObzedat extends Card {

    public MawOfTheObzedat() {
        // Sacrifice a creature: Creatures you control get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(),
                        new BoostAllOwnCreaturesEffect(1, 1)
                ),
                "Sacrifice a creature: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
