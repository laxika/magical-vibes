package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;

public class TellingTime extends Card {

    public TellingTime() {
        super("Telling Time", CardType.INSTANT, "{1}{U}", CardColor.BLUE);

        setCardText("Look at the top three cards of your library. Put one of those cards into your hand, one on top of your library, and one on the bottom of your library.");
        addEffect(EffectSlot.SPELL, new LookAtTopCardsHandTopBottomEffect(3));
    }
}
