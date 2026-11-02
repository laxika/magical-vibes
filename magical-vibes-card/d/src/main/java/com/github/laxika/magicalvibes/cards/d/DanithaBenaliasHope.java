package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachAurasToSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "15")
@CardRegistration(set = "DMU", collectorNumber = "287")
public class DanithaBenaliasHope extends Card {

    public DanithaBenaliasHope() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                AttachAurasToSourceEffect.oneAuraOrEquipmentFromHandOrGraveyard());
    }
}
