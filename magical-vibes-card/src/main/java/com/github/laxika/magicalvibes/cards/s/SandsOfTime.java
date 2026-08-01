package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayersSkipUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.SimultaneouslyFlipControlledPermanentsTapStatesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "153")
public class SandsOfTime extends Card {

    private static final PermanentPredicate ARTIFACT_CREATURE_OR_LAND = new PermanentAnyOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentIsCreaturePredicate(),
            new PermanentIsLandPredicate()));

    public SandsOfTime() {
        // Each player skips their untap step (including phasing — CR 502.1).
        addEffect(EffectSlot.STATIC, new PlayersSkipUntapStepEffect());

        // At the beginning of each player's upkeep, simultaneously flip tap states of that
        // player's artifacts, creatures, and lands.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new SimultaneouslyFlipControlledPermanentsTapStatesEffect(ARTIFACT_CREATURE_OR_LAND));
    }
}
