package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "CHK", collectorNumber = "209")
public class GaleForce extends Card {

    public GaleForce() {
        addSpellEffect(DealDamageEffect.toEachCreature(5, new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
