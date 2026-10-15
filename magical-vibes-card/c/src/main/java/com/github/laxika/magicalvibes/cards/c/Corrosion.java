package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersOfTypeFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostOwnCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "128")
public class Corrosion extends Card {

    public Corrosion() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));

        // Put a rust counter on each artifact target opponent controls. Then destroy each artifact
        // with mana value ≤ rust counters on it (can't be regenerated). Ruling: only artifacts that
        // actually have rust counters are destroyed (covers own artifacts that somehow hold rust).
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.RUST, 1,
                        new PermanentIsArtifactPredicate(),
                        EachPermanentScope.TARGET_PLAYER),
                new DestroyAllPermanentsEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentHasCountersPredicate(CounterType.RUST),
                                new PermanentManaValueAtMostOwnCountersPredicate(CounterType.RUST)
                        )),
                        true)
        ));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new RemoveAllCountersOfTypeFromAllPermanentsEffect(CounterType.RUST));
    }
}
