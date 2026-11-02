package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "96")
@CardRegistration(set = "SCD", collectorNumber = "83")
public class LazotepReaver extends Card {

    public LazotepReaver() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AmassGoblinsEffect(1, CardSubtype.ZOMBIE));
    }
}
