package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;

public class Peek extends Card {

    public Peek() {
        super("Peek", CardType.INSTANT, "{U}", CardColor.BLUE);

        setCardText("Look at target player's hand.\nDraw a card.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new LookAtHandEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
