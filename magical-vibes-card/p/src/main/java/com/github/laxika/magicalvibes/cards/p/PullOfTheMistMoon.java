package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandToPerpetuallyGrantEnterExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "3")
public class PullOfTheMistMoon extends Card {

    public PullOfTheMistMoon() {
        addStaticEffect(new KickerEffect("{1}{U}"));

        // When Pull of the Mist Moon enters, exile target nonland permanent an opponent controls
        // until Pull of the Mist Moon leaves the battlefield.
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT,
                        new ExileTargetPermanentUntilSourceLeavesEffect(
                                false, TargetFilters.nonlandPermanentAnOpponentControls().predicate()));

        // If it was kicked, choose a nonland permanent card in your hand. It perpetually gains the
        // same exile ability when it enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                new ChooseCardFromHandToPerpetuallyGrantEnterExileEffect());
    }
}
