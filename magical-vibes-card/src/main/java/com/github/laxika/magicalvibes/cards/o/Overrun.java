package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

@CardRegistration(set = "10E", collectorNumber = "284")
public class Overrun extends Card {

    public Overrun() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(3, 3));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.TRAMPLE, GrantKeywordEffect.Scope.OWN_CREATURES));
    }
}
