package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "43")
public class SliptideSerpent extends Card {

    public SliptideSerpent() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(ReturnToHandEffect.self()),
                "{3}{U}: Return this creature to its owner's hand."
        ));
    }
}
