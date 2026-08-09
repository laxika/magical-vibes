package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "23")
public class SpiritualAsylum extends Card {

    public SpiritualAsylum() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.SHROUD,
                GrantScope.OWN_PERMANENTS,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate()))));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new SacrificeSelfEffect());
    }
}
