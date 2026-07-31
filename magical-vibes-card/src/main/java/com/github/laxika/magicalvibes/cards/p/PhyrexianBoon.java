package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "58a")
@CardRegistration(set = "ALL", collectorNumber = "58b")
public class PhyrexianBoon extends Card {

    public PhyrexianBoon() {
        target(TargetFilters.creature())
                // Enchanted creature gets +2/+1 as long as it's black. Otherwise, it gets -1/-2.
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                        new StaticBoostEffect(2, 1, Set.of(), GrantScope.ENCHANTED_CREATURE),
                        new StaticBoostEffect(-1, -2, Set.of(), GrantScope.ENCHANTED_CREATURE)
                ));
    }
}
