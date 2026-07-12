package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "299")
public class DistortingLens extends Card {

    public DistortingLens() {
        // {T}: Target permanent becomes the color of your choice until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new SetChosenColorUntilEndOfTurnEffect()),
                "{T}: Target permanent becomes the color of your choice until end of turn."
        ));
    }
}
