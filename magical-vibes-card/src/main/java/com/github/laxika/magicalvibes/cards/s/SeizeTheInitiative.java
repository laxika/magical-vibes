package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "SOM", collectorNumber = "20")
public class SeizeTheInitiative extends Card {

    public SeizeTheInitiative() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 1));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));
    }
}
