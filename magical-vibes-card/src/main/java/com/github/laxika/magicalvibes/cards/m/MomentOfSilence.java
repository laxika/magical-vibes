package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.SkipRecipient;

@CardRegistration(set = "MMQ", collectorNumber = "28")
public class MomentOfSilence extends Card {

    public MomentOfSilence() {
        // Target player skips their next combat phase this turn.
        addEffect(EffectSlot.SPELL,
                new SkipNextEffect(SkipKind.COMBAT_PHASE, SkipRecipient.TARGET_PLAYER));
    }
}
