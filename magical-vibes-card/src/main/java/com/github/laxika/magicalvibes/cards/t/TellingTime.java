package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "114")
public class TellingTime extends Card {

    public TellingTime() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsHandTopBottomEffect(3));
    }
}
