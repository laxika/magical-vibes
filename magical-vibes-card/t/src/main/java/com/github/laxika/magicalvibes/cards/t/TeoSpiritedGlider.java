package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "74")
public class TeoSpiritedGlider extends Card {

    public TeoSpiritedGlider() {
        target(TargetFilters.creatureYouControl(), 0, 1)
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new ConditionalEffect(
                                new HasAttacker(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                                new DrawDiscardAndConniveEffect(true)));
    }
}
