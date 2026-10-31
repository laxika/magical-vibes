package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

public class FungusFrolic extends Card {

    public FungusFrolic() {
        addSpellEffect(new CreateTokenEffect(
                2, "Saproling", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));
    }
}
