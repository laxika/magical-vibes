package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;

import java.util.List;

public class ChoMannoRevolutionary extends Card {

    public ChoMannoRevolutionary() {
        super("Cho-Manno, Revolutionary", CardType.CREATURE, "{2}{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.REBEL));
        setCardText("Prevent all damage that would be dealt to Cho-Manno, Revolutionary.");
        setPower(2);
        setToughness(2);
        setStaticEffects(List.of(new PreventAllDamageEffect()));
    }
}
