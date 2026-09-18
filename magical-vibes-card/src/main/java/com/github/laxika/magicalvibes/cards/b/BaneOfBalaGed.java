package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerChoosesPermanentsToExileEffect;

@CardRegistration(set = "PIO", collectorNumber = "1")
public class BaneOfBalaGed extends Card {

    public BaneOfBalaGed() {
        addEffect(EffectSlot.ON_ATTACK, new DefendingPlayerChoosesPermanentsToExileEffect(2));
    }
}
