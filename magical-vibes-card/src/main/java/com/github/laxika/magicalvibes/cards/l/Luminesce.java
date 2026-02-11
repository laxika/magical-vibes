package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromColorsEffect;

import java.util.List;
import java.util.Set;

public class Luminesce extends Card {

    public Luminesce() {
        super("Luminesce", CardType.INSTANT, "{W}", CardColor.WHITE);

        setCardText("Prevent all damage that black sources and red sources would deal this turn.");
        setSpellEffects(List.of(new PreventDamageFromColorsEffect(Set.of(CardColor.BLACK, CardColor.RED))));
    }
}
