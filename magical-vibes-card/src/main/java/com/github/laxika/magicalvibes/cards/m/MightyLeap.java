package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "22")
public class MightyLeap extends Card {

    public MightyLeap() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
    }
}
