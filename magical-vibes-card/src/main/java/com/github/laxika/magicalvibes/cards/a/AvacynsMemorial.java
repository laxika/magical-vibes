package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "MIC", collectorNumber = "31")
@CardRegistration(set = "MIC", collectorNumber = "69")
public class AvacynsMemorial extends Card {

    public AvacynsMemorial() {
        // Other legendary permanents you control have indestructible.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.INDESTRUCTIBLE,
                GrantScope.OWN_PERMANENTS,
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
    }
}
