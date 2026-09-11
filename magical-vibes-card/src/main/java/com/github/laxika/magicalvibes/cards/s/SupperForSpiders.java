package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnOpponentCreaturesFromGraveyardAsFoodEffect;

@CardRegistration(set = "HOB", collectorNumber = "86")
public class SupperForSpiders extends Card {

    public SupperForSpiders() {
        addEffect(EffectSlot.SPELL, new ReturnOpponentCreaturesFromGraveyardAsFoodEffect());
    }
}
