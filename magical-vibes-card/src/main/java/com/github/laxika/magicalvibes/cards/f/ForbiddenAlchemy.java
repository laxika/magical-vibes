package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseOneToHandRestToGraveyardEffect;

@CardRegistration(set = "ISD", collectorNumber = "55")
public class ForbiddenAlchemy extends Card {

    public ForbiddenAlchemy() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsChooseOneToHandRestToGraveyardEffect(4));
        addCastingOption(new FlashbackCast("{6}{B}"));
    }
}
