package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GoadEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "163")
public class RedemptionArc extends Card {

    public RedemptionArc() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GoadEquippedCreatureEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new ExileEnchantedCreatureEffect()),
                "{1}{W}: Exile enchanted creature."
        ));
    }
}
