package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "TSP", collectorNumber = "222")
public class SquallLine extends Card {

    public SquallLine() {
        addSpellEffect(DealDamageEffect.toEachCreature(new XValue(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)).andTo(Subjects.eachPlayer()));
    }
}
