package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "187")
public class TrumpetingHerd extends Card {

    public TrumpetingHerd() {
        // Create a 3/3 green Elephant creature token.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Elephant", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.ELEPHANT), Set.of(), Set.of()));
    }
}
