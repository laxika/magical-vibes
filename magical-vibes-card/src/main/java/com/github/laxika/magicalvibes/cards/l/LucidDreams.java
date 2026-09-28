package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MH2", collectorNumber = "50")
public class LucidDreams extends Card {

    public LucidDreams() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new CardTypesAmongCardsInGraveyard()));
    }
}
