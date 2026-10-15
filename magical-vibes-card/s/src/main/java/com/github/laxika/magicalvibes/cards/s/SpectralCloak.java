package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "78")
public class SpectralCloak extends Card {

    public SpectralCloak() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new ConditionalEffect(
                        new EnchantedPermanentMatches(
                                new PermanentNotPredicate(new PermanentIsTappedPredicate()),
                                "enchanted creature is untapped"),
                        new GrantKeywordEffect(Keyword.SHROUD, GrantScope.ENCHANTED_CREATURE)));
    }
}
