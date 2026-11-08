package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "158")
public class RoninWarclub extends Card {

    public RoninWarclub() {
        addStaticEffect(new StaticBoostEffect(2, 1, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature());
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
