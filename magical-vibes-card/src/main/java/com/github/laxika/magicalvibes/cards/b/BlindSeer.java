package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "47")
public class BlindSeer extends Card {

    public BlindSeer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new SetChosenColorUntilEndOfTurnEffect(true)),
                "{1}{U}: Target spell or permanent becomes the color of your choice until end of turn."
        ));
    }
}
