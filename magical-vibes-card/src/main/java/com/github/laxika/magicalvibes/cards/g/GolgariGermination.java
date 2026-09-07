package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "209")
public class GolgariGermination extends Card {

    public GolgariGermination() {
        // Whenever a nontoken creature you control dies, create a 1/1 green Saproling creature token.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));
    }
}
