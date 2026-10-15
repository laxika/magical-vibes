package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

@CardRegistration(set = "DGM", collectorNumber = "26")
public class MazeAbomination extends Card {

    public MazeAbomination() {
        // ALL_OWN_CREATURES so the filter is applied uniformly; Maze Abomination is mono-black
        // and therefore never matches itself (its own deathtouch is intrinsic).
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES,
                new PermanentIsMulticoloredPredicate()));
    }
}
