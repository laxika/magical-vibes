package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureOfEnchantedEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEquippedCreatureOfEnchantedEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "148")
public class RuneOfSpeed extends Card {

    public RuneOfSpeed() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        new StaticBoostEffect(1, 0, Set.of(Keyword.HASTE), GrantScope.ENCHANTED_CREATURE),
                        null
                ))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                        new BoostEquippedCreatureOfEnchantedEquipmentEffect(1, 0),
                        null
                ))
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordToEquippedCreatureOfEnchantedEquipmentEffect(Keyword.HASTE));
    }
}
