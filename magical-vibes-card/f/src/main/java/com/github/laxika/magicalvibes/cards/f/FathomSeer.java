package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "62")
@CardRegistration(set = "DD2", collectorNumber = "3")
@CardRegistration(set = "JVC", collectorNumber = "3")
@CardRegistration(set = "DDN", collectorNumber = "45")
@CardRegistration(set = "A25", collectorNumber = "56")
@CardRegistration(set = "TSR", collectorNumber = "68")
@CardRegistration(set = "C14", collectorNumber = "109")
public class FathomSeer extends Card {

    public FathomSeer() {
        addMorph("{0}", new ReturnPermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new DrawCardEffect(2));
    }
}
