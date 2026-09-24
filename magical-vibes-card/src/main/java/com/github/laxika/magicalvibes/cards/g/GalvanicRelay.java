package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

@CardRegistration(set = "MH2", collectorNumber = "127")
public class GalvanicRelay extends Card {

    public GalvanicRelay() {
        addEffect(EffectSlot.SPELL, new ExileTopCardsMayPlayUntilNextTurnEffect(1));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
