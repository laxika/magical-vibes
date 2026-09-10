package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "DSK", collectorNumber = "87")
public class CommuneWithEvil extends Card {

    public CommuneWithEvil() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(4, 1));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
