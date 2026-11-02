package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureBecomesCopyOfExiledCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class DinosaurHeaddress extends Card {

    public DinosaurHeaddress() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new AttachSourceEquipmentToTargetCreatureEffect());
        addTrigger(Triggers.becomesAttachedToCreature(),
                new AttachedCreatureBecomesCopyOfExiledCreatureEffect(true));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
