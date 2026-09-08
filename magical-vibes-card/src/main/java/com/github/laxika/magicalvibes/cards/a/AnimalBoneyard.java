package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "4")
public class AnimalBoneyard extends Card {

    public AnimalBoneyard() {
        // Enchanted land has "{T}, Sacrifice a creature: You gain life equal to the sacrificed
        // creature's toughness."
        target(TargetFilters.land()).addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(
                                new SacrificeCreatureCost(false, false, true),
                                new GainLifeEffect(new XValue())
                        ),
                        "{T}, Sacrifice a creature: You gain life equal to the sacrificed creature's toughness."
                ),
                GrantScope.ENCHANTED_PERMANENT
        ));
    }
}
