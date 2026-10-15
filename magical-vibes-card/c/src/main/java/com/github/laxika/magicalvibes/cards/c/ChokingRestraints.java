package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "16")
public class ChokingRestraints extends Card {

    public ChokingRestraints() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{W}",
                List.of(new SacrificeSelfCost(), new ExileEnchantedCreatureEffect()),
                "{3}{W}{W}, Sacrifice this Aura: Exile enchanted creature."
        ));
    }
}
