package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersOfTypeFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "233")
public class QuicksilverFountain extends Card {

    public QuicksilverFountain() {
        var floodedLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentHasCountersPredicate(CounterType.FLOOD)));
        addEffect(EffectSlot.STATIC,
                new GrantSubtypeEffect(CardSubtype.ISLAND, GrantScope.ALL_PERMANENTS, true, floodedLand));

        var targetLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                new PermanentControlledByActivePlayerPredicate()));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                PutCounterOnTargetPermanentEffect.withTargetRestriction(CounterType.FLOOD, 1, targetLand));

        var nonIslandLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ISLAND))));
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new NotCondition(new AnyPlayerControlsPermanent(nonIslandLand)),
                new RemoveAllCountersOfTypeFromAllPermanentsEffect(CounterType.FLOOD)));
    }
}
