package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;

@CardRegistration(set = "INR", collectorNumber = "236")
@CardRegistration(set = "INR", collectorNumber = "430")
@CardRegistration(set = "MID", collectorNumber = "224")
public class GalvanicIteration extends Card {

    public GalvanicIteration() {
        addEffect(EffectSlot.SPELL, new CopyNextInstantOrSorceryCastThisTurnEffect());
        addCastingOption(new FlashbackCast("{1}{U}{R}"));
    }
}
