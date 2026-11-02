package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachAllEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "110")
public class VulshokBattlemaster extends Card {

    public VulshokBattlemaster() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AttachAllEquipmentToSourceEffect());
    }
}
