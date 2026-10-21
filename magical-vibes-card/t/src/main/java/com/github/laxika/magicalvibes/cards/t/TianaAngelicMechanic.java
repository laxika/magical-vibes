package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "28")
public class TianaAngelicMechanic extends Card {

    public TianaAngelicMechanic() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_CREWS_VEHICLE,
                new PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsSourceCardPredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))), 1, 0));
    }
}
