package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "660")
public class CloudshredderSliver extends Card {

    public CloudshredderSliver() {
        // Cloudshredder Sliver is itself a Sliver, so ALL_OWN_CREATURES includes the source.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.HASTE),
                GrantScope.ALL_OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
