package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "TSP", collectorNumber = "6")
public class CavalryMaster extends Card {

    public CavalryMaster() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FLANKING,
                GrantScope.OWN_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.FLANKING)));
    }
}
