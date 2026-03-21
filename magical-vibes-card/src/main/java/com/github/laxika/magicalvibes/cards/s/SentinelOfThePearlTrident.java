package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "65")
public class SentinelOfThePearlTrident extends Card {

    public SentinelOfThePearlTrident() {
        // When Sentinel of the Pearl Trident enters, you may exile target historic
        // permanent you control. If you do, return that card to the battlefield under
        // its owner's control at the beginning of the next end step.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsHistoricPredicate()
                )),
                "Target must be a historic permanent you control"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExileTargetPermanentAndReturnAtEndStepEffect(),
                        "Exile target historic permanent you control?"));
    }
}
