package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "FRF", collectorNumber = "161")
public class HewedStoneRetainers extends Card {

    public HewedStoneRetainers() {
        // Cast this spell only if you've cast another spell this turn.
        setCastCondition(new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()));
    }
}
