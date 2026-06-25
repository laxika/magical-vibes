package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DKA", collectorNumber = "101")
public class PyreheartWolf extends Card {

    public PyreheartWolf() {
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(Keyword.MENACE, GrantScope.OWN_CREATURES));
    }
}
