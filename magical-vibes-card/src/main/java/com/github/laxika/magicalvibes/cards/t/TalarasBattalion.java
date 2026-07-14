package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "EVE", collectorNumber = "77")
public class TalarasBattalion extends Card {

    public TalarasBattalion() {
        // Cast this spell only if you've cast another green spell this turn. (Trample is auto-loaded.)
        setCastCondition(new ControllerCastAnotherSpellThisTurn(new CardColorPredicate(CardColor.GREEN)));
    }
}
