package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "13")
public class MartyrsCause extends Card {

    public MartyrsCause() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(),
                        PreventDamageFromChosenSourceEffect.nextDamageToAnyTarget()
                ),
                "Sacrifice a creature: The next time a source of your choice would deal damage to any target this turn, prevent that damage."
        ));
    }
}
