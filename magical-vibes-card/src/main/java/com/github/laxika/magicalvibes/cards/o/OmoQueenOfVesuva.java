package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "2")
@CardRegistration(set = "M3C", collectorNumber = "14")
@CardRegistration(set = "M3C", collectorNumber = "22")
@CardRegistration(set = "M3C", collectorNumber = "30")
@CardRegistration(set = "M3C", collectorNumber = "141")
@CardRegistration(set = "M3C", collectorNumber = "145")
@CardRegistration(set = "M3C", collectorNumber = "149")
public class OmoQueenOfVesuva extends Card {

    private static final PermanentPredicate NONLAND_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsLandPredicate()),
            new PermanentHasCountersPredicate(CounterType.EVERYTHING)));

    private static final PermanentPredicate LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentHasCountersPredicate(CounterType.EVERYTHING)));

    public OmoQueenOfVesuva() {
        setAllowSharedTargets(true);

        target(TargetFilters.land(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.EVERYTHING))
                .addEffect(EffectSlot.ON_ATTACK,
                        new PutCounterOnTargetPermanentEffect(CounterType.EVERYTHING));
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.EVERYTHING))
                .addEffect(EffectSlot.ON_ATTACK,
                        new PutCounterOnTargetPermanentEffect(CounterType.EVERYTHING));

        addEffect(EffectSlot.STATIC, new GrantAllCreatureTypesToOwnCreaturesEffect(
                GrantScope.ALL_CREATURES_INCLUDING_SELF, NONLAND_CREATURE));
        CardSubtype.landTypes().forEach(subtype -> addEffect(EffectSlot.STATIC,
                new GrantSubtypeEffect(subtype, GrantScope.ALL_PERMANENTS, false, LAND)));
    }
}
