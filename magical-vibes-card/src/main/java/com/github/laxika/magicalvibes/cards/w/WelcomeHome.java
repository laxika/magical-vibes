package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

public class WelcomeHome extends Card {

    public WelcomeHome() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                3,
                "Bear",
                2,
                2,
                CardColor.GREEN,
                List.of(CardSubtype.BEAR),
                Set.of(),
                Set.of()));
    }
}
