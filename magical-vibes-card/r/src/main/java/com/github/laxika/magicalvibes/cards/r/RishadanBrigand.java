package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "MMQ", collectorNumber = "92")
public class RishadanBrigand extends Card {

    public RishadanBrigand() {
        addEffect(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING), "creatures with flying"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachOpponentSacrificesPermanentUnlessPaysEffect("{3}"));
    }
}
