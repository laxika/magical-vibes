package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "TMP", collectorNumber = "88")
public class SkyshroudCondor extends Card {

    public SkyshroudCondor() {
        // Cast this spell only if you've cast another spell this turn. (Flying is auto-loaded.)
        setCastCondition(new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()));
    }
}
