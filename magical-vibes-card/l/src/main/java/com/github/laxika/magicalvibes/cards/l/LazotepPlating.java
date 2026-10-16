package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "WAR", collectorNumber = "59")
@CardRegistration(set = "SCD", collectorNumber = "57")
public class LazotepPlating extends Card {

    public LazotepPlating() {
        addEffect(EffectSlot.SPELL, new AmassGoblinsEffect(1, CardSubtype.ZOMBIE));
        addEffect(EffectSlot.SPELL, new GrantControllerKeywordUntilEndOfTurnEffect(Keyword.HEXPROOF));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_PERMANENTS));
    }
}
