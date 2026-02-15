package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class Sift extends Card {

    public Sift() {
        super("Sift", CardType.SORCERY, "{3}{U}", CardColor.BLUE);

        setCardText("Draw three cards, then discard a card.");
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new DiscardCardEffect());
    }
}
