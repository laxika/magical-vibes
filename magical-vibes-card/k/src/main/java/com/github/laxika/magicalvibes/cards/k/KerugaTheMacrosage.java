package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "225")
@CardRegistration(set = "MUL", collectorNumber = "48")
@CardRegistration(set = "MUL", collectorNumber = "113")
@CardRegistration(set = "MUL", collectorNumber = "178")
@CardRegistration(set = "MB2", collectorNumber = "84")
public class KerugaTheMacrosage extends Card {

    public KerugaTheMacrosage() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(
                new PermanentCount(new PermanentMinManaValuePredicate(3), CountScope.CONTROLLER, true)));
    }
}
