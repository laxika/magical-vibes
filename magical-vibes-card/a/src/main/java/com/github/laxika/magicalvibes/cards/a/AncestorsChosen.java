package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "1")
@CardRegistration(set = "JUD", collectorNumber = "1")
@CardRegistration(set = "UMA", collectorNumber = "9")
public class AncestorsChosen extends Card {

    public AncestorsChosen() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GainLifeEffect(new CardsInGraveyard(null, CountScope.CONTROLLER)));
    }
}
