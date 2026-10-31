package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

public class Haggle extends Card {

    public Haggle() {
        addSpellEffect(new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
