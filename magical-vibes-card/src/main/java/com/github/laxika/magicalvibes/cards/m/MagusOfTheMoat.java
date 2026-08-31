package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "FUT", collectorNumber = "12")
public class MagusOfTheMoat extends Card {

    public MagusOfTheMoat() {
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackUnlessPredicateEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
