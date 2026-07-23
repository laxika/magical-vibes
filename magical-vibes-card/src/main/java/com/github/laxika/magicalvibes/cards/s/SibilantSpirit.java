package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerMayDrawCardEffect;

@CardRegistration(set = "6ED", collectorNumber = "98")
@CardRegistration(set = "5ED", collectorNumber = "123")
@CardRegistration(set = "ICE", collectorNumber = "97")
public class SibilantSpirit extends Card {

    public SibilantSpirit() {
        // Flying is loaded from Scryfall metadata.
        // Whenever this creature attacks, defending player may draw a card.
        addEffect(EffectSlot.ON_ATTACK, new DefendingPlayerMayDrawCardEffect());
    }
}
