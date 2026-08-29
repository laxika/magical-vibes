package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M21", collectorNumber = "35")
public class SecureTheScene extends Card {

    public SecureTheScene() {
        target(TargetFilters.nonlandPermanent()).addEffect(EffectSlot.SPELL,
                new ExileTargetPermanentThenEffect(
                        CreateTokenEffect.whiteSoldier(1),
                        ThenEffectRecipient.TARGET_CONTROLLER));
    }
}
