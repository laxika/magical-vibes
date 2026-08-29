package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandBottomExileEffect;

@CardRegistration(set = "STX", collectorNumber = "186")
public class ExpressiveIteration extends Card {

    public ExpressiveIteration() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsHandBottomExileEffect(3));
    }
}
