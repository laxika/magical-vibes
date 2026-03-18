package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerCreatureCardInGraveyardEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "189")
public class KessigCagebreakers extends Card {

    public KessigCagebreakers() {
        // Whenever Kessig Cagebreakers attacks, create a 2/2 green Wolf creature token
        // that's tapped and attacking for each creature card in your graveyard.
        addEffect(EffectSlot.ON_ATTACK, new CreateTokensPerCreatureCardInGraveyardEffect(
                "Wolf", 2, 2, CardColor.GREEN, List.of(CardSubtype.WOLF), true
        ));
    }
}
