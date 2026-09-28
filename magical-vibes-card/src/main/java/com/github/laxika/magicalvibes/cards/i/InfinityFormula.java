package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "601")
public class InfinityFormula extends Card {

    public InfinityFormula() {
        // When this Equipment enters, attach it to target creature you control.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AttachSourceEquipmentToTargetCreatureEffect());

        // Equipped creature gets +1/+2 and has "Whenever this creature attacks, you gain 2 life."
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ATTACK, new GainLifeEffect(2), GrantScope.EQUIPPED_CREATURE));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
