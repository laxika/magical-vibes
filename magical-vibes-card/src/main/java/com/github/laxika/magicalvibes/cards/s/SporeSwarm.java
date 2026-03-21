package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "180")
public class SporeSwarm extends Card {

    public SporeSwarm() {
        // Create three 1/1 green Saproling creature tokens.
        addEffect(EffectSlot.SPELL, new CreateCreatureTokenEffect(3, "Saproling", 1, 1,
                CardColor.GREEN, List.of(CardSubtype.SAPROLING),
                Set.of(), Set.of()));
    }
}
