package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToOwnTappedCreaturesEffect;

public class AdeptWatershaper extends Card {

    public AdeptWatershaper() {
        addEffect(EffectSlot.STATIC, new GrantKeywordToOwnTappedCreaturesEffect(Keyword.INDESTRUCTIBLE));
    }
}
