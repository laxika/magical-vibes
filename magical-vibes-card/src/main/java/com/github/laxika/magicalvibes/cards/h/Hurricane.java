package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "10E", collectorNumber = "270")
public class Hurricane extends Card {

    public Hurricane() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(0, true, true, new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
