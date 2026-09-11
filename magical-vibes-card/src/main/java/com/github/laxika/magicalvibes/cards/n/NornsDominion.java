package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersOfTypeFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OPC2", collectorNumber = "27")
public class NornsDominion extends Card {

    public NornsDominion() {
        PermanentAllOfPredicate unmarkedNonland = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.FATE))
        ));

        addEffect(EffectSlot.PLANESWALK_FROM_TRIGGERED, SequenceEffect.of(
                new DestroyAllPermanentsEffect(unmarkedNonland, false, EachPermanentScope.ALL_PLAYERS, null, false),
                new RemoveAllCountersOfTypeFromAllPermanentsEffect(CounterType.FATE)));

        target(TargetFilters.permanent()).addEffect(EffectSlot.CHAOS_TRIGGERED,
                new MayEffect(new PutCounterOnTargetPermanentEffect(CounterType.FATE),
                        "Put a fate counter on target permanent?"));
    }
}
