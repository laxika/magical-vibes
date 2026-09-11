package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

import java.util.List;

@CardRegistration(set = "ME1", collectorNumber = "157")
public class Forcefield extends Card {

    public Forcefield() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(PreventDamageFromChosenSourceEffect.nextCombatDamageToYouFromChosenCreature()),
                "{1}: The next time an unblocked creature of your choice would deal combat damage to you this turn, prevent all but 1 of that damage."
        ));
    }
}
