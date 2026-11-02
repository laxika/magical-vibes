package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "143")
@CardRegistration(set = "TMT", collectorNumber = "242")
public class DonLeoProblemSolvers extends Card {

    public DonLeoProblemSolvers() {
        setAllowSharedTargets(true);
        FlickerEffect flicker = FlickerEffect.flickerTarget();
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact you control"), 0, 1)
                .addTrigger(Triggers.endStep(ActivePlayer.YOU), flicker);
        target(TargetFilters.creatureYouControl(), 0, 1).bindEffect(flicker);
    }
}
