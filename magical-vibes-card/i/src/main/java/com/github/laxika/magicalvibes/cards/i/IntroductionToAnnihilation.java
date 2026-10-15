package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "3")
public class IntroductionToAnnihilation extends Card {

    public IntroductionToAnnihilation() {
        target(TargetFilters.nonlandPermanent()).addEffect(EffectSlot.SPELL,
                new ExileTargetPermanentThenEffect(new DrawCardEffect(1), ThenEffectRecipient.TARGET_CONTROLLER));
    }
}
