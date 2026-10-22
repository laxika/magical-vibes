package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedOrBlockedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AKH", collectorNumber = "37")
@CardRegistration(set = "AKR", collectorNumber = "44")
public class VizierOfDeferment extends Card {

    public VizierOfDeferment() {
        // Flash (auto-loaded). When Vizier of Deferment enters, you may exile target creature. If that
        // creature didn't attack or block this turn, nothing is exiled (the check is made on resolution).
        // Return that card to the battlefield under its owner's control at the beginning of the next end step.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(ConditionalEffect.unless(
                                new TargetPermanentMatches(new PermanentAttackedOrBlockedThisTurnPredicate()),
                                FlickerEffect.exileTargetReturnAtEndStep()),
                                "Exile target creature that attacked or blocked this turn?"));
    }
}
