package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "186")
public class GreenDragon extends Card {

    public GreenDragon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RegisterGlobalTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.creatureOpponentControlsDealtDamage(),
                        new DestroyTargetPermanentEffect())));
    }
}
