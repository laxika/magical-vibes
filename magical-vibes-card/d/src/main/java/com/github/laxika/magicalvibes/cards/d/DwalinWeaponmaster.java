package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "154")
public class DwalinWeaponmaster extends Card {

    public DwalinWeaponmaster() {
        PutCounterOnEachControlledPermanentEffect honeEquipment =
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.HONE, 1, new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT));
        addTrigger(Triggers.enters(ObjectRelation.THIS), honeEquipment);
        addTrigger(Triggers.attacks(), honeEquipment);
    }
}
