package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MH2", collectorNumber = "29")
public class SearchThePremises extends Card {

    public SearchThePremises() {
        // Whenever a creature attacks you or a planeswalker you control, investigate.
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU, CreateTokenEffect.ofClueToken(1));
    }
}
