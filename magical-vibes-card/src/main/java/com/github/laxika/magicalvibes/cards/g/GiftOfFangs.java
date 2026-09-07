package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOW", collectorNumber = "113")
public class GiftOfFangs extends Card {

    public GiftOfFangs() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE),
                new StaticBoostEffect(-2, -2, GrantScope.ENCHANTED_CREATURE)));
    }
}
