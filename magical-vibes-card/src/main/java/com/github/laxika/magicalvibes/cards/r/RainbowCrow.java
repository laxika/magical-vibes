package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "69")
public class RainbowCrow extends Card {

    public RainbowCrow() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SetChosenColorUntilEndOfTurnEffect(false, false)),
                "{1}: This creature becomes the color of your choice until end of turn."
        ));
    }
}
