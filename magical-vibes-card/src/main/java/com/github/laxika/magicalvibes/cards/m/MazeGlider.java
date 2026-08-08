package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

@CardRegistration(set = "DGM", collectorNumber = "13")
public class MazeGlider extends Card {

    public MazeGlider() {
        // ALL_OWN_CREATURES so the source is also covered should it ever become multicolored.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLYING, GrantScope.ALL_OWN_CREATURES,
                new PermanentIsMulticoloredPredicate()));
    }
}
