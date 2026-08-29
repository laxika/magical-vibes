package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "SOI", collectorNumber = "172")
public class MagmaticChasm extends Card {

    public MagmaticChasm() {
        addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
        ));
    }
}
