package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "TLA", collectorNumber = "261")
public class TheWallsOfBaSingSe extends Card {

    public TheWallsOfBaSingSe() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS));
    }
}
