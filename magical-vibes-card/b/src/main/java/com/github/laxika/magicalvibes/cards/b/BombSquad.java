package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "179")
public class BombSquad extends Card {

    public BombSquad() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.FUSE)),
                "{T}: Put a fuse counter on target creature.",
                TargetFilters.creature()));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCounterOnEachMatchingPermanentEffect(
                CounterType.FUSE,
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasCountersPredicate(CounterType.FUSE))),
                EachPermanentScope.ALL_PLAYERS));

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> true,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasAtLeastCountersPredicate(CounterType.FUSE, 4))),
                List.of(new RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffect(
                        PermanentReference.TRIGGERING, CounterType.FUSE, 4)),
                "Bomb Squad's fuse trigger"));
    }
}
