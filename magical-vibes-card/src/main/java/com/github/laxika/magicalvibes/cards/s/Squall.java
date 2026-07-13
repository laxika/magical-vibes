package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "7ED", collectorNumber = "271")
public class Squall extends Card {

    public Squall() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(2, false, false, new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
