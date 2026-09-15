package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GoadEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GN3", collectorNumber = "114")
public class BloodthirstyBlade extends Card {

    public BloodthirstyBlade() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GoadEquippedCreatureEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new EquipEffect()),
                "Equip {1}",
                TargetFilters.creatureAnOpponentControls(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
