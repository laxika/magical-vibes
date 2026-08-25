package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

public class BringBack extends Card {

    public BringBack() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2,
                "Human",
                1,
                1,
                CardColor.WHITE,
                List.of(CardSubtype.HUMAN),
                Set.of(),
                Set.of()
        ));
    }
}
