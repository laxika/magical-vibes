package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "M10", collectorNumber = "134")
@CardRegistration(set = "POR", collectorNumber = "124")
@CardRegistration(set = "P02", collectorNumber = "94")
@CardRegistration(set = "7ED", collectorNumber = "180")
@CardRegistration(set = "6ED", collectorNumber = "173")
@CardRegistration(set = "5ED", collectorNumber = "223")
public class Earthquake extends Card {

    public Earthquake() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(0, true, true, new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
