package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class CounselOfTheSoratami extends Card {

    public CounselOfTheSoratami() {
        super("Counsel of the Soratami", CardType.SORCERY, "{2}{U}", CardColor.BLUE);

        setCardText("Draw two cards.");
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
