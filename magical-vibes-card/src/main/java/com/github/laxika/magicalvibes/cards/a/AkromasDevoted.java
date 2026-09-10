package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "2")
public class AkromasDevoted extends Card {

    public AkromasDevoted() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.CLERIC)));
    }
}
