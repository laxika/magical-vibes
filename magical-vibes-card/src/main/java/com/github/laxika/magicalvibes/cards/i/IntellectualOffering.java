package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentDrawAndUntapEffect;

@CardRegistration(set = "C14", collectorNumber = "15")
public class IntellectualOffering extends Card {

    public IntellectualOffering() {
        addEffect(EffectSlot.SPELL, new ChooseOpponentDrawAndUntapEffect());
    }
}
