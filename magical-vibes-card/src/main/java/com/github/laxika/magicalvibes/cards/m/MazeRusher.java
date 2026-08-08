package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

@CardRegistration(set = "DGM", collectorNumber = "33")
public class MazeRusher extends Card {

    public MazeRusher() {
        // ALL_OWN_CREATURES so the grant tracks the filter for the source too, should it ever
        // become multicolored (it is mono-red on its own, and has haste printed regardless).
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES,
                new PermanentIsMulticoloredPredicate()));
    }
}
