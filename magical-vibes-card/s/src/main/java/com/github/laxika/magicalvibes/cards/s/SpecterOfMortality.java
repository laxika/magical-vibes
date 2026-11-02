package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfOwnGraveyardCardsThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import static com.github.laxika.magicalvibes.model.CardType.CREATURE;

@CardRegistration(set = "WOE", collectorNumber = "107")
@CardRegistration(set = "WOE", collectorNumber = "342")
public class SpecterOfMortality extends Card {

    public SpecterOfMortality() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileAnyNumberOfOwnGraveyardCardsThenEffect(new CardTypePredicate(CREATURE),
                        new BoostAllCreaturesEffect(new Scaled(new EventValue(), -1),
                                new Scaled(new EventValue(), -1),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))));
    }
}
