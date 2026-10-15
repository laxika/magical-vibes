package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandThenPutOntoBattlefieldIfSharesNameWithPermanentEffect;

@CardRegistration(set = "TOR", collectorNumber = "46")
public class RetracedImage extends Card {

    public RetracedImage() {
        addEffect(EffectSlot.SPELL, new RevealCardFromHandThenPutOntoBattlefieldIfSharesNameWithPermanentEffect());
    }
}
