package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "113")
@CardRegistration(set = "TLE", collectorNumber = "192")
public class ChongAndLilyNomads extends Card {

    private static final String PUT_LORE_COUNTERS =
            "Put a lore counter on each of any number of target Sagas you control.";
    private static final String BOOST_CREATURES =
            "Creatures you control get +1/+0 until end of turn for each lore counter among Sagas you control.";

    public ChongAndLilyNomads() {
        var saga = new PermanentHasSubtypePredicate(CardSubtype.SAGA);
        var sagaTarget = new ControlledPermanentPredicateTargetFilter(saga, "Target must be a Saga you control");
        var loreCountersOnSagas = new PermanentCounterSum(CounterType.LORE, saga, CountScope.CONTROLLER);
        var choice = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        PUT_LORE_COUNTERS,
                        List.of(new PutCounterOnTargetPermanentEffect(CounterType.LORE)),
                        sagaTarget, null, 0, 99, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        BOOST_CREATURES,
                        new BoostAllOwnCreaturesEffect(loreCountersOnSagas, new Fixed(0)))));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(
                        new MinimumMatchingAttackers(1, new PermanentHasSubtypePredicate(CardSubtype.BARD)),
                        new ChooseOneAtTriggerTimeEffect(choice)));
    }
}
