package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "183")
@CardRegistration(set = "LCI", collectorNumber = "325")
public class EarthshakerDreadmaw extends Card {

    public EarthshakerDreadmaw() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR), CountScope.CONTROLLER, true)));
    }
}
