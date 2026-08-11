package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealTopCardsToHandRestToExileEffect;

@CardRegistration(set = "INV", collectorNumber = "100")
public class DesperateResearch extends Card {

    public DesperateResearch() {
        addEffect(EffectSlot.SPELL, new ChooseNameRevealTopCardsToHandRestToExileEffect(7));
    }
}
