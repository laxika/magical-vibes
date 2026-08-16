package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "67")
public class ThirdPathSavant extends Card {

    public ThirdPathSavant() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}",
                List.of(new DrawCardEffect(2)),
                "{7}: Draw two cards."
        ));
    }
}
