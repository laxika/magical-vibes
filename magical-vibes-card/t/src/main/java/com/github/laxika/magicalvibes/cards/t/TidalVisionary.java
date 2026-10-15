package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "80")
public class TidalVisionary extends Card {

    public TidalVisionary() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SetChosenColorUntilEndOfTurnEffect()),
                "{T}: Target creature becomes the color of your choice until end of turn.",
                TargetFilters.creature()
        ));
    }
}
