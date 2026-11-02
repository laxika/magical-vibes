package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class PrickleFaeries extends Card {

    public PrickleFaeries() {
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                new DealDamageIfFewCardsInHandEffect(2, 2));
    }
}
