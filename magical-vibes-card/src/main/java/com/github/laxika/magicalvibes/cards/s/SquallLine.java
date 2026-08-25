package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "TSP", collectorNumber = "222")
public class SquallLine extends Card {

    public SquallLine() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(0, true, true,
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
