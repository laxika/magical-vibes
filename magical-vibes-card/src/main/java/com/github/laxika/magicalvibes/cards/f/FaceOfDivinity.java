package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastAttachedAurasPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "8")
public class FaceOfDivinity extends Card {

    public FaceOfDivinity() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentHasAtLeastAttachedAurasPredicate(2),
                        new GrantKeywordEffect(
                                Set.of(Keyword.FIRST_STRIKE, Keyword.LIFELINK),
                                GrantScope.ENCHANTED_CREATURE),
                        null));
    }
}
