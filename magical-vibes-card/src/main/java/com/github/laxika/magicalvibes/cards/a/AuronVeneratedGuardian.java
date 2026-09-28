package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfThenReflexiveEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "10")
@CardRegistration(set = "FIC", collectorNumber = "130")
public class AuronVeneratedGuardian extends Card {

    public AuronVeneratedGuardian() {
        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByDefendingPlayerPredicate(),
                new PermanentPowerLessThanSourcePowerPredicate()));

        addEffect(EffectSlot.ON_ATTACK, new PutCountersOnSelfThenReflexiveEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new ExileTargetPermanentUntilSourceLeavesEffect(false, targetFilter)));
    }
}
