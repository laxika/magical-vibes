package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "STX", collectorNumber = "68")
public class CrushingDisappointment extends Card {

    public CrushingDisappointment() {
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2, LoseLifeRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
