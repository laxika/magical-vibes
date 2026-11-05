package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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

        var putCounterOnTargetPermanent = new PutCounterOnTargetPermanentEffect(CounterType.EVERYTHING);
        var putCounterOnTargetPermanent3 = new PutCounterOnTargetPermanentEffect(CounterType.EVERYTHING);
        target(TargetFilters.land(), 0, 1)
                .bindEffect(putCounterOnTargetPermanent)
                .bindEffect(putCounterOnTargetPermanent3);
        var putCounterOnTargetPermanent2 = new PutCounterOnTargetPermanentEffect(CounterType.EVERYTHING);
        var putCounterOnTargetPermanent4 = new PutCounterOnTargetPermanentEffect(CounterType.EVERYTHING);
        target(TargetFilters.creature(), 0, 1)
                .bindEffect(putCounterOnTargetPermanent2)
                .bindEffect(putCounterOnTargetPermanent4);
        addTrigger(Triggers.attacks(), putCounterOnTargetPermanent3, putCounterOnTargetPermanent4);
        addTrigger(Triggers.enters(ObjectRelation.THIS), putCounterOnTargetPermanent, putCounterOnTargetPermanent2);

        addStaticEffect(new GrantAllCreatureTypesToOwnCreaturesEffect(
                GrantScope.ALL_CREATURES_INCLUDING_SELF, NONLAND_CREATURE));
        CardSubtype.rulesLandTypes().forEach(subtype -> addStaticEffect(new GrantSubtypeEffect(subtype, GrantScope.ALL_PERMANENTS, false, LAND)));
    }
}
