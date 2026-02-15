package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealOpponentHandsEffect;

public class Telepathy extends Card {

    public Telepathy() {
        super("Telepathy", CardType.ENCHANTMENT, "{U}", CardColor.BLUE);

        setCardText("Your opponents play with their hands revealed.");
        addEffect(EffectSlot.STATIC, new RevealOpponentHandsEffect());
    }
}
