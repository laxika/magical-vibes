package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "5")
public class CathedralMembrane extends Card {

    public CathedralMembrane() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DealDamageToBlockedAttackersOnDeathEffect(6));
    }
}
