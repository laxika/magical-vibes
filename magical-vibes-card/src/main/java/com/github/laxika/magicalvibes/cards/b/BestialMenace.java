package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "97")
public class BestialMenace extends Card {

    public BestialMenace() {
        // Create a 1/1 green Snake, a 2/2 green Wolf, and a 3/3 green Elephant token.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect("Snake", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SNAKE), Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect("Wolf", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.WOLF), Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect("Elephant", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.ELEPHANT), Set.of(), Set.of()));
    }
}
