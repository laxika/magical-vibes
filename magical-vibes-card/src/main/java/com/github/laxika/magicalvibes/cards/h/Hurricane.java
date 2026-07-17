package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "10E", collectorNumber = "270")
@CardRegistration(set = "POR", collectorNumber = "170")
@CardRegistration(set = "P02", collectorNumber = "129")
@CardRegistration(set = "7ED", collectorNumber = "252")
@CardRegistration(set = "6ED", collectorNumber = "237")
@CardRegistration(set = "5ED", collectorNumber = "303")
public class Hurricane extends Card {

    public Hurricane() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(0, true, true, new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
