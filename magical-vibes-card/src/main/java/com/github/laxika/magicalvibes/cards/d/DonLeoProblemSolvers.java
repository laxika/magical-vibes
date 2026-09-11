package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMT", collectorNumber = "143")
@CardRegistration(set = "TMT", collectorNumber = "242")
public class DonLeoProblemSolvers extends Card {

    public DonLeoProblemSolvers() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact you control"), 0, 1)
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, FlickerEffect.flickerTarget());
        target(TargetFilters.creatureYouControl(), 0, 1)
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, FlickerEffect.flickerTarget());
    }
}
