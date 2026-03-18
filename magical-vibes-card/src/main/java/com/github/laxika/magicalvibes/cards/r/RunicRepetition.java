package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasFlashbackPredicate;

@CardRegistration(set = "ISD", collectorNumber = "72")
public class RunicRepetition extends Card {

    public RunicRepetition() {
        addEffect(EffectSlot.SPELL, new ReturnTargetCardFromExileToHandEffect(new CardHasFlashbackPredicate(), true));
    }
}
