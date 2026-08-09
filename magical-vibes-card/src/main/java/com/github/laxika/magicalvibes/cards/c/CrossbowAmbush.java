package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "STH", collectorNumber = "105")
public class CrossbowAmbush extends Card {

    public CrossbowAmbush() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.REACH, GrantScope.OWN_CREATURES));
    }
}
