package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "PTK", collectorNumber = "56")
public class SunQuanLordOfWu extends Card {

    public SunQuanLordOfWu() {
        // Creatures you control have horsemanship (including Sun Quan himself).
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.HORSEMANSHIP, GrantScope.ALL_OWN_CREATURES));
    }
}
