package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "48")
public class NestedGhoul extends Card {

    public NestedGhoul() {
        // Whenever a source deals damage to this creature, create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new CreateTokenEffect(
                "Zombie", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.ZOMBIE),
                Set.of(), Set.of()));
    }
}
