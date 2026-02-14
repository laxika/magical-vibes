package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlagiarizeEffect;

public class Plagiarize extends Card {

    public Plagiarize() {
        super("Plagiarize", CardType.INSTANT, "{3}{U}", CardColor.BLUE);

        setCardText("Until end of turn, if target player would draw a card, instead that player skips that draw and you draw a card.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new PlagiarizeEffect());
    }
}
