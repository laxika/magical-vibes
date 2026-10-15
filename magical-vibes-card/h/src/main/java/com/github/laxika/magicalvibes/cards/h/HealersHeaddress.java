package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "129")
public class HealersHeaddress extends Card {

    public HealersHeaddress() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(PreventDamageEffect.nextToTarget(1)),
                        "{T}: Prevent the next 1 damage that would be dealt to any target this turn."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{W}",
                List.of(new EquipEffect()),
                "{W}{W}: Attach this Equipment to target creature you control.",
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
