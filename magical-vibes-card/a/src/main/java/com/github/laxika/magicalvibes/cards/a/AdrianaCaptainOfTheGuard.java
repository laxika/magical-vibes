package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DMC", collectorNumber = "139")
@CardRegistration(set = "C20", collectorNumber = "200")
@CardRegistration(set = "ONC", collectorNumber = "114")
public class AdrianaCaptainOfTheGuard extends Card {

    public AdrianaCaptainOfTheGuard() {
        // Other creatures you control have melee.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.MELEE, GrantScope.OWN_CREATURES));
    }
}
