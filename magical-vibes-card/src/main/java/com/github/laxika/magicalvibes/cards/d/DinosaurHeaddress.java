package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureBecomesCopyOfExiledCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class DinosaurHeaddress extends Card {

    public DinosaurHeaddress() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new AttachSourceEquipmentToTargetCreatureEffect());
        addEffect(EffectSlot.ON_EQUIPMENT_ATTACHED,
                new AttachedCreatureBecomesCopyOfExiledCreatureEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
