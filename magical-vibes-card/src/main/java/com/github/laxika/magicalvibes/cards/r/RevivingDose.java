package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

public class RevivingDose extends Card {

    public RevivingDose() {
        super("Reviving Dose", CardType.INSTANT, "{2}{W}", CardColor.WHITE);

        setCardText("You gain 3 life.\nDraw a card.");
        setSpellEffects(List.of(new GainLifeEffect(3), new DrawCardEffect()));
    }
}
