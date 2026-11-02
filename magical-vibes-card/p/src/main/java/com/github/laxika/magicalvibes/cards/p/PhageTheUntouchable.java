package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "166")
@CardRegistration(set = "LGN", collectorNumber = "78")
@CardRegistration(set = "SLD", collectorNumber = "1195")
public class PhageTheUntouchable extends Card {

    public PhageTheUntouchable() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseGameIfNotCastFromHandEffect());
        addTrigger(Triggers.dealsCombatDamageToCreature(), new DestroyTargetPermanentEffect(true));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new TargetPlayerLosesGameEffect(null));
    }
}
