package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

@CardRegistration(set = "AFC", collectorNumber = "129")
@CardRegistration(set = "C19", collectorNumber = "27")
public class IgniteTheFuture extends Card {

    public IgniteTheFuture() {
        addEffect(EffectSlot.SPELL,
                new ExileTopCardsMayPlayUntilNextTurnEffect(3, false, true));
        addCastingOption(new FlashbackCast("{7}{R}"));
    }
}
