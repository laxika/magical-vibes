package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageDealtByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "4")
public class ArmamentOfNyx extends Card {

    public ArmamentOfNyx() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentIsEnchantmentPredicate(),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.ENCHANTED_CREATURE),
                        new PreventAllDamageDealtByEnchantedCreatureEffect()
                ));
    }
}
