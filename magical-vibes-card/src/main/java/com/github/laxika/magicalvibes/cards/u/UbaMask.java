package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UbaMaskDrawReplacementEffect;

@CardRegistration(set = "CHK", collectorNumber = "272")
public class UbaMask extends Card {

    public UbaMask() {
        // If a player would draw a card, that player exiles that card face up instead.
        // Each player may play lands and cast spells from among cards they exiled with this
        // artifact this turn.
        addEffect(EffectSlot.STATIC, new UbaMaskDrawReplacementEffect());
    }
}
