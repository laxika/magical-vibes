package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;

public class TimeStop extends Card {

    public TimeStop() {
        addEffect(EffectSlot.SPELL, new EndTurnEffect());
    }
}
