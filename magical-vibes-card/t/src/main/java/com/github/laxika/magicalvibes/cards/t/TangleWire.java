package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "139")
@CardRegistration(set = "V13", collectorNumber = "8")
@CardRegistration(set = "SLD", collectorNumber = "694")
public class TangleWire extends Card {

    private static final PermanentPredicate ARTIFACT_CREATURE_OR_LAND = new PermanentAnyOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentIsCreaturePredicate(),
            new PermanentIsLandPredicate()));

    public TangleWire() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.FADE, new Fixed(4)));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterOrSacrificeSelfEffect(CounterType.FADE));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new TapPermanentsForAmountEffect(new CountersOnSource(CounterType.FADE),
                        ARTIFACT_CREATURE_OR_LAND));
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                new TapPermanentsForAmountEffect(new CountersOnSource(CounterType.FADE),
                        ARTIFACT_CREATURE_OR_LAND));
    }
}
