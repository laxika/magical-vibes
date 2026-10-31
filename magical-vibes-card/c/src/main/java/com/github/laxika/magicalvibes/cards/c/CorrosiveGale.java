package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "NPH", collectorNumber = "107")
public class CorrosiveGale extends Card {

    public CorrosiveGale() {
        addSpellEffect(new MassDamageEffect(0, true, false, new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
