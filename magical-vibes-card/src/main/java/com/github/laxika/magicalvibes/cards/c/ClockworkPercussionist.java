package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

@CardRegistration(set = "DSK", collectorNumber = "130")
public class ClockworkPercussionist extends Card {

    public ClockworkPercussionist() {
        addEffect(EffectSlot.ON_DEATH, new ExileTopCardsMayPlayUntilNextTurnEffect(1));
    }
}
