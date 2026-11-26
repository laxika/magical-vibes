package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOK", collectorNumber = "98")
public class GazeOfAdamaro extends Card {

    public GazeOfAdamaro() {
        // Deals damage to target player equal to the number of cards in that player's hand.
        addSpellEffect(DealDamageEffect.to(Subjects.targetPlayer(), new CardsInHand(CountScope.TARGET_PLAYER)));
    }
}
