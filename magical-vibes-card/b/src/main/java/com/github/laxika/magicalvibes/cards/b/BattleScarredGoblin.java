package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "192")
public class BattleScarredGoblin extends Card {

    private static final PermanentPredicate CREATURES_BLOCKING_SOURCE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentBlockingSourcePredicate()
    ));

    public BattleScarredGoblin() {
        // Whenever this creature becomes blocked, it deals 1 damage to each creature blocking it.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DealDamageToEachMatchingPermanentEffect(1, CREATURES_BLOCKING_SOURCE,
                        EachPermanentScope.ALL_PLAYERS));
    }
}
