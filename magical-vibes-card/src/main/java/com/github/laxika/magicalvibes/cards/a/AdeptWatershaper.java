package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ECL", collectorNumber = "3")
public class AdeptWatershaper extends Card {

    public AdeptWatershaper() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Scope.OWN_TAPPED_CREATURES));
    }
}
