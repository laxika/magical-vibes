package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "USG", collectorNumber = "92")
public class Rescind extends Card {

    public Rescind() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        addCycling("{2}");
    }
}
