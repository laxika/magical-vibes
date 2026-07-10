package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "9ED", collectorNumber = "259")
public class NeedleStorm extends Card {

    public NeedleStorm() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(4, false, false, new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
