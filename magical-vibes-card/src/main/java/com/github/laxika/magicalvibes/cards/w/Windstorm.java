package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "M10", collectorNumber = "205")
public class Windstorm extends Card {

    public Windstorm() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(0, true, false, new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
