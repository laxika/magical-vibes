package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetWhileHasCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersOfTypeFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "233")
public class QuicksilverFountain extends Card {

    public QuicksilverFountain() {
        var targetLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                new PermanentControlledByActivePlayerPredicate()));
        target(new PermanentPredicateTargetFilter(targetLand,
                "Target must be a non-Island land controlled by the active player", true))
                .addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, SequenceEffect.of(
                PutCounterOnTargetPermanentEffect.withTargetRestriction(CounterType.FLOOD, 1, targetLand),
                new GrantSubtypeToTargetWhileHasCounterEffect(CardSubtype.ISLAND, CounterType.FLOOD, true)));

        var nonIslandLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ISLAND))));
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new NotCondition(new AnyPlayerControlsPermanent(nonIslandLand)),
                new RemoveAllCountersOfTypeFromAllPermanentsEffect(CounterType.FLOOD)));
    }
}
