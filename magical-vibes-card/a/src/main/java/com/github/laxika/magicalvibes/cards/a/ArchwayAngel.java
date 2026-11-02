package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "3")
@CardRegistration(set = "PIO", collectorNumber = "5")
public class ArchwayAngel extends Card {

    public ArchwayAngel() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(new Scaled(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.GATE), CountScope.CONTROLLER), 2)));
    }
}
