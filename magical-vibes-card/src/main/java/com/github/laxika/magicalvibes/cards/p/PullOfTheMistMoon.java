package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandToPerpetuallyGrantEnterExileEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YDMU", collectorNumber = "3")
public class PullOfTheMistMoon extends Card {

    public PullOfTheMistMoon() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{U}"));

        // When Pull of the Mist Moon enters, exile target nonland permanent an opponent controls
        // until Pull of the Mist Moon leaves the battlefield.
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect(
                                false, TargetFilters.nonlandPermanentAnOpponentControls().predicate()));

        // If it was kicked, choose a nonland permanent card in your hand. It perpetually gains the
        // same exile ability when it enters.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                new ChooseCardFromHandToPerpetuallyGrantEnterExileEffect()));
    }
}
