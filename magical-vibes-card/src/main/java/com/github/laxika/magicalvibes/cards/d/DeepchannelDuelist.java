package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "213")
@CardRegistration(set = "ECL", collectorNumber = "333")
public class DeepchannelDuelist extends Card {

    public DeepchannelDuelist() {
        var merfolk = new PermanentHasSubtypePredicate(CardSubtype.MERFOLK);

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, merfolk));

        var controlledMerfolk = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                merfolk,
                new PermanentControlledBySourceControllerPredicate()
        ));
        target(new PermanentPredicateTargetFilter(controlledMerfolk,
                "Target must be a Merfolk creature you control"))
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new UntapPermanentsEffect(TapUntapScope.TARGET, merfolk));
    }
}
