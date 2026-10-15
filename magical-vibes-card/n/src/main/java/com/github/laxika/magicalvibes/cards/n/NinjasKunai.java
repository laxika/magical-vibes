package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSourceEquipmentCost;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "252")
public class NinjasKunai extends Card {

    public NinjasKunai() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{1}",
                        List.of(new SacrificeSourceEquipmentCost(), new DealDamageToAnyTargetEffect(3)),
                        "{1}, {T}, Sacrifice Ninja's Kunai: Ninja's Kunai deals 3 damage to any target."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
