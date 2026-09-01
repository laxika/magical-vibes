package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "86")
@CardRegistration(set = "FIN", collectorNumber = "443")
@CardRegistration(set = "FIN", collectorNumber = "577")
public class YshtolaRhul extends Card {

    public YshtolaRhul() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                "Target must be a creature you control"
        )).addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                FlickerEffect.flickerTargetWithAdditionalEndStep());
    }
}
