package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "71")
public class FathomFleetBoarder extends Card {

    public FathomFleetBoarder() {
        // When this creature enters, you lose 2 life unless you control another Pirate.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new NotCondition(new ControlsAnotherPermanent(
                        new PermanentHasSubtypePredicate(CardSubtype.PIRATE))),
                new LoseLifeEffect(2)));
    }
}
