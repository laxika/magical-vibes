package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "40K", collectorNumber = "145")
public class TyranidPrime extends Card {

    public TyranidPrime() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.EVOLVE, GrantScope.OWN_CREATURES));
    }
}
