package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachCreatedEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndSacrificeAttachedEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "22")
public class OutfittedJouster extends Card {

    public OutfittedJouster() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new ConjureCardToBattlefieldEffect("ELD", "202"),
                new ConjureCardToBattlefieldEffect("M15", "213"),
                new AttachCreatedEquipmentToSourceEffect()));

        addStaticEffect(new PreventDamageAndSacrificeAttachedEquipmentEffect());
    }
}
