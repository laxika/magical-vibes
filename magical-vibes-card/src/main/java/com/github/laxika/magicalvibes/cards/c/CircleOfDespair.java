package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "259")
public class CircleOfDespair extends Card {

    public CircleOfDespair() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(),
                        PreventDamageFromChosenSourceEffect.nextDamageToAnyTarget()
                ),
                "{1}, Sacrifice a creature: The next time a source of your choice would deal damage to any target this turn, prevent that damage."
        ));
    }
}
