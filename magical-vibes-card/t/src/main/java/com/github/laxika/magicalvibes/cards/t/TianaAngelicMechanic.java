package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "28")
public class TianaAngelicMechanic extends Card {

    public TianaAngelicMechanic() {
        // When Tiana or another legendary creature you control crews a Vehicle, that Vehicle perpetually gets
        // +1/+0.
        addTrigger(Triggers.creatureYouControlCrewsVehicle().withObjectFilter(new PermanentAnyOfPredicate(List.of(
                        new PermanentIsSourceCardPredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)))),
                PerpetuallyBoostCardEffect.ofTriggeringPermanent(1, 0));
    }
}
