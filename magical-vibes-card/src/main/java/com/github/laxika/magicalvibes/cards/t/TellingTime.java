package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;

public class TellingTime extends Card {

    public TellingTime() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsHandTopBottomEffect(3));
    }
}
