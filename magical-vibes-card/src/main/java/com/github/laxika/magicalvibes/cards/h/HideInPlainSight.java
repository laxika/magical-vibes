package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "MKM", collectorNumber = "166")
@CardRegistration(set = "MKM", collectorNumber = "410")
public class HideInPlainSight extends Card {

    public HideInPlainSight() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.cloakTwoFromTopFive());
    }
}
