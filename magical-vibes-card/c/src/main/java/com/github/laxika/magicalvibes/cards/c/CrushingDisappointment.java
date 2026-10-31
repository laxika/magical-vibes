package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "STX", collectorNumber = "68")
public class CrushingDisappointment extends Card {

    public CrushingDisappointment() {
        addSpellEffect(new LoseLifeEffect(2, LoseLifeRecipient.EACH_PLAYER));
        addSpellEffect(new DrawCardEffect(2));
    }
}
