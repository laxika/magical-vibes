package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "146")
public class CallOfTheConclave extends Card {

    public CallOfTheConclave() {
        // Create a 3/3 green Centaur creature token.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Centaur", 3, 3, CardColor.GREEN, List.of(CardSubtype.CENTAUR), Set.of(), Set.of()));
    }
}
