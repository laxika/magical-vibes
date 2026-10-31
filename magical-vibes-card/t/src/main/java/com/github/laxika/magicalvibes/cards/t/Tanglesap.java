package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "ZEN", collectorNumber = "186")
public class Tanglesap extends Card {

    public Tanglesap() {
        addSpellEffect(PreventDamageEffect.allCombatExcept(
                new PermanentHasKeywordPredicate(Keyword.TRAMPLE)));
    }
}
