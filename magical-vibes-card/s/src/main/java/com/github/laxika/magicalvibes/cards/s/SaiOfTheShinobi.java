package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PC2", collectorNumber = "113")
@CardRegistration(set = "A25", collectorNumber = "230")
@CardRegistration(set = "PCA", collectorNumber = "113")
public class SaiOfTheShinobi extends Card {

    public SaiOfTheShinobi() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new AttachSourceEquipmentToEnteringCreatureEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
