package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersOfTypeFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "222")
public class OblivionStone extends Card {

    public OblivionStone() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.FATE)),
                "{4}, {T}: Put a fate counter on target permanent.",
                TargetFilters.permanent()
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new SacrificeSelfCost(),
                        SequenceEffect.of(
                                new DestroyAllPermanentsEffect(
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                                new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.FATE))
                                        )),
                                        false,
                                        EachPermanentScope.ALL_PLAYERS,
                                        null,
                                        false),
                                new RemoveAllCountersOfTypeFromAllPermanentsEffect(CounterType.FATE)
                        )
                ),
                "{5}, {T}, Sacrifice this artifact: Destroy each nonland permanent without a fate counter on it, then remove all fate counters from all permanents."
        ));
    }
}
