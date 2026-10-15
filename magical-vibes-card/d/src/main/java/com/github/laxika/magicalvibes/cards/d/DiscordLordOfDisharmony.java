package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscordCopyCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscordRandomCardCopyEffect;

@CardRegistration(set = "SLD", collectorNumber = "798")
public class DiscordLordOfDisharmony extends Card {

    public DiscordLordOfDisharmony() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new DiscordRandomCardCopyEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new DiscordCopyCastTriggerEffect());
    }
}
