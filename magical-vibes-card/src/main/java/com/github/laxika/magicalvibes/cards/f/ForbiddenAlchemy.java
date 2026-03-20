package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseNToHandRestToGraveyardEffect;

@CardRegistration(set = "ISD", collectorNumber = "55")
public class ForbiddenAlchemy extends Card {

    public ForbiddenAlchemy() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsChooseNToHandRestToGraveyardEffect(4, 1));
        addCastingOption(new FlashbackCast("{6}{B}"));
    }
}
