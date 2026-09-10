package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZNR", collectorNumber = "113")
public class MindCarver extends Card {

    public MindCarver() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AttachSourceEquipmentToTargetCreatureEffect());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentGraveyardAtLeast(8),
                new StaticBoostEffect(2, 1, GrantScope.EQUIPPED_CREATURE)));
        addActivatedAbility(new EquipActivatedAbility("{2}{B}"));
    }
}
