package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureAndSameNameCardsFromGraveyardToHandEffect;

@CardRegistration(set = "MH2", collectorNumber = "83")
public class EchoingReturn extends Card {

    public EchoingReturn() {
        addEffect(EffectSlot.SPELL, new ReturnTargetCreatureAndSameNameCardsFromGraveyardToHandEffect());
    }
}
