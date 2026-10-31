package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffect;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;

@CardRegistration(set = "SOM", collectorNumber = "98")
public class MoltenPsyche extends Card {

    public MoltenPsyche() {
        // Each player shuffles the cards from their hand into their library, then draws that many cards.
        addSpellEffect(new ShuffleHandIntoLibraryAndDrawEffect());

        // Metalcraft — If you control three or more artifacts, Molten Psyche deals damage to each
        // opponent equal to the number of cards that player has drawn this turn.
        addSpellEffect(new ConditionalEffect(new Metalcraft(), 
                new DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffect()
        ));
    }
}
