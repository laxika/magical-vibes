package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "157")
public class RageThrower extends Card {

    public RageThrower() {
        // Whenever another creature dies, Rage Thrower deals 2 damage to target player or planeswalker.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER), new DealDamageToTargetPlayerOrPlaneswalkerEffect(2));
    }
}
