package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEquippedCreatureOfEnchantedEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KHM", collectorNumber = "75")
public class RuneOfFlight extends Card {

    public RuneOfFlight() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1))
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                        Keyword.FLYING, GrantScope.ENCHANTED_PERMANENT, new PermanentIsCreaturePredicate()))
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordToEquippedCreatureOfEnchantedEquipmentEffect(Keyword.FLYING));
    }
}
