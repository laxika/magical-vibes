package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "TSP", collectorNumber = "89")
public class TruthOrTale extends Card {

    public TruthOrTale() {
        addEffect(EffectSlot.SPELL, new RevealTopCardsAndSeparateEffect(
                5, CardPileDisposition.ONE_FROM_CHOSEN_HAND_AND_BOTTOM, true));
    }
}
