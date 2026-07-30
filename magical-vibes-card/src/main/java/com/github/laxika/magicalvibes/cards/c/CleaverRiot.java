package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "M13", collectorNumber = "125")
public class CleaverRiot extends Card {

    public CleaverRiot() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.OWN_CREATURES));
    }
}
