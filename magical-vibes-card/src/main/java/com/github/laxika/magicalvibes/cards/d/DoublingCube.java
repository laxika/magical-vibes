package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleManaPoolEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "321")
public class DoublingCube extends Card {

    public DoublingCube() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new DoubleManaPoolEffect()),
                false,
                "{3}, {T}: Double the amount of each type of unspent mana you have."
        ));
    }
}
