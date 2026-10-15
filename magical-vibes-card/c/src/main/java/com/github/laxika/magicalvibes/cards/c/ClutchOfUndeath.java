package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SCG", collectorNumber = "61")
public class ClutchOfUndeath extends Card {

    public ClutchOfUndeath() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                        new StaticBoostEffect(3, 3, GrantScope.ENCHANTED_CREATURE),
                        new StaticBoostEffect(-3, -3, GrantScope.ENCHANTED_CREATURE)
                ));
    }
}
