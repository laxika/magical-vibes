package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PlayersSkipUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.SimultaneouslyFlipControlledPermanentsTapStatesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "153")
public class SandsOfTime extends Card {

    private static final PermanentPredicate ARTIFACT_CREATURE_OR_LAND = new PermanentAnyOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentIsCreaturePredicate(),
            new PermanentIsLandPredicate()));

    public SandsOfTime() {
        // Each player skips their untap step (including phasing — CR 502.1).
        addStaticEffect(new PlayersSkipUntapStepEffect());

        // At the beginning of each player's upkeep, simultaneously flip tap states of that
        // player's artifacts, creatures, and lands.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new SimultaneouslyFlipControlledPermanentsTapStatesEffect(ARTIFACT_CREATURE_OR_LAND));
    }
}
