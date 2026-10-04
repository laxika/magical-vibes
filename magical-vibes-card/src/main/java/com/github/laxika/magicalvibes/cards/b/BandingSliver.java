package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MB1", collectorNumber = "2")
public class BandingSliver extends Card {

    public BandingSliver() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.BANDING, GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
