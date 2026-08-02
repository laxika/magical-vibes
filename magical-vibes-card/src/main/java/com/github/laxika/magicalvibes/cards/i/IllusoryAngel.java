package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "M15", collectorNumber = "59")
public class IllusoryAngel extends Card {

    public IllusoryAngel() {
        setCastCondition(new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()));
    }
}
