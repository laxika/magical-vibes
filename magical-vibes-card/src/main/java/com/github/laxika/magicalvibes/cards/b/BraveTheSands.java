package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "KTK", collectorNumber = "5")
public class BraveTheSands extends Card {

    public BraveTheSands() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));
    }
}
