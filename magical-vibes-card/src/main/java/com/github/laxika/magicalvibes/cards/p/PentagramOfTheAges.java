package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenSourceEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "306")
public class PentagramOfTheAges extends Card {

    public PentagramOfTheAges() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new PreventNextDamageFromChosenSourceEffect(false)),
                "The next time a source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
