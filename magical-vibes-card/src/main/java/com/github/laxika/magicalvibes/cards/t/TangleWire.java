package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterOrSacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsForAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "139")
public class TangleWire extends Card {

    private static final PermanentPredicate ARTIFACT_CREATURE_OR_LAND = new PermanentAnyOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentIsCreaturePredicate(),
            new PermanentIsLandPredicate()));

    public TangleWire() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.FADE, new Fixed(4)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterOrSacrificeSelfEffect(CounterType.FADE));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new TapPermanentsForAmountEffect(new CountersOnSource(CounterType.FADE),
                        ARTIFACT_CREATURE_OR_LAND));
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new TapPermanentsForAmountEffect(new CountersOnSource(CounterType.FADE),
                        ARTIFACT_CREATURE_OR_LAND));
    }
}
