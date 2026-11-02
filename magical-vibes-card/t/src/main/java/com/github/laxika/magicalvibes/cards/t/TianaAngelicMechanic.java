package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "28")
public class TianaAngelicMechanic extends Card {

    public TianaAngelicMechanic() {
        addTrigger(Triggers.creatureYouControlCrewsVehicle(),
                new PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsSourceCardPredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))), 1, 0));
    }
}
