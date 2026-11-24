package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "110")
public class VulshokBattlemaster extends Card {

    public VulshokBattlemaster() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AttachSourceEquipmentToTargetCreatureEffect(
                Subjects.controlledBy(Subjects.eachPlayer(), new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)),
                Subjects.source()));
    }
}
